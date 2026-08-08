package com.iskcon.bhagavaddarshan

import android.app.Application
import com.iskcon.bhagavaddarshan.data.AppDatabase
import com.iskcon.bhagavaddarshan.data.SubscriptionRepository

class BhagavadDarshanApp : Application() {
    lateinit var repository: SubscriptionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = SubscriptionRepository(db.subscriptionDao())
    }
}
