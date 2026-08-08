package com.iskcon.bhagavaddarshan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Subscription>>

    @Query(
        """
        SELECT * FROM subscriptions
        WHERE name LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR CAST(receiptNo AS TEXT) LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<Subscription>>

    @Query(
        """
        SELECT * FROM subscriptions
        WHERE name LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR CAST(receiptNo AS TEXT) LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun searchAll(query: String): Flow<List<Subscription>>

    @Query(
        """
        SELECT * FROM subscriptions
        WHERE agentId = :agentId
          AND (
            name LIKE '%' || :query || '%'
            OR phone LIKE '%' || :query || '%'
            OR CAST(receiptNo AS TEXT) LIKE '%' || :query || '%'
          )
        ORDER BY createdAt DESC
        """
    )
    fun searchForAgent(agentId: Long, query: String): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE agentId = :agentId ORDER BY createdAt DESC")
    fun observeByAgent(agentId: Long): Flow<List<Subscription>>

    @Query(
        """
        SELECT * FROM subscriptions
        WHERE status IN ('pending_payment', 'payment_failed')
        ORDER BY createdAt DESC
        """
    )
    fun observePending(): Flow<List<Subscription>>

    @Query(
        """
        SELECT * FROM subscriptions
        WHERE agentId = :agentId
          AND status IN ('pending_payment', 'payment_failed')
        ORDER BY createdAt DESC
        """
    )
    fun observePendingForAgent(agentId: Long): Flow<List<Subscription>>

    @Query(
        """
        SELECT * FROM subscriptions
        WHERE endDate <= :beforeDate
          AND status != 'expired'
        ORDER BY endDate ASC
        """
    )
    fun observeExpiringBefore(beforeDate: String): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Subscription?

    @Query("SELECT COALESCE(MAX(receiptNo), 510556) FROM subscriptions")
    suspend fun maxReceiptNo(): Long

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun count(): Long

    @Query("SELECT COUNT(*) FROM subscriptions WHERE agentId = :agentId")
    suspend fun countByAgent(agentId: Long): Long

    @Query("SELECT COUNT(*) FROM subscriptions WHERE createdAt BETWEEN :from AND :to")
    suspend fun countBetween(from: Long, to: Long): Long

    @Query(
        "SELECT COUNT(*) FROM subscriptions WHERE agentId = :agentId AND createdAt BETWEEN :from AND :to"
    )
    suspend fun countByAgentBetween(agentId: Long, from: Long, to: Long): Long

    @Query(
        """
        SELECT COUNT(*) FROM subscriptions
        WHERE endDate >= :fromDate AND endDate <= :toDate
          AND status != 'expired'
        """
    )
    suspend fun countExpiringBetween(fromDate: String, toDate: String): Long

    @Query(
        """
        SELECT COUNT(*) FROM subscriptions
        WHERE agentId = :agentId
          AND endDate >= :fromDate AND endDate <= :toDate
          AND status != 'expired'
        """
    )
    suspend fun countExpiringByAgentBetween(
        agentId: Long,
        fromDate: String,
        toDate: String
    ): Long

    @Query("SELECT * FROM subscriptions WHERE createdAt BETWEEN :from AND :to")
    suspend fun listBetween(from: Long, to: Long): List<Subscription>

    @Query(
        "SELECT * FROM subscriptions WHERE agentId = :agentId AND createdAt BETWEEN :from AND :to"
    )
    suspend fun listByAgentBetween(agentId: Long, from: Long, to: Long): List<Subscription>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(subscription: Subscription): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<Subscription>)

    @Update
    suspend fun update(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
