/**
 * Cloud Functions for NoteShare — FCM Push Notification Delivery
 *
 * Listens to Firestore writes on pair subcollections and sends
 * targeted push notifications to the partner's device(s).
 *
 * IMPORTANT: Requires Firebase Blaze (pay-as-you-go) plan to deploy.
 * The code is ready — upgrade your plan and run `firebase deploy --only functions`.
 */

import * as admin from "firebase-admin";
import {
  onDocumentCreated,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// ═══════════════════════════════════════════════════
// Helper: Resolve partner's FCM tokens from a pairId
// ═══════════════════════════════════════════════════

interface PairDoc {
  user1Id: string;
  user2Id: string;
  status: string;
}

/**
 * Given a pairId and the actorId (who performed the action),
 * returns all FCM tokens for the partner.
 */
async function getPartnerTokens(
  pairId: string,
  actorId: string
): Promise<string[]> {
  // 1. Load the pair document
  const pairSnap = await db.collection("pairs").doc(pairId).get();
  if (!pairSnap.exists) {
    logger.warn(`Pair ${pairId} not found`);
    return [];
  }

  const pair = pairSnap.data() as PairDoc;
  if (pair.status !== "active") {
    logger.info(`Pair ${pairId} is not active (status: ${pair.status}), skipping notification`);
    return [];
  }

  // 2. Determine the partner
  const partnerId = pair.user1Id === actorId ? pair.user2Id : pair.user1Id;
  if (!partnerId) {
    logger.info("No partner found in pair");
    return [];
  }

  // 3. Get partner's FCM tokens
  const userSnap = await db.collection("users").doc(partnerId).get();
  if (!userSnap.exists) {
    logger.warn(`Partner user ${partnerId} not found`);
    return [];
  }

  const userData = userSnap.data();
  const fcmTokens: Record<string, number> = userData?.fcmTokens || {};
  const tokens = Object.keys(fcmTokens);

  if (tokens.length === 0) {
    logger.info(`No FCM tokens found for partner ${partnerId}`);
  }

  return tokens;
}

/**
 * Send a notification to all of a user's devices.
 * Automatically cleans up invalid tokens.
 */
async function sendToPartner(
  tokens: string[],
  title: string,
  body: string,
  data: Record<string, string> = {}
): Promise<void> {
  if (tokens.length === 0) return;

  const message: admin.messaging.MulticastMessage = {
    tokens,
    notification: { title, body },
    data,
    android: {
      priority: "high",
      notification: {
        channelId: "noteshare_channel",
        priority: "high",
      },
    },
  };

  const response = await messaging.sendEachForMulticast(message);

  // Clean up invalid tokens
  if (response.failureCount > 0) {
    response.responses.forEach((resp, idx) => {
      if (resp.error) {
        const errorCode = resp.error.code;
        if (
          errorCode === "messaging/invalid-registration-token" ||
          errorCode === "messaging/registration-token-not-registered"
        ) {
          logger.info(`Removing invalid FCM token: ${tokens[idx].substring(0, 10)}...`);
          // Find the user with this token and remove it
          cleanupStaleToken(tokens[idx]);
        }
      }
    });
  }

  logger.info(
    `Sent ${response.successCount}/${tokens.length} notifications (${response.failureCount} failures)`
  );
}

/**
 * Remove a stale FCM token from whichever user document contains it.
 */
async function cleanupStaleToken(token: string): Promise<void> {
  try {
    // Query all users who have this token
    const usersSnap = await db
      .collection("users")
      .where(`fcmTokens.${token}`, ">", 0)
      .get();

    for (const doc of usersSnap.docs) {
      await doc.ref.update({
        [`fcmTokens.${token}`]: admin.firestore.FieldValue.delete(),
      });
      logger.info(`Cleaned up stale token from user ${doc.id}`);
    }
  } catch (e) {
    logger.error("Failed to clean up stale token", e);
  }
}

// ═══════════════════════════════════════════════════
// Trigger: New Note Created
// ═══════════════════════════════════════════════════
export const onNoteCreated = onDocumentCreated(
  "pairs/{pairId}/notes/{noteId}",
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const pairId = event.params.pairId;
    const authorId = data.authorId as string;
    const authorName = data.authorName as string || "Your partner";
    const title = data.title as string;
    const scheduledAt = data.scheduledAt as number | null;

    // BR-301: Time Capsule — don't notify for future-scheduled notes
    if (scheduledAt && scheduledAt > Date.now()) {
      logger.info(`Note ${event.params.noteId} is scheduled for the future, skipping notification`);
      return;
    }

    const tokens = await getPartnerTokens(pairId, authorId);

    const notePreview = title || "New note";
    await sendToPartner(
      tokens,
      `${authorName} shared a note 📝`,
      notePreview,
      { type: "note", noteId: event.params.noteId }
    );
  }
);

// ═══════════════════════════════════════════════════
// Trigger: New Mood Check-in
// ═══════════════════════════════════════════════════
export const onMoodCreated = onDocumentCreated(
  "pairs/{pairId}/moods/{moodId}",
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const pairId = event.params.pairId;
    const userId = data.userId as string;
    const userName = data.userName as string || "Your partner";
    const level = data.level as number;

    const moodEmoji: Record<number, string> = {
      1: "😢",
      2: "😕",
      3: "😐",
      4: "😊",
      5: "🥰",
    };

    const emoji = moodEmoji[level] || "😐";
    const tokens = await getPartnerTokens(pairId, userId);

    await sendToPartner(
      tokens,
      `${userName} checked in ${emoji}`,
      data.note ? (data.note as string) : "Tap to see how they're feeling",
      { type: "mood", moodId: event.params.moodId }
    );
  }
);

// ═══════════════════════════════════════════════════
// Trigger: New Calendar Event
// ═══════════════════════════════════════════════════
export const onEventCreated = onDocumentCreated(
  "pairs/{pairId}/events/{eventId}",
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const pairId = event.params.pairId;
    const createdBy = data.createdBy as string;
    const eventTitle = data.title as string || "New event";
    const eventType = data.type as string || "custom";

    const typeEmoji: Record<string, string> = {
      birthday: "🎂",
      anniversary: "💕",
      date: "🌹",
      custom: "📅",
    };

    const emoji = typeEmoji[eventType] || "📅";
    const tokens = await getPartnerTokens(pairId, createdBy);

    await sendToPartner(
      tokens,
      `New event added ${emoji}`,
      eventTitle,
      { type: "event", eventId: event.params.eventId }
    );
  }
);

// ═══════════════════════════════════════════════════
// Trigger: Join Request on Pair
// ═══════════════════════════════════════════════════
export const onJoinRequested = onDocumentUpdated(
  "pairs/{pairId}",
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after) return;

    // Only trigger when pendingJoinUserId transitions from null to a value
    if (before.pendingJoinUserId || !after.pendingJoinUserId) return;

    const pairId = event.params.pairId;
    const inviterId = after.user1Id as string;
    const requesterName = after.pendingJoinUserName as string || "Someone";

    // Notify the inviter (user1) that someone wants to join
    const inviterSnap = await db.collection("users").doc(inviterId).get();
    if (!inviterSnap.exists) return;

    const inviterData = inviterSnap.data();
    const fcmTokens: Record<string, number> = inviterData?.fcmTokens || {};
    const tokens = Object.keys(fcmTokens);

    await sendToPartner(
      tokens,
      "New pair request 💑",
      `${requesterName} wants to connect with you`,
      { type: "join_request", pairId }
    );
  }
);
