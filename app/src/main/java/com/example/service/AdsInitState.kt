package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the ads SDK is up yet.
 *
 * Consent has to be gathered before `MobileAds.initialize`, and initialisation
 * is itself asynchronous, so the first screen composes well before ads can be
 * requested. A banner that asked at that moment got a failure and — because it
 * never asked again — stayed blank for the whole session. Views wait on this
 * instead of guessing.
 */
object AdsInitState {
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun markReady() { _ready.value = true }
}
