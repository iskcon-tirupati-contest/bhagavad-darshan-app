package com.iskcon.bhagavaddarshan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agents")
data class Agent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val passwordHash: String,
    val address: String = "",
    val photoPath: String = "",
    val role: String = Role.AGENT,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    object Role {
        const val ADMIN = "admin"
        const val AGENT = "agent"
        /** Session-only role for devotee login (not stored in agents table). */
        const val CUSTOMER = "customer"
    }
}
