package com.jaincomapny.android_link_preview

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages a single interstitial ad lifecycle.
 *
 * Flow: load ad on init → user taps download → show ad → on dismiss → PDF download starts.
 * If no ad is ready (still loading / failed), download starts immediately as a fallback.
 *
 * Replace [adUnitId] with your real unit ID from the AdMob console before publishing.
 */
class AdManager(context: Context) {

    private val adUnitId = BuildConfig.ADMOB_AD_UNIT_ID

    private var interstitialAd: InterstitialAd? = null
    private val appContext = context.applicationContext

    init {
        loadAd()
    }

    private fun loadAd() {
        InterstitialAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Shows an interstitial ad then calls [onComplete] when it is dismissed.
     * Falls back to calling [onComplete] immediately if no ad is ready.
     */
    fun showAdThenDownload(activity: Activity, onComplete: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            onComplete()
            loadAd()
            return
        }
        interstitialAd = null // consume — don't show the same ad twice
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onComplete()
                loadAd() // preload next
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onComplete()
                loadAd()
            }
        }
        ad.show(activity)
    }
}
