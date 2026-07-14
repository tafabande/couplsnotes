package com.example.noteshare.data.repository

import com.example.noteshare.data.model.Pair
import com.example.noteshare.data.remote.FirestoreDataSource
import com.example.noteshare.util.Constants
import com.example.noteshare.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import java.util.concurrent.TimeUnit

@Singleton
class PairRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val authRepository: AuthRepository
) {
    /**
     * Create a new pair and generate an invite code.
     */
    suspend fun createPair(userId: String): Result<Pair> {
        return try {
            // BR-102: A user must explicitly terminate their current relationship bond before creating a new one
            val user = firestoreDataSource.getUser(userId) ?: return Result.Error("User not found")
            if (user.pairId != null) {
                val existingPair = firestoreDataSource.getPair(user.pairId)
                if (existingPair != null && existingPair.isActive) {
                    return Result.Error("BR-102 Violation: User already belongs to an active pair. Unpair first.")
                }
            }

            val inviteCode = generateInviteCode()
            val pair = Pair(
                user1Id = userId,
                inviteCode = inviteCode,
                status = Constants.PAIR_STATUS_PENDING,
                createdAt = System.currentTimeMillis()
            )
            val pairId = firestoreDataSource.createPair(pair)

            // Update user's pairId
            firestoreDataSource.updateUser(userId, mapOf("pairId" to pairId))

            val createdPair = pair.copy(id = pairId)
            Result.Success(createdPair)
        } catch (e: Exception) {
            Result.Error("Failed to create pair: ${e.message}", e)
        }
    }

    /**
     * Join an existing pair using an invite code.
     */
    suspend fun joinPair(userId: String, inviteCode: String): Result<Pair> {
        return try {
            // BR-102: A user must explicitly terminate their current relationship bond before joining a new one
            val user = firestoreDataSource.getUser(userId) ?: return Result.Error("User not found")
            if (user.pairId != null) {
                val existingPair = firestoreDataSource.getPair(user.pairId)
                if (existingPair != null && existingPair.isActive) {
                    return Result.Error("BR-102 Violation: User already belongs to an active pair. Unpair first.")
                }
            }

            val pair = firestoreDataSource.findPairByInviteCode(inviteCode)
                ?: return Result.Error("Invalid or expired invite code")

            // BR-103: Invite Token Expiration (48 hours)
            val currentTime = System.currentTimeMillis()
            val expirationTime = pair.createdAt + TimeUnit.HOURS.toMillis(48)
            if (currentTime > expirationTime) {
                // Auto flag as invalid by reverting status
                firestoreDataSource.updatePair(pair.id, mapOf("status" to "expired"))
                return Result.Error("BR-103 Violation: Invite code expired (valid for 48 hours)")
            }

            if (pair.user1Id == userId) {
                return Result.Error("You can't join your own pair")
            }

            if (pair.user2Id.isNotBlank()) {
                return Result.Error("This pairing is already linked")
            }

            // Ask the inviter to approve the request instead of auto-linking.
            firestoreDataSource.updatePair(pair.id, mapOf(
                "pendingJoinUserId" to userId,
                "pendingJoinUserName" to (user.displayName.ifBlank { user.email }),
                "joinRequestedAt" to System.currentTimeMillis()
            ))

            Result.Success(pair.copy(
                pendingJoinUserId = userId,
                pendingJoinUserName = user.displayName.ifBlank { user.email },
                joinRequestedAt = System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            Result.Error("Failed to join pair: ${e.message}", e)
        }
    }

    suspend fun approveJoinRequest(pairId: String, approverId: String, requesterId: String): Result<Pair> {
        return try {
            val pair = firestoreDataSource.getPair(pairId) ?: return Result.Error("Pair not found")
            if (!pair.containsUser(approverId)) return Result.Error("You are not part of this pairing")
            if (pair.user1Id != approverId) return Result.Error("Only the inviter can approve requests")
            if (pair.pendingJoinUserId != requesterId) return Result.Error("That request is no longer pending")

            firestoreDataSource.updatePair(pairId, mapOf(
                "user2Id" to requesterId,
                "status" to Constants.PAIR_STATUS_ACTIVE,
                "pendingJoinUserId" to null,
                "pendingJoinUserName" to null,
                "joinRequestedAt" to null
            ))
            firestoreDataSource.updateUser(approverId, mapOf("pairId" to pairId))
            firestoreDataSource.updateUser(requesterId, mapOf("pairId" to pairId))

            Result.Success(pair.copy(
                user2Id = requesterId,
                status = Constants.PAIR_STATUS_ACTIVE,
                pendingJoinUserId = null,
                pendingJoinUserName = null,
                joinRequestedAt = null
            ))
        } catch (e: Exception) {
            Result.Error("Failed to approve request: ${e.message}", e)
        }
    }

    suspend fun rejectJoinRequest(pairId: String, approverId: String): Result<Unit> {
        return try {
            val pair = firestoreDataSource.getPair(pairId) ?: return Result.Error("Pair not found")
            if (pair.user1Id != approverId) return Result.Error("Only the inviter can reject requests")
            firestoreDataSource.updatePair(pairId, mapOf(
                "pendingJoinUserId" to null,
                "pendingJoinUserName" to null,
                "joinRequestedAt" to null
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to reject request: ${e.message}", e)
        }
    }

    /**
     * Get pair details.
     */
    suspend fun getPair(pairId: String): Result<Pair> {
        return try {
            val pair = firestoreDataSource.getPair(pairId)
                ?: return Result.Error("Pair not found")
            Result.Success(pair)
        } catch (e: Exception) {
            Result.Error("Failed to get pair: ${e.message}", e)
        }
    }

    /**
     * Observe pair in real-time.
     */
    fun observePair(pairId: String): Flow<Pair?> {
        return firestoreDataSource.observePair(pairId)
    }

    /**
     * Request to disconnect pair. Enters a 7-day cooldown period.
     * During this period, the status is "disconnecting" and data is read-only.
     */
    suspend fun requestDisconnect(pairId: String): Result<Unit> {
        return try {
            firestoreDataSource.updatePair(pairId, mapOf(
                "status" to Constants.PAIR_STATUS_DISCONNECTING,
                "disconnectRequestedAt" to System.currentTimeMillis(),
                "accessEndsAt" to System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15)
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to request disconnect: ${e.message}", e)
        }
    }

    /**
     * Cancel the disconnect request and restore active status.
     */
    suspend fun cancelDisconnect(pairId: String): Result<Unit> {
        return try {
            firestoreDataSource.updatePair(pairId, mapOf(
                "status" to Constants.PAIR_STATUS_ACTIVE,
                "disconnectRequestedAt" to null,
                "accessEndsAt" to null
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to cancel disconnect: ${e.message}", e)
        }
    }

    suspend fun requestWipe(pairId: String, requesterId: String): Result<Unit> {
        return try {
            firestoreDataSource.updatePair(pairId, mapOf(
                "wipeRequestedBy" to requesterId,
                "wipeRequestedAt" to System.currentTimeMillis()
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to request wipe: ${e.message}", e)
        }
    }

    suspend fun confirmWipe(pairId: String): Result<Unit> {
        return try {
            firestoreDataSource.updatePair(pairId, mapOf(
                "status" to Constants.PAIR_STATUS_DISSOLVED,
                "wipeRequestedBy" to null,
                "wipeRequestedAt" to null
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to wipe pair: ${e.message}", e)
        }
    }

    /**
     * Generate a random 6-character alphanumeric invite code.
     */
    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluding ambiguous chars
        return (1..Constants.INVITE_CODE_LENGTH)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}
