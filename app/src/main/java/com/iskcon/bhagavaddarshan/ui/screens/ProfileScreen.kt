package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.PasswordHasher
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    app: BhagavadDarshanApp,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val agentId = app.session.agentId

    LaunchedEffect(agentId) {
        val a = withContext(Dispatchers.IO) { app.database.agentDao().getById(agentId) }
        if (a != null) {
            name = a.name
            phone = a.phone
            address = a.address
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = { Text("My profile", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null; saved = false },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter(Char::isDigit).take(10)
                    error = null
                    saved = false
                },
                label = { Text("Mobile") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; saved = false },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null; saved = false },
                label = { Text("New password (optional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (saved) Text("Saved", color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = {
                    FormValidators.name(name)?.let { error = it; return@Button }
                    FormValidators.phone(phone)?.let { error = it; return@Button }
                    if (password.isNotBlank()) {
                        FormValidators.password(password)?.let { error = it; return@Button }
                    }
                    scope.launch {
                        val err = withContext(Dispatchers.IO) {
                            val dao = app.database.agentDao()
                            val cur = dao.getById(agentId) ?: return@withContext "Profile not found"
                            val other = dao.findByPhone(phone)
                            if (other != null && other.id != cur.id) {
                                return@withContext "Mobile already used"
                            }
                            dao.update(
                                cur.copy(
                                    name = name.trim(),
                                    phone = phone,
                                    address = address.trim(),
                                    passwordHash = if (password.isBlank()) cur.passwordHash
                                    else PasswordHasher.hash(password)
                                )
                            )
                            app.session.updateProfile(name.trim(), phone)
                            null
                        }
                        if (err != null) error = err else {
                            saved = true
                            password = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save profile")
            }
        }
    }
}
