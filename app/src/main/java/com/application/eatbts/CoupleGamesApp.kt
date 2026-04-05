package com.application.eatbts

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.application.eatbts.ads.AdsManager
import com.application.eatbts.data.DataManager
import com.application.eatbts.data.local.AppPreferencesRepository
import com.application.eatbts.firebase.AnalyticsLogger
import com.application.eatbts.firebase.AuthRepository
import com.application.eatbts.firebase.PlayerProfileRepository
import com.application.eatbts.playgames.PlayGamesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CoupleGamesApp : Application() {

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    lateinit var dataManager: DataManager
        private set
    lateinit var preferencesRepository: AppPreferencesRepository
        private set
    lateinit var analyticsLogger: AnalyticsLogger
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var playerProfileRepository: PlayerProfileRepository
        private set
    lateinit var playGamesManager: PlayGamesManager
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        dataManager = DataManager(this)
        preferencesRepository = AppPreferencesRepository(this)
        analyticsLogger = AnalyticsLogger(FirebaseAnalytics.getInstance(this))
        authRepository = AuthRepository()
        playerProfileRepository = PlayerProfileRepository()
        playGamesManager = PlayGamesManager()

        MobileAds.initialize(this) {}
        AdsManager.init(this, analyticsLogger)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                getString(R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val u = auth.currentUser ?: return@addAuthStateListener
            applicationScope.launch(Dispatchers.IO) {
                runCatching {
                    playerProfileRepository.upsertPlayer(
                        userId = u.uid,
                        name = u.displayName?.trim()?.ifEmpty { null } ?: "Guest",
                        avatarUrl = u.photoUrl?.toString(),
                    )
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@addOnCompleteListener
            applicationScope.launch(Dispatchers.IO) {
                runCatching {
                    playerProfileRepository.upsertPlayer(
                        userId = uid,
                        name = user.displayName?.trim()?.ifEmpty { null } ?: "Guest",
                        avatarUrl = user.photoUrl?.toString(),
                        fcmToken = token,
                    )
                }
            }
        }
    }
}
