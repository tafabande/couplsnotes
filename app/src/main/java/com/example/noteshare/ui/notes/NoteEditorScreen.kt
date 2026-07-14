package com.example.noteshare.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.noteshare.data.model.Tag
import com.example.noteshare.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val isNewNote = noteId == "new"
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var displayStyle by remember { mutableStateOf(Constants.DISPLAY_PLAIN) }
    var isVault by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showStylePicker by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.noteEvent.collect { event ->
            when (event) {
                is NoteEvent.NoteSaved -> onNavigateBack()
                is NoteEvent.Error -> { /* show snackbar */ }
            }
        }
    }

    // Debounced Auto-save for existing notes
    LaunchedEffect(title, content, selectedTags, displayStyle, isVault) {
        if (!isNewNote && title.isNotBlank() && content.isNotBlank()) {
            kotlinx.coroutines.delay(1500L) // 1.5 second debounce
            viewModel.updateNote(
                noteId = noteId,
                title = title,
                content = content,
                tags = selectedTags.toList(),
                displayStyle = displayStyle,
                isVault = isVault
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save button
                    TextButton(
                        onClick = {
                            attemptedSave = true
                            if (title.isNotBlank() && content.isNotBlank()) {
                                viewModel.createNote(
                                    title = title,
                                    content = content,
                                    tags = selectedTags.toList(),
                                    displayStyle = displayStyle,
                                    isVault = isVault
                                )
                            }
                        }
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                .verticalScroll(rememberScrollState())
        ) {
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; if (it.isNotBlank()) attemptedSave = false },
                placeholder = { Text("Title *", style = MaterialTheme.typography.headlineSmall) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.background
                ),
                isError = attemptedSave && title.isBlank(),
                supportingText = if (attemptedSave && title.isBlank()) { { Text("Title is required") } } else null,
                singleLine = true
            )

            // Content field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it; if (it.isNotBlank()) attemptedSave = false },
                placeholder = { Text("Start writing... *") },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.background
                ),
                isError = attemptedSave && content.isBlank(),
                supportingText = if (attemptedSave && content.isBlank()) { { Text("Note content cannot be empty") } } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════
            // Vault Security Section
            // ═══════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Vault Note",
                        modifier = Modifier.size(20.dp),
                        tint = if (isVault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Vault Note",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Require biometric authentication to view",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isVault,
                    onCheckedChange = { isVault = it }
                )
            }

            androidx.compose.material3.Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // ═══════════════════════════════════
            // Tags Section
            // ═══════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Label,
                    contentDescription = "Tags",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Tag.DEFAULTS) { tag ->
                    FilterChip(
                        selected = selectedTags.contains(tag.name),
                        onClick = {
                            selectedTags = if (selectedTags.contains(tag.name))
                                selectedTags - tag.name
                            else
                                selectedTags + tag.name
                        },
                        label = { Text("${tag.emoji} ${tag.name}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // ═══════════════════════════════════
            // Display Style Section
            // ═══════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Style,
                    contentDescription = "Style",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Display Style",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StyleOption(
                    label = "Plain",
                    emoji = "📄",
                    selected = displayStyle == Constants.DISPLAY_PLAIN,
                    onClick = { displayStyle = Constants.DISPLAY_PLAIN },
                    modifier = Modifier.weight(1f)
                )
                StyleOption(
                    label = "Big Picture",
                    emoji = "🖼️",
                    selected = displayStyle == Constants.DISPLAY_BIG_PICTURE,
                    onClick = { displayStyle = Constants.DISPLAY_BIG_PICTURE },
                    modifier = Modifier.weight(1f)
                )
                StyleOption(
                    label = "Framed",
                    emoji = "🪟",
                    selected = displayStyle == Constants.DISPLAY_FRAMED,
                    onClick = { displayStyle = Constants.DISPLAY_FRAMED },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyleOption(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (selected)
            CardDefaults.outlinedCardBorder()
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
