package com.iskcon.bhagavaddarshan

import android.app.Application
import android.util.Log
import com.iskcon.bhagavaddarshan.data.AgentSeeder
import com.iskcon.bhagavaddarshan.data.AppDatabase
import com.iskcon.bhagavaddarshan.data.PlanSeeder
import com.iskcon.bhagavaddarshan.data.SessionManager
import com.iskcon.bhagavaddarshan.data.SubscriberSeedImporter
import com.iskcon.bhagavaddarshan.data.SubscriptionRepository
import com.iskcon.bhagavaddarshan.whatsapp.WhatsAppClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BhagavadDarshanApp : Application() {
    lateinit var repository: SubscriptionRepository
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var session: SessionManager
        private set
    lateinit var whatsAppClient: WhatsAppClient
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        repository = SubscriptionRepository(database.subscriptionDao())
        session = SessionManager(this)
        whatsAppClient = WhatsAppClient()
        appScope.launch {
            AgentSeeder.seedIfEmpty(database.agentDao())
            PlanSeeder.seedIfEmpty(database.planDao())
            // Reset seed flag if DB was wiped by migration
            val prefs = getSharedPreferences("seed", MODE_PRIVATE)
            if (database.subscriptionDao().count() == 0L) {
                prefs.edit().putBoolean("subscribers_pdf_imported_v3", false).apply()
            }
            val n = SubscriberSeedImporter(this@BhagavadDarshanApp, database.subscriptionDao())
                .importIfNeeded()
            if (n > 0) {
                Log.i("BhagavadDarshan", "Imported $n subscribers from PDF seed")
            }
        }
    }
}
