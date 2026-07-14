package com.example.noteshare.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var displayName by remember { mutableStateOf(uiState.user?.displayName ?: "") }
    var age by remember { mutableStateOf(uiState.user?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(uiState.user?.gender ?: "") }
    var favoriteColor by remember { mutableStateOf(uiState.user?.favoriteColor ?: "") }

    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            displayName = user.displayName
            age = user.age?.toString() ?: ""
            gender = user.gender ?: ""
            favoriteColor = user.favoriteColor ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateProfile(mapOf(
                            "displayName" to displayName,
                            "age" to age.toIntOrNull(),
                            "gender" to gender.ifBlank { null },
                            "favoriteColor" to favoriteColor.ifBlank { null }
                        ))
                        onNavigateBack()
                    }) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            OutlinedTextField(
                value = favoriteColor,
                onValueChange = { favoriteColor = it },
                label = { Text("Favorite Color") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Email: ${uiState.user?.email ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
