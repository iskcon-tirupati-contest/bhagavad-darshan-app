package com.iskcon.bhagavaddarshan.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import org.json.JSONObject

/**
 * Opens PhonePe / Paytm / GPay directly using Razorpay Custom Checkout (checkout.js)
 * inside a hidden WebView, then intercepts the UPI intent so the Razorpay sheet never shows.
 */
object UpiIntentLauncher {

    private var webView: WebView? = null
    private val main = Handler(Looper.getMainLooper())

    fun start(
        activity: Activity,
        keyId: String,
        orderId: String,
        amountPaise: Int,
        contact: String,
        name: String,
        wallet: UpiWallet,
        onOpened: () -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        val html = buildHtml(
            keyId = keyId,
            orderId = orderId,
            amountPaise = amountPaise,
            contact = contact.filter(Char::isDigit).takeLast(10),
            name = name.ifBlank { "Devotee" },
            wallet = wallet
        )
        val view = WebView(activity)
        @SuppressLint("SetJavaScriptEnabled")
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        view.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 13; Pixel) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"
        view.addJavascriptInterface(
            Bridge { message ->
                main.post {
                    stop()
                    onError(message.ifBlank { "Could not open ${wallet.label}" })
                }
            },
            "AndroidBridge"
        )
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString().orEmpty()
                if (url.isUpiDeepLink()) {
                    val err = UpiAppLauncher.launch(activity, wallet, url)
                    main.post {
                        stop()
                        if (err != null) onError(err) else onOpened()
                    }
                    return true
                }
                return false
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(v: WebView?, url: String?): Boolean {
                val target = url.orEmpty()
                if (target.isUpiDeepLink()) {
                    val err = UpiAppLauncher.launch(activity, wallet, target)
                    main.post {
                        stop()
                        if (err != null) onError(err) else onOpened()
                    }
                    return true
                }
                return false
            }
        }
        view.layoutParams = FrameLayout.LayoutParams(1, 1)
        val parent = activity.findViewById<ViewGroup>(android.R.id.content)
        parent.addView(view)
        webView = view
        view.loadDataWithBaseURL(
            "https://api.razorpay.com",
            html,
            "text/html",
            "UTF-8",
            null
        )
        main.postDelayed({
            if (webView === view) {
                stop()
                onError("Could not open ${wallet.label}. Try again.")
            }
        }, 18_000)
    }

    fun stop() {
        main.removeCallbacksAndMessages(null)
        val view = webView ?: return
        webView = null
        runCatching {
            (view.parent as? ViewGroup)?.removeView(view)
            view.stopLoading()
            view.destroy()
        }
    }

    private class Bridge(private val onError: (String) -> Unit) {
        @JavascriptInterface
        fun onError(message: String) {
            onError.invoke(message)
        }
    }

    private fun buildHtml(
        keyId: String,
        orderId: String,
        amountPaise: Int,
        contact: String,
        name: String,
        wallet: UpiWallet
    ): String {
        val email = "devotee.$contact@iskcon-tirupati.org"
        val payload = JSONObject()
            .put("amount", amountPaise)
            .put("currency", "INR")
            .put("email", email)
            .put("contact", contact)
            .put("key", keyId)
            .put("order_id", orderId)
            .put("method", "upi")
            .put("_[flow]", "intent")
            .put("upi_app_package_name", wallet.packageName)
            .put(
                "upi",
                JSONObject()
                    .put("flow", "intent")
                    .put("app", wallet.razorpayAppId)
            )
            .put("notes", JSONObject().put("name", name).put("wallet", wallet.label))
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8"/>
              <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
            </head>
            <body>
            <script>
              (function () {
                var payload = $payload;
                function fail(msg) {
                  if (window.AndroidBridge) AndroidBridge.onError(String(msg || 'UPI failed'));
                }
                try {
                  var rzp = new Razorpay(payload);
                  rzp.on('payment.failed', function (resp) {
                    var d = (resp && resp.error && resp.error.description) || 'Payment failed';
                    fail(d);
                  });
                  rzp.open();
                } catch (e) {
                  fail(e && e.message ? e.message : 'Could not start UPI');
                }
              })();
            </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun String.isUpiDeepLink(): Boolean {
        val lower = lowercase()
        return lower.startsWith("upi:") ||
            lower.startsWith("phonepe:") ||
            lower.startsWith("ppe:") ||
            lower.startsWith("paytmmp:") ||
            lower.startsWith("tez:") ||
            lower.startsWith("gpay:") ||
            lower.startsWith("intent:")
    }
}
