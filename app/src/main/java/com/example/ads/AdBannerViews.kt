package com.example.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Anchored Adaptive Banner Ad View (Disabled by user request)
 */
@Composable
fun AnchoredAdaptiveBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConstants.ANCHORED_ADAPTIVE_BANNER_ID
) {
    if (!AdConstants.ADS_ENABLED) {
        return
    }
}

/**
 * Fixed Size Banner Ad View (Disabled by user request)
 */
@Composable
fun FixedSizeBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConstants.FIXED_SIZE_BANNER_ID
) {
    if (!AdConstants.ADS_ENABLED) {
        return
    }
}
