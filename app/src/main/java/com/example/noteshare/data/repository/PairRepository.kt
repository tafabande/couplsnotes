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

            // BR-101: The Duopoly Constriction is naturally enforced here by exactly setting user2Id
            // Update pair with second user and set to active
            firestoreDataSource.updatePair(pair.id, mapOf(
                "user2Id" to userId,
                "status" to Constants.PAIR_STATUS_ACTIVE
            ))

            // Update user's pairId
            firestoreDataSource.updateUser(userId, mapOf("pairId" to pair.id))

            val updatedPair = pair.copy(user2Id = userId, status = Constants.PAIR_STATUS_ACTIVE)
            Result.Success(updatedPair)
        } catch (e: Exception) {
            Result.Error("Failed to join pair: ${e.message}", e)
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
                "disconnectRequestedAt" to System.currentTimeMillis()
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
                "disconnectRequestedAt" to null
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to cancel disconnect: ${e.message}", e)
        }
    }

    /**
     * Dissolve a pair completely (after cooldown).
     */
    suspend fun dissolvePair(pairId: String): Result<Unit> {
        return try {
            firestoreDataSource.updatePair(pairId, mapOf(
                "status" to Constants.PAIR_STATUS_DISSOLVED
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to dissolve pair: ${e.message}", e)
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
