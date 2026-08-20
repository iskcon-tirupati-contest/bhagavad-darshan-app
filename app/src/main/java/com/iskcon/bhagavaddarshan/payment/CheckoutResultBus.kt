package com.iskcon.bhagavaddarshan.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed interface CheckoutResult {
    data class Success(val paymentId: String) : CheckoutResult
    data class Failure(val message: String) : CheckoutResult
}

/**
 * Razorpay Checkout reports back on the Activity, not into Compose.
 * This bridges that callback to whichever checkout screen is open.
 */
object CheckoutResultBus {
    private val _results = MutableSharedFlow<CheckoutResult>(extraBufferCapacity = 4)
    val results: SharedFlow<CheckoutResult> = _results

    fun publish(result: CheckoutResult) {
        _results.tryEmit(result)
    }
}
