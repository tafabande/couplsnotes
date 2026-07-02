package com.example.noteshare

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteshare.config.AppEnvironment
import com.example.noteshare.config.EnvironmentConfig
import com.example.noteshare.model.Note
import com.example.noteshare.repository.NoteRepository

class MainActivity : ComponentActivity() {

    private val repository = NoteRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteShareScreen(repository = repository) { message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteShareScreen(
    repository: NoteRepository,
    showToast: (String) -> Unit
) {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var selectedEnv by remember { mutableStateOf(EnvironmentConfig.current) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshNotes() {
        isLoading = true
        repository.getNotes(
            onSuccess = { fetchedNotes ->
                notes = fetchedNotes
                isLoading = false
            },
            onFailure = { error ->
                showToast("Failed to fetch notes: ${error.message}")
                isLoading = false
            }
        )
    }

    LaunchedEffect(selectedEnv) {
        EnvironmentConfig.current = selectedEnv
        refreshNotes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NoteShare App",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Environment Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(text = "Env: ", fontWeight = FontWeight.Bold)
            AppEnvironment.values().forEach { env ->
                RadioButton(
                    selected = (selectedEnv == env),
                    onClick = { selectedEnv = env }
                )
                Text(
                    text = env.name.substring(0, 3),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Add Note Form
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                if (title.isBlank() || content.isBlank() || author.isBlank()) {
                    showToast("Please fill in all fields")
                    return@Button
                }
                val newNote = Note(
                    title = title,
                    content = content,
                    author = author
                )
                repository.addNote(
                    note = newNote,
                    onSuccess = {
                        showToast("Note added successfully")
                        title = ""
                        content = ""
                        author = ""
                        refreshNotes()
                    },
                    onFailure = { error ->
                        showToast("Error adding note: ${error.message}")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Add Note")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes) { note ->
                    NoteItem(note = note, onDelete = {
                        repository.deleteNote(
                            noteId = note.id,
                            onSuccess = {
                                showToast("Note deleted")
                                refreshNotes()
                            },
                            onFailure = { error ->
                                showToast("Error deleting note: ${error.message}")
                            }
                        )
                    })
                }
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = note.content, modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "By: ${note.author}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete note",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
