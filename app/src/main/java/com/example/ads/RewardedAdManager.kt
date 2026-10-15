package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    var isLoading = false
        private set

    fun loadAd(onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        if (!AdConstants.ADS_ENABLED) {
            return
        }
        if (isLoading || isAdLoaded()) {
            if (isAdLoaded()) onLoaded?.invoke()
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdConstants.REWARDED_AD_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d("RewardedAdManager", "Rewarded ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.w("RewardedAdManager", "Rewarded ad failed to load: ${error.message}")
                    onFailed?.invoke(error.message)
                }
            }
        )
    }

    fun isAdLoaded(): Boolean = rewardedAd != null

    fun showAd(
        activity: Activity,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        if (!AdConstants.ADS_ENABLED) {
            onUserEarnedReward(1, "Pro")
            onDismissed()
            return
        }
        val currentAd = rewardedAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadAd()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadAd()
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    // Ad is displaying
                }
            }

            currentAd.show(activity) { rewardItem ->
                Log.d("RewardedAdManager", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward(rewardItem.amount, rewardItem.type)
            }
        } else {
            // If ad was not ready, trigger load and finish
            loadAd()
            onDismissed()
        }
    }
}
