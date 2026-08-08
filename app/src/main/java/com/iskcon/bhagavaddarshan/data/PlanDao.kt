package com.iskcon.bhagavaddarshan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE active = 1 ORDER BY sortOrder ASC, years ASC")
    fun observeActive(): Flow<List<SubscriptionPlanEntity>>

    @Query("SELECT * FROM plans ORDER BY sortOrder ASC, years ASC")
    fun observeAll(): Flow<List<SubscriptionPlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SubscriptionPlanEntity?

    @Query("SELECT * FROM plans WHERE years = :years LIMIT 1")
    suspend fun getByYears(years: Int): SubscriptionPlanEntity?

    @Query("SELECT COUNT(*) FROM plans")
    suspend fun count(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: SubscriptionPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<SubscriptionPlanEntity>)

    @Update
    suspend fun update(plan: SubscriptionPlanEntity)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
