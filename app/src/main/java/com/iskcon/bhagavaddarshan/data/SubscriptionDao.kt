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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(subscription: Subscription): Long

    @Update
    suspend fun update(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
