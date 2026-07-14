package com.example.noteshare.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
    var showOptionsMenu by remember { mutableStateOf(false) }
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

    if (showTagPicker) {
        AlertDialog(
            onDismissRequest = { showTagPicker = false },
            title = { Text("Select Tags") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Tag.DEFAULTS) { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag.name),
                            onClick = {
                                selectedTags = if (selectedTags.contains(tag.name))
                                    selectedTags - tag.name
                                else
                                    selectedTags + tag.name
                            },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(tag.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(tag.name)
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagPicker = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showStylePicker) {
        AlertDialog(
            onDismissRequest = { showStylePicker = false },
            title = { Text("Display Style") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyleOption(
                        label = "Plain",
                        icon = Icons.Default.Description,
                        selected = displayStyle == Constants.DISPLAY_PLAIN,
                        onClick = { displayStyle = Constants.DISPLAY_PLAIN; showStylePicker = false }
                    )
                    StyleOption(
                        label = "Big Picture",
                        icon = Icons.Default.Image,
                        selected = displayStyle == Constants.DISPLAY_BIG_PICTURE,
                        onClick = { displayStyle = Constants.DISPLAY_BIG_PICTURE; showStylePicker = false }
                    )
                    StyleOption(
                        label = "Framed",
                        icon = Icons.Default.CropPortrait,
                        selected = displayStyle == Constants.DISPLAY_FRAMED,
                        onClick = { displayStyle = Constants.DISPLAY_FRAMED; showStylePicker = false }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showStylePicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (isNewNote) "New note" else "Edit note", fontWeight = FontWeight.SemiBold)
                        Text("Keep it simple and save quickly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Vault Note") },
                                trailingIcon = {
                                    Switch(
                                        checked = isVault,
                                        onCheckedChange = { isVault = it },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                },
                                onClick = { isVault = !isVault }
                            )
                            DropdownMenuItem(
                                text = { Text("Tags") },
                                onClick = {
                                    showOptionsMenu = false
                                    showTagPicker = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Display Style") },
                                onClick = {
                                    showOptionsMenu = false
                                    showStylePicker = true
                                }
                            )
                        }
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

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyleOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
