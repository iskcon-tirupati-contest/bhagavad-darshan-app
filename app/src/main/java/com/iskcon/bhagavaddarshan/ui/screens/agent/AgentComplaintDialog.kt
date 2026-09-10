package com.iskcon.bhagavaddarshan.ui.screens.agent

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.ui.components.PressableOutlineButton
import com.iskcon.bhagavaddarshan.ui.components.PressablePrimaryButton
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.util.FormValidators
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.launch

data class AgentIssueOption(val code: String, val label: String)

val AgentIssueOptions = listOf(
    AgentIssueOption("otp_whatsapp", "OTP not received on WhatsApp"),
    AgentIssueOption("cant_install", "Cannot download or install the app"),
    AgentIssueOption("app_crash", "App crashes or will not open"),
    AgentIssueOption("login_failed", "Login / OTP verification failed"),
    AgentIssueOption("update_issue", "App update not showing"),
    AgentIssueOption("other", "Other")
)

@Composable
fun AgentComplaintDialog(
    app: BhagavadDarshanApp,
    initialName: String,
    initialPhone: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone.filter { it.isDigit() }.takeLast(10)) }
    var deviceModel by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf<AgentIssueOption?>(null) }
    var otherDetails by remember { mutableStateOf("") }
    var issueMenuOpen by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Marigold,
        unfocusedBorderColor = Marigold.copy(alpha = 0.75f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("📝 Register a complaint", fontWeight = FontWeight.Bold)
        },
        text = {
            if (success) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "✅ Complaint registered successfully.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Our team will contact you on WhatsApp soon.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Use your WhatsApp number so we can reach you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it.filter { c -> c.isDigit() }.take(10)
                            error = null
                        },
                        label = { Text("WhatsApp mobile") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = deviceModel,
                        onValueChange = { deviceModel = it; error = null },
                        label = { Text("Mobile model") },
                        placeholder = { Text("e.g. Redmi Note 12") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    Column {
                        OutlinedTextField(
                            value = issue?.label ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Issue faced") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    null,
                                    Modifier.clickable { issueMenuOpen = true }
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.ReportProblem, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { issueMenuOpen = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        DropdownMenu(
                            expanded = issueMenuOpen,
                            onDismissRequest = { issueMenuOpen = false }
                        ) {
                            AgentIssueOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.label) },
                                    onClick = {
                                        issue = opt
                                        issueMenuOpen = false
                                        error = null
                                    }
                                )
                            }
                        }
                    }
                    if (issue?.code == "other" || !otherDetails.isBlank() && issue != null) {
                        OutlinedTextField(
                            value = otherDetails,
                            onValueChange = { otherDetails = it; error = null },
                            label = {
                                Text(if (issue?.code == "other") "Describe the issue*" else "More details (optional)")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    } else if (issue != null) {
                        OutlinedTextField(
                            value = otherDetails,
                            onValueChange = { otherDetails = it; error = null },
                            label = { Text("More details (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        dismissButton = {
            PressableOutlineButton(
                text = if (success) "Close" else "Cancel",
                onClick = {
                    UiSounds.click(context)
                    onDismiss()
                }
            )
        },
        confirmButton = {
            if (!success) {
                PressablePrimaryButton(
                    text = if (submitting) "Submitting…" else "Submit",
                    enabled = !submitting,
                    onClick = {
                        FormValidators.name(name)?.let {
                            error = it
                            UiSounds.error(context)
                            return@PressablePrimaryButton
                        }
                        FormValidators.phone(phone)?.let {
                            error = it
                            UiSounds.error(context)
                            return@PressablePrimaryButton
                        }
                        if (deviceModel.trim().length < 2) {
                            error = "Enter your mobile model"
                            UiSounds.error(context)
                            return@PressablePrimaryButton
                        }
                        val selected = issue
                        if (selected == null) {
                            error = "Select an issue"
                            UiSounds.error(context)
                            return@PressablePrimaryButton
                        }
                        if (selected.code == "other" && otherDetails.trim().length < 5) {
                            error = "Please describe your issue"
                            UiSounds.error(context)
                            return@PressablePrimaryButton
                        }
                        submitting = true
                        error = null
                        scope.launch {
                            val result = app.api.submitAgentSupportComplaint(
                                name = name.trim(),
                                phone = phone.trim(),
                                deviceModel = deviceModel.trim(),
                                issue = selected.code,
                                otherDetails = otherDetails.trim()
                            )
                            submitting = false
                            result.fold(
                                onSuccess = {
                                    success = true
                                    UiSounds.success(context)
                                    Toast.makeText(
                                        context,
                                        it.optString(
                                            "message",
                                            "Complaint registered successfully."
                                        ),
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onFailure = {
                                    error = it.message ?: "Could not submit complaint"
                                    UiSounds.error(context)
                                }
                            )
                        }
                    }
                )
            }
        }
    )
}
