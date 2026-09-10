package com.iskcon.bhagavaddarshan.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.BuildConfig
import com.iskcon.bhagavaddarshan.data.AppUpdateInfo
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

/** Checks Lightsail-hosted latest APK and prompts when a newer version exists. */
@Composable
fun AppUpdateGate(app: BhagavadDarshanApp) {
    var update by remember { mutableStateOf<AppUpdateInfo?>(null) }
    /** Once user taps Continue for a version, don't re-show it this session. */
    var dismissedVersionCode by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    fun applyRemote(json: JSONObject) {
        val info = parseUpdate(json) ?: run {
            update = null
            return
        }
        val newer = info.versionCode > BuildConfig.VERSION_CODE && info.apkUrl.isNotBlank()
        val notDismissed = info.versionCode > dismissedVersionCode
        update = if (newer && notDismissed) info else null
    }

    fun refresh() {
        scope.launch {
            app.api.getAppVersion("android").onSuccess { applyRemote(it) }
        }
    }

    LaunchedEffect(Unit) {
        delay(600)
        refresh()
    }

    // After installing from the browser, returning to the app re-checks
    // (still respects dismissedVersionCode so Continue stays closed).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    update?.let { info ->
        AppUpdateDialog(
            info = info,
            onDismiss = {
                dismissedVersionCode = info.versionCode
                update = null
            }
        )
    }
}

/** @deprecated Use [AppUpdateGate] — kept so older call sites compile during rename. */
@Composable
fun StaffAppUpdateGate(app: BhagavadDarshanApp) = AppUpdateGate(app)

@Composable
fun AppUpdateDialog(
    info: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var opening by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!opening) onDismiss()
        },
        title = {
            Text(
                "Update available v${info.version}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    info.releaseNotes.ifBlank {
                        "A newer version of Bhagavad Darshan is available."
                    },
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "After installing, return here — or tap Continue to use this version.",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFC62828), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            PressablePrimaryButton(
                text = if (opening) "Opening…" else "Update now",
                compact = true,
                enabled = !opening,
                containerColor = Marigold,
                onClick = {
                    UiSounds.click(context)
                    opening = true
                    error = null
                    try {
                        if (info.apkUrl.isBlank()) {
                            error = "Download link missing"
                            opening = false
                            return@PressablePrimaryButton
                        }
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        opening = false
                    } catch (_: Exception) {
                        error = "Could not open download link"
                        opening = false
                    }
                }
            )
        },
        dismissButton = {
            // Plain TextButton — reliable dismiss (custom pressable was racing refresh).
            TextButton(
                onClick = {
                    UiSounds.click(context)
                    onDismiss()
                }
            ) {
                Text("Continue", fontWeight = FontWeight.SemiBold, color = Marigold)
            }
        }
    )
}

private fun parseUpdate(json: JSONObject): AppUpdateInfo? {
    val code = json.optInt("versionCode", 0)
    val url = json.optString("apkUrl").orEmpty()
    if (code <= 0 || url.isBlank()) return null
    return AppUpdateInfo(
        version = json.optString("version").ifBlank { code.toString() },
        versionCode = code,
        apkUrl = url,
        mandatory = json.optBoolean("mandatory", false),
        releaseNotes = json.optString("releaseNotes").orEmpty()
    )
}
