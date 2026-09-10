package com.iskcon.bhagavaddarshan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.BuildConfig
import com.iskcon.bhagavaddarshan.ui.components.AgentHeaderTitle
import com.iskcon.bhagavaddarshan.ui.components.GradientTopBar
import com.iskcon.bhagavaddarshan.ui.components.PressableOutlineButton
import com.iskcon.bhagavaddarshan.ui.components.PressablePrimaryButton
import com.iskcon.bhagavaddarshan.ui.components.SectionLabel
import com.iskcon.bhagavaddarshan.ui.components.ShadowCard
import com.iskcon.bhagavaddarshan.ui.theme.AgentGoldSheenBrush
import com.iskcon.bhagavaddarshan.ui.theme.AgentHeroBrush
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
import com.iskcon.bhagavaddarshan.util.FormValidators
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    app: BhagavadDarshanApp,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showComplaint by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val agentId = app.session.agentId
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(agentId) {
        if (agentId <= 0L) return@LaunchedEffect
        val json = app.api.getAgent(app.session.authToken, agentId).getOrNull()
        val a = json?.optJSONObject("agent") ?: json
        if (a != null) {
            name = a.optString("name")
            phone = a.optString("phone")
            address = a.optString("address")
        }
    }

    if (showComplaint) {
        com.iskcon.bhagavaddarshan.ui.screens.agent.AgentComplaintDialog(
            app = app,
            initialName = name,
            initialPhone = phone,
            onDismiss = { showComplaint = false }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("🚪 Logout?", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to logout now?") },
            dismissButton = {
                PressableOutlineButton(
                    text = "Cancel",
                    onClick = {
                        UiSounds.click(context)
                        showLogoutConfirm = false
                    }
                )
            },
            confirmButton = {
                PressablePrimaryButton(
                    text = "Logout",
                    onClick = {
                        UiSounds.click(context)
                        showLogoutConfirm = false
                        onLogout()
                    }
                )
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GradientTopBar(
                brush = AgentHeroBrush,
                title = { AgentHeaderTitle(title = "👤 Profile") },
                actions = {
                    IconButton(onClick = {
                        UiSounds.click(context)
                        showLogoutConfirm = true
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = TempleGreen
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(92.dp)
                    .background(AgentGoldSheenBrush, CircleShape)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.take(1).ifBlank { "A" }.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                "🙏 ${name.ifBlank { "Agent" }}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            ShadowCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("✨ Your details")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Marigold,
                            unfocusedBorderColor = Marigold.copy(alpha = 0.75f)
                        )
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Mobile") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        trailingIcon = { Icon(Icons.Default.Lock, contentDescription = "Locked") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = Marigold.copy(alpha = 0.55f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = androidx.compose.ui.graphics.Color(0xFFF7F3EA)
                        )
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Marigold,
                            unfocusedBorderColor = Marigold.copy(alpha = 0.75f)
                        )
                    )
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            PressablePrimaryButton(
                text = "💾 Save profile",
                onClick = {
                    focusManager.clearFocus()
                    FormValidators.name(name)?.let {
                        error = it
                        UiSounds.error(context)
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        return@PressablePrimaryButton
                    }
                    scope.launch {
                        val body = JSONObject().apply {
                            put("name", name.trim())
                            put("address", address.trim())
                        }
                        val result = app.api.updateAgent(app.session.authToken, agentId, body)
                        result.fold(
                            onSuccess = {
                                app.session.updateProfile(name.trim(), phone)
                                focusManager.clearFocus()
                                UiSounds.success(context)
                                Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                val msg = it.message ?: "Could not save profile"
                                error = msg
                                UiSounds.error(context)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            PressableOutlineButton(
                text = "📝 Register a complaint",
                onClick = {
                    UiSounds.click(context)
                    showComplaint = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "💬 Use WhatsApp number for login & support",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "📦 Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
