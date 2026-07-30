package com.alertmesh.server

import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class PacketLossInjector {
    private val _lossRate = AtomicReference(0.0)

    var lossRate: Double
        get() = _lossRate.get()
        set(value) {
            require(value in 0.0..1.0) { "Loss rate must be between 0.0 and 1.0" }
            _lossRate.set(value)
        }

    fun shouldDrop(): Boolean {
        val rate = lossRate
        if (rate <= 0.0) return false
        if (rate >= 1.0) return true
        return Random.nextDouble() < rate
    }
}
