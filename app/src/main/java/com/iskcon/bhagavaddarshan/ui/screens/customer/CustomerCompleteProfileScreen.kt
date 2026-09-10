package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.IndianStatesAndUts
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCompleteProfileScreen(
    app: BhagavadDarshanApp,
    onComplete: () -> Unit
) {
    val api = remember { BdApi() }
    val scope = rememberCoroutineScope()
    val token = app.session.authToken

    var name by remember { mutableStateOf(app.session.agentName) }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var villageTown by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Andhra Pradesh") }
    var stateMenuOpen by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = EditCream,
        topBar = {
            CompactTopBar(
                title = {
                    Text("Complete your profile", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(EditCream)
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Your agent added you to Bhagavad Darshan. Please fill in the remaining details to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = houseNo,
                onValueChange = { houseNo = it },
                label = { Text("House / Flat") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Street / Area") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = villageTown,
                onValueChange = { villageTown = it },
                label = { Text("City / Village*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            OutlinedTextField(
                value = pincode,
                onValueChange = { pincode = it.filter(Char::isDigit).take(6) },
                label = { Text("Pincode*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customerFieldColors()
            )
            Text("State*", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, color = UxInk)
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, UxGold200, RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f))
                        .clickable { stateMenuOpen = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(state, fontFamily = Montserrat, color = UxInk)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EditMuted)
                }
                DropdownMenu(expanded = stateMenuOpen, onDismissRequest = { stateMenuOpen = false }) {
                    IndianStatesAndUts.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontFamily = Montserrat) },
                            onClick = {
                                state = option
                                stateMenuOpen = false
                            }
                        )
                    }
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            RadiantGoldButton(
                text = if (loading) "Saving…" else "Save & continue",
                onClick = {
                    if (name.trim().length < 2) {
                        error = "Enter full name"
                        return@RadiantGoldButton
                    }
                    if (villageTown.isBlank()) {
                        error = "City / village is required"
                        return@RadiantGoldButton
                    }
                    if (pincode.length != 6) {
                        error = "Enter 6-digit pincode"
                        return@RadiantGoldButton
                    }
                    loading = true
                    scope.launch {
                        val fields = JSONObject()
                            .put("name", name.trim())
                            .put("houseNo", houseNo.trim())
                            .put("street", street.trim())
                            .put("villageTown", villageTown.trim())
                            .put("district", "")
                            .put("pincode", pincode)
                            .put("state", state.trim())
                        val result = api.updateProfile(token, fields)
                        loading = false
                        result.onSuccess {
                            app.session.agentName = name.trim()
                            onComplete()
                        }.onFailure { error = friendlyError(it) }
                    }
                },
                enabled = !loading
            )
        }
    }
}
