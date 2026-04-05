package com.application.eatbts.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.application.eatbts.firebase.AnalyticsLogger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google test ad units — replace with production IDs in Play Console / AdMob.
 */
object AdsManager {

    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

    @Volatile
    private var analytics: AnalyticsLogger? = null

    @Volatile
    private var rewardedAd: RewardedAd? = null

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    private val interstitialLoading = AtomicBoolean(false)

    fun init(context: Context, analyticsLogger: AnalyticsLogger) {
        analytics = analyticsLogger
        loadRewarded(context.applicationContext)
        preloadInterstitial(context.applicationContext)
    }

    fun loadRewarded(context: Context) {
        RewardedAd.load(
            context,
            TEST_REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            },
        )
    }

    fun showRewardedIfReady(
        activity: Activity,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onClosed: () -> Unit,
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewarded(activity.applicationContext)
            onClosed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded(activity.applicationContext)
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                loadRewarded(activity.applicationContext)
                onClosed()
            }
        }
        ad.show(activity) { item ->
            analytics?.logAdReward("rewarded")
            onUserEarnedReward(item.amount, item.type)
        }
        analytics?.logAdImpression("rewarded")
    }

    fun preloadInterstitial(context: Context) {
        if (!interstitialLoading.compareAndSet(false, true)) return
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialLoading.set(false)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    interstitialLoading.set(false)
                }
            },
        )
    }

    fun showInterstitialIfReady(activity: Activity, onDismiss: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            preloadInterstitial(activity.applicationContext)
            onDismiss()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitial(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                preloadInterstitial(activity.applicationContext)
                onDismiss()
            }
        }
        ad.show(activity)
        analytics?.logAdImpression("interstitial")
    }
}
