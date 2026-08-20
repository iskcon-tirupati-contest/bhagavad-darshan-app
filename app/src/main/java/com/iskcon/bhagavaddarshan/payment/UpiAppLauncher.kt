package com.iskcon.bhagavaddarshan.payment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

enum class UpiWallet(
    val label: String,
    val packageName: String,
    val razorpayAppId: String
) {
    PHONEPE("PhonePe", "com.phonepe.app", "phonepe"),
    PAYTM("Paytm", "net.one97.paytm", "paytm"),
    GOOGLE_PAY("Google Pay", "com.google.android.apps.nbu.paisa.user", "google_pay")
}

object UpiAppLauncher {

    fun isInstalled(context: Context, wallet: UpiWallet): Boolean =
        try {
            context.packageManager.getPackageInfo(wallet.packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    /**
     * Opens the given UPI wallet with a UPI / wallet deep link.
     * @return null on success, or an error message.
     */
    fun launch(context: Context, wallet: UpiWallet, rawUri: String): String? {
        if (!isInstalled(context, wallet)) {
            return "${wallet.label} is not installed"
        }
        val candidates = uriCandidates(wallet, rawUri)
        var lastError: String? = null
        for (uri in candidates) {
            val err = launchUri(context, wallet.packageName, uri)
            if (err == null) return null
            lastError = err
        }
        return lastError ?: "${wallet.label} could not open UPI"
    }

    private fun launchUri(context: Context, packageName: String, uri: String): String? {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                setPackage(packageName)
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            if (context !is android.app.Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            null
        } catch (_: ActivityNotFoundException) {
            "not found"
        } catch (e: Exception) {
            e.message ?: "open failed"
        }
    }

    private fun uriCandidates(wallet: UpiWallet, rawUri: String): List<String> {
        val upi = toUpiPay(rawUri)
        val query = upi.substringAfter("upi://pay", missingDelimiterValue = "")
        val out = LinkedHashSet<String>()
        if (rawUri.startsWith("upi:") || rawUri.startsWith("phonepe:") ||
            rawUri.startsWith("paytmmp:") || rawUri.startsWith("tez:")
        ) {
            out += rawUri
        }
        if (upi.startsWith("upi:")) out += upi
        when (wallet) {
            UpiWallet.PHONEPE -> {
                if (query.isNotBlank()) {
                    out += "phonepe://pay$query"
                    out += "ppe://pay$query"
                }
            }
            UpiWallet.PAYTM -> {
                if (query.isNotBlank()) {
                    out += "paytmmp://pay$query"
                    out += "paytmmp://upi/pay$query"
                }
            }
            UpiWallet.GOOGLE_PAY -> {
                if (query.isNotBlank()) {
                    out += "tez://upi/pay$query"
                    out += "gpay://upi/pay$query"
                }
            }
        }
        return out.toList()
    }

    private fun toUpiPay(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("upi:")) return trimmed
        if (trimmed.startsWith("intent:", ignoreCase = true)) {
            val scheme = Regex("scheme=([a-zA-Z0-9]+)").find(trimmed)?.groupValues?.getOrNull(1)
            val path = trimmed.removePrefix("intent:").substringBefore("#Intent")
            return if (scheme.isNullOrBlank()) {
                if (path.startsWith("//pay")) "upi:$path" else trimmed
            } else {
                "$scheme:$path"
            }
        }
        return trimmed
    }
}
