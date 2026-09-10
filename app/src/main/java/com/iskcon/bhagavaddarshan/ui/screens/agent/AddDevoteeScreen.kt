package com.iskcon.bhagavaddarshan.ui.screens.agent

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.ui.components.AgentHeaderTitle
import com.iskcon.bhagavaddarshan.ui.components.GradientTopBar
import com.iskcon.bhagavaddarshan.ui.components.IndianStatesAndUts
import com.iskcon.bhagavaddarshan.ui.components.PressablePrimaryButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.AgentHeroBrush
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.util.FormValidators
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private enum class AddDevoteeStep { FORM, SUCCESS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDevoteeScreen(
    app: BhagavadDarshanApp,
    devoteeId: Long? = null,
    onBack: () -> Unit,
    onBackToHome: () -> Unit
) {
    val isEdit = devoteeId != null && devoteeId > 0L
    var step by remember { mutableStateOf(AddDevoteeStep.FORM) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var villageTown by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("Andhra Pradesh") }
    var stateMenuOpen by remember { mutableStateOf(false) }
    var addressOpen by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var loadingExisting by remember { mutableStateOf(isEdit) }
    var error by remember { mutableStateOf<String?>(null) }
    var savedName by remember { mutableStateOf("") }
    var whatsAppNote by remember { mutableStateOf("") }
    var originalPhone by remember { mutableStateOf("") }
    var pendingPhoneConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun performSave() {
        loading = true
        scope.launch {
            val body = JSONObject()
                .put("name", name.trim())
                .put("phone", phone)
                .put("houseNo", houseNo.trim())
                .put("street", street.trim())
                .put("villageTown", villageTown.trim())
                .put("pincode", pincode)
                .put("state", state.trim())
            val result = withContext(Dispatchers.IO) {
                if (isEdit) {
                    app.api.updateDevotee(app.session.authToken, devoteeId!!, body)
                } else {
                    app.api.createDevotee(app.session.authToken, body)
                }
            }
            loading = false
            result.onSuccess { json ->
                UiSounds.chime(context)
                savedName = json.optJSONObject("devotee")?.optString("name") ?: name.trim()
                originalPhone = phone
                if (isEdit) {
                    Toast.makeText(context, "Devotee details saved", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    whatsAppNote = json.optString("whatsAppMessage").ifBlank {
                        if (json.optBoolean("whatsAppSent")) {
                            "WhatsApp notification sent"
                        } else {
                            "WhatsApp notification not enabled or failed"
                        }
                    }
                    Toast.makeText(context, "Devotee saved", Toast.LENGTH_SHORT).show()
                    step = AddDevoteeStep.SUCCESS
                }
            }.onFailure {
                val msg = friendlyError(it)
                error = msg
                UiSounds.error(context)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(devoteeId) {
        if (!isEdit) return@LaunchedEffect
        loadingExisting = true
        val result = withContext(Dispatchers.IO) {
            app.api.getDevotee(app.session.authToken, devoteeId!!)
        }
        loadingExisting = false
        result.onSuccess { json ->
            val d = json.optJSONObject("devotee") ?: return@onSuccess
            name = d.optString("name")
            phone = d.optString("phone")
            originalPhone = phone
            houseNo = d.optString("houseNo")
            street = d.optString("street")
            villageTown = d.optString("villageTown")
            pincode = d.optString("pincode")
            state = d.optString("state").ifBlank { "Andhra Pradesh" }
            addressOpen = listOf(houseNo, street, villageTown, pincode).any { it.isNotBlank() }
        }.onFailure {
            error = friendlyError(it)
            UiSounds.error(context)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    if (pendingPhoneConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingPhoneConfirm = false },
            title = { Text("Update phone number?", fontWeight = FontWeight.Bold) },
            text = { Text("Change mobile from $originalPhone to $phone?") },
            dismissButton = {
                com.iskcon.bhagavaddarshan.ui.components.PressableOutlineButton(
                    text = "Cancel",
                    onClick = {
                        UiSounds.click(context)
                        pendingPhoneConfirm = false
                    }
                )
            },
            confirmButton = {
                PressablePrimaryButton(
                    text = "Update",
                    onClick = {
                        UiSounds.click(context)
                        pendingPhoneConfirm = false
                        performSave()
                    }
                )
            }
        )
    }

    Scaffold(
        containerColor = EditCream,
        topBar = {
            if (step == AddDevoteeStep.FORM) {
                GradientTopBar(
                    brush = AgentHeroBrush,
                    title = {
                        AgentHeaderTitle(
                            title = if (isEdit) "✏️ Edit devotee" else "➕ Add devotee"
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TempleGreen
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (loadingExisting) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            transitionSpec = {
                (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(280)))
                    .togetherWith(slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(220)))
            },
            label = "addDevoteeStep"
        ) { current ->
            when (current) {
                AddDevoteeStep.FORM -> AddDevoteeForm(
                    isEdit = isEdit,
                    name = name,
                    onNameChange = { name = it; error = null },
                    phone = phone,
                    onPhoneChange = { phone = it.filter(Char::isDigit).take(10); error = null },
                    houseNo = houseNo,
                    onHouseNoChange = { houseNo = it },
                    street = street,
                    onStreetChange = { street = it },
                    villageTown = villageTown,
                    onVillageTownChange = { villageTown = it },
                    pincode = pincode,
                    onPincodeChange = { pincode = it.filter(Char::isDigit).take(6) },
                    state = state,
                    stateMenuOpen = stateMenuOpen,
                    onStateMenuOpen = { stateMenuOpen = it },
                    onStateSelect = { state = it; stateMenuOpen = false },
                    addressOpen = addressOpen,
                    onAddressOpenChange = {
                        UiSounds.click(context)
                        addressOpen = it
                    },
                    error = error,
                    loading = loading,
                    onSave = {
                        FormValidators.phone(phone)?.let {
                            error = it
                            UiSounds.error(context)
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            return@AddDevoteeForm
                        }
                        if (name.trim().length < 2) {
                            error = "Enter devotee name"
                            UiSounds.error(context)
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            return@AddDevoteeForm
                        }
                        if (pincode.isNotBlank() && pincode.length != 6) {
                            error = "Enter 6-digit pincode"
                            UiSounds.error(context)
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            return@AddDevoteeForm
                        }
                        // Phone change confirmation when editing
                        if (isEdit && originalPhone.isNotBlank() && phone != originalPhone) {
                            pendingPhoneConfirm = true
                            return@AddDevoteeForm
                        }
                        performSave()
                    }
                )
                AddDevoteeStep.SUCCESS -> DevoteeSavedSuccess(
                    name = savedName,
                    subtitle = whatsAppNote,
                    onBackToHome = {
                        UiSounds.click(context)
                        onBackToHome()
                    },
                    onAddAnother = {
                        UiSounds.click(context)
                        name = ""
                        phone = ""
                        houseNo = ""
                        street = ""
                        villageTown = ""
                        pincode = ""
                        state = "Andhra Pradesh"
                        addressOpen = false
                        error = null
                        step = AddDevoteeStep.FORM
                    }
                )
            }
        }
    }
}

@Composable
private fun AddDevoteeForm(
    isEdit: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    houseNo: String,
    onHouseNoChange: (String) -> Unit,
    street: String,
    onStreetChange: (String) -> Unit,
    villageTown: String,
    onVillageTownChange: (String) -> Unit,
    pincode: String,
    onPincodeChange: (String) -> Unit,
    state: String,
    stateMenuOpen: Boolean,
    onStateMenuOpen: (Boolean) -> Unit,
    onStateSelect: (String) -> Unit,
    addressOpen: Boolean,
    onAddressOpenChange: (Boolean) -> Unit,
    error: String?,
    loading: Boolean,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            if (isEdit) {
                "✏️ Update devotee details. Address is optional."
            } else {
                "🙏 Name and mobile are required. Address is optional."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full name*") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = customerFieldColors()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Mobile number*") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = customerFieldColors()
        )

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, UxGold200, RoundedCornerShape(12.dp))
                .clickable { onAddressOpenChange(!addressOpen) }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🏠 Address (optional)",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                color = UxInk
            )
            Icon(
                if (addressOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = EditMuted
            )
        }

        AnimatedVisibility(
            visible = addressOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = houseNo,
                    onValueChange = onHouseNoChange,
                    label = { Text("House / Flat") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = customerFieldColors()
                )
                OutlinedTextField(
                    value = street,
                    onValueChange = onStreetChange,
                    label = { Text("Street / Area") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = customerFieldColors()
                )
                OutlinedTextField(
                    value = villageTown,
                    onValueChange = onVillageTownChange,
                    label = { Text("City / Village") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = customerFieldColors()
                )
                OutlinedTextField(
                    value = pincode,
                    onValueChange = onPincodeChange,
                    label = { Text("Pincode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = customerFieldColors()
                )
                Text("State", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, color = UxInk)
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, UxGold200, RoundedCornerShape(12.dp))
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f))
                            .clickable {
                                UiSounds.click(context)
                                onStateMenuOpen(true)
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state, fontFamily = Montserrat, color = UxInk)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EditMuted)
                    }
                    DropdownMenu(expanded = stateMenuOpen, onDismissRequest = { onStateMenuOpen(false) }) {
                        IndianStatesAndUts.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontFamily = Montserrat) },
                                onClick = {
                                    UiSounds.click(context)
                                    onStateSelect(option)
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically()) {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        PressablePrimaryButton(
            text = when {
                loading -> "⏳ Saving…"
                isEdit -> "💾 Save changes"
                else -> "💾 Save devotee"
            },
            onClick = onSave,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DevoteeSavedSuccess(
    name: String,
    subtitle: String,
    onBackToHome: () -> Unit,
    onAddAnother: () -> Unit
) {
    val scale = remember { Animatable(0.2f) }
    val checkAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.6f) }
    val ringAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // Tick grows in gradually
        launch {
            scale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            checkAlpha.animateTo(1f, tween(900, delayMillis = 180))
        }
        // Expanding ring after tick settles
        launch {
            kotlinx.coroutines.delay(650)
            ringAlpha.animateTo(0.55f, tween(200))
            ringScale.animateTo(1.55f, tween(1100))
            ringAlpha.animateTo(0f, tween(500))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Box(
                Modifier
                    .size(112.dp)
                    .graphicsLayer(
                        scaleX = ringScale.value,
                        scaleY = ringScale.value,
                        alpha = ringAlpha.value
                    )
                    .clip(CircleShape)
                    .background(Leaf.copy(alpha = 0.25f))
            )
            Box(
                Modifier
                    .size(112.dp)
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        spotColor = Leaf.copy(alpha = 0.45f)
                    )
                    .clip(CircleShape)
                    .background(LeafContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Leaf)
                        .graphicsLayer(alpha = checkAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "🙏 Hare Krishna",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Leaf,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "✅ Devotee saved",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        PressablePrimaryButton(
            text = "Back to Home",
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        )
        Spacer(Modifier.height(12.dp))
        PressablePrimaryButton(
            text = "Add another",
            onClick = onAddAnother,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            containerColor = Leaf,
            pressedColor = Color(0xFF1B5E20)
        )
    }
}
