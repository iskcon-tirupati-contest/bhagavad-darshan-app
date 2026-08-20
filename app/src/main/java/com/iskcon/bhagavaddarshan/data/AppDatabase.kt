package com.iskcon.bhagavaddarshan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Subscription::class, Agent::class, SubscriptionPlanEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun agentDao(): AgentDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bhagavad_darshan.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed runs from Application after get()
                        }
                    })
                    .build()
                    .also { instance = it }
            }
    }
}

object AgentSeeder {
    suspend fun seedIfEmpty(dao: AgentDao) {
        if (dao.count() > 0) return
        dao.insert(
            Agent(
                name = "Temple Admin",
                phone = "9999999999",
                passwordHash = PasswordHasher.hash("admin123"),
                address = "ISKCON Temple Office",
                role = Agent.Role.ADMIN
            )
        )
        dao.insert(
            Agent(
                name = "Demo Agent",
                phone = "8888888888",
                passwordHash = PasswordHasher.hash("agent123"),
                address = "Field seva",
                role = Agent.Role.AGENT
            )
        )
    }
}

object PlanSeeder {
    suspend fun seedIfEmpty(dao: PlanDao) {
        if (dao.count() == 0L) {
            val plans = SubscriptionPlan.entries.mapIndexed { index, plan ->
                SubscriptionPlanEntity.fromEnum(plan, sortOrder = index + 1)
            }
            dao.insertAll(plans)
            return
        }
        // Keep English labels in sync with flyer enum
        SubscriptionPlan.entries.forEach { plan ->
            val existing = dao.getByYears(plan.years) ?: return@forEach
            if (existing.labelTe != plan.labelEn) {
                dao.update(existing.copy(labelTe = plan.labelEn))
            }
        }
    }
}
