package com.iskcon.bhagavaddarshan

import android.app.Application
import com.iskcon.bhagavaddarshan.data.SessionManager
import com.iskcon.bhagavaddarshan.data.SubscriptionRepository
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.whatsapp.WhatsAppClient

/**
 * Application entry. Source of truth is Lightsail Postgres via [BdApi] —
 * local Room is no longer opened or seeded.
 */
class BhagavadDarshanApp : Application() {
    lateinit var repository: SubscriptionRepository
        private set
    lateinit var session: SessionManager
        private set
    lateinit var whatsAppClient: WhatsAppClient
        private set
    lateinit var api: BdApi
        private set

    override fun onCreate() {
        super.onCreate()
        session = SessionManager(this)
        api = BdApi()
        repository = SubscriptionRepository(api, session)
        whatsAppClient = WhatsAppClient()
        // Expiry reminders should run on the server; client worker removed from launch path.
    }
}
