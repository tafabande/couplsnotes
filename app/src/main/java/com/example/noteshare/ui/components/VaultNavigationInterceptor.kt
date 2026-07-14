package com.example.noteshare.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.noteshare.data.model.Note
import com.example.noteshare.util.BiometricHelper

@Composable
fun rememberVaultNavigationInterceptor(
    onNavigateToDetail: (String) -> Unit
): (Note) -> Unit {
    val context = LocalContext.current as? FragmentActivity
    return { note ->
        if (note.isVault) {
            if (context != null) {
                BiometricHelper.showBiometricPrompt(
                    activity = context,
                    onSuccess = {
                        onNavigateToDetail(note.id)
                    },
                    onError = {
                        // In a real app we might show a snackbar on error
                    }
                )
            } else {
                // Fallback if not a FragmentActivity
                onNavigateToDetail(note.id)
            }
        } else {
            onNavigateToDetail(note.id)
        }
    }
}
