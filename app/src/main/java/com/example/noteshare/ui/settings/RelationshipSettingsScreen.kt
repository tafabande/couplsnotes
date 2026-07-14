package com.example.noteshare.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.noteshare.ui.pairing.PairingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipSettingsScreen(
    onNavigateBack: () -> Unit,
    pairingViewModel: PairingViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val pairingState by pairingViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }
    var showApprovalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pairingState.error) {
        pairingState.error?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(settingsState.error) {
        settingsState.error?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(pairingState.pair) {
        val pendingJoinUserId = pairingState.pair?.pendingJoinUserId
        if (pendingJoinUserId != null) {
            showApprovalDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Relationship", fontWeight = FontWeight.SemiBold)
                        Text("Link, manage, or unlink one partner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (pairingState.pair?.isActive == true) "Linked" else "Not linked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when {
                            pairingState.pair?.pendingJoinUserId != null -> "Awaiting approval"
                            pairingState.pair?.isDisconnecting == true -> "Unlink countdown active"
                            else -> "Connect one partner at a time"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (pairingState.pair?.pendingJoinUserId != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Join request", fontWeight = FontWeight.SemiBold)
                        Text(pairingState.pair?.pendingJoinUserName ?: "Someone wants to connect")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { pairingViewModel.approveJoinRequest(pairingState.pair!!.pendingJoinUserId!!) }) {
                                Text("Approve")
                            }
                            OutlinedButton(onClick = { pairingViewModel.rejectJoinRequest() }) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }

            if (pairingState.pair == null || pairingState.pair?.isDissolved == true) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Create pairing code", fontWeight = FontWeight.SemiBold)
                        OutlinedButton(
                            onClick = { pairingViewModel.createInvite() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate 6-digit key")
                        }
                        if (pairingState.inviteCode != null) {
                            Text("Code: ${pairingState.inviteCode}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (pairingState.pair?.isActive == true) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Unlink and privacy", fontWeight = FontWeight.SemiBold)
                        Text("You can unlink and keep access for 15 days, or request a wipe to remove the pair sooner.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showUnlinkDialog = true }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unlink")
                            }
                            Button(onClick = { showWipeDialog = true }) {
                                Icon(Icons.Default.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wipe now")
                            }
                        }
                    }
                }
            } else if (pairingState.pair?.isDisconnecting == true) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Unlink pending", fontWeight = FontWeight.SemiBold)
                        Text("Access stays available for 15 days unless one partner wipes the pair.")
                        OutlinedButton(onClick = { pairingViewModel.cancelDisconnect() }) {
                            Text("Keep linked")
                        }
                    }
                }
            }

            if (pairingState.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Updating relationship...")
                }
            }

            if (pairingState.pair != null && pairingState.pair?.isActive != true) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter partner key", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase().take(6) },
                            label = { Text("6-digit key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = { pairingViewModel.joinWithCode(code) },
                            enabled = code.length == 6,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Request link")
                        }
                    }
                }
            }
        }
    }

    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    pairingViewModel.requestDisconnect()
                    showUnlinkDialog = false
                }) { Text("Unlink") }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) { Text("Cancel") }
            },
            title = { Text("Unlink pair") },
            text = { Text("Access remains for 15 days. Either partner can still wipe the pair sooner.") }
        )
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    pairingViewModel.requestWipe()
                    pairingViewModel.confirmWipe()
                    showWipeDialog = false
                }) { Text("Wipe") }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) { Text("Cancel") }
            },
            title = { Text("Wipe pair") },
            text = { Text("This removes the pair quickly and revokes access for both partners.") }
        )
    }

    if (showApprovalDialog) {
        AlertDialog(
            onDismissRequest = { showApprovalDialog = false },
            confirmButton = {
                TextButton(onClick = { showApprovalDialog = false }) { Text("OK") }
            },
            title = { Text("Pending request") },
            text = { Text("A partner wants to connect. Approve or reject from this screen.") }
        )
    }
}
