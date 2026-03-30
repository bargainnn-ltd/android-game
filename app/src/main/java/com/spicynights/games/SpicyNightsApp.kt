package com.spicynights.games

import android.app.Application
import com.spicynights.games.data.DataManager
import com.spicynights.games.data.local.AppPreferencesRepository

class SpicyNightsApp : Application() {
    lateinit var dataManager: DataManager
        private set
    lateinit var preferencesRepository: AppPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        dataManager = DataManager(this)
        preferencesRepository = AppPreferencesRepository(this)
    }
}
