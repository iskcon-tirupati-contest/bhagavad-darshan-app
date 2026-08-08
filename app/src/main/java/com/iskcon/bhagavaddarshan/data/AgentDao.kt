package com.iskcon.bhagavaddarshan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): Agent?

    @Query("SELECT * FROM agents WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Agent?

    @Query("SELECT * FROM agents ORDER BY name ASC")
    fun observeAll(): Flow<List<Agent>>

    @Query("SELECT * FROM agents WHERE role = 'agent' ORDER BY name ASC")
    fun observeAgentsOnly(): Flow<List<Agent>>

    @Query("SELECT COUNT(*) FROM agents")
    suspend fun count(): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(agent: Agent): Long

    @Update
    suspend fun update(agent: Agent)

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun deleteById(id: Long)
}
