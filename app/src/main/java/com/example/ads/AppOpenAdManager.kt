package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager(private val context: Context) {
    private var appOpenAd: AppOpenAd? = null
    var isLoading = false
        private set
    var isShowing = false
        private set
    private var loadTime: Long = 0

    fun loadAd(onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        if (!AdConstants.ADS_ENABLED) {
            return
        }
        if (isLoading || isAdAvailable()) {
            if (isAdAvailable()) onLoaded?.invoke()
            return
        }

        isLoading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AdConstants.APP_OPEN_AD_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoading = false
                    loadTime = Date().time
                    Log.d("AppOpenAdManager", "App Open Ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    appOpenAd = null
                    Log.w("AppOpenAdManager", "App Open Ad failed: ${loadAdError.message}")
                    onFailed?.invoke(loadAdError.message)
                }
            }
        )
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour = 3600000L
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    fun showAdIfAvailable(
        activity: Activity,
        onAdDismissed: (() -> Unit)? = null
    ) {
        if (isShowing) {
            Log.d("AppOpenAdManager", "Ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.d("AppOpenAdManager", "Ad not ready; preloading for next time.")
            loadAd()
            onAdDismissed?.invoke()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowing = false
                loadAd()
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowing = false
                loadAd()
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                isShowing = true
            }
        }

        appOpenAd?.show(activity)
    }
}
