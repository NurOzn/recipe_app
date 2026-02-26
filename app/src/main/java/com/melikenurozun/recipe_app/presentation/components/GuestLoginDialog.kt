package com.melikenurozun.recipe_app.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun GuestLoginDialog(
    onDismissRequest: () -> Unit,
    onLoginClick: (() -> Unit)? = null,
    title: String = "Uh-oh, you’re not logged in.",
    text: String = "You need to be logged in to use this feature."
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            if (onLoginClick != null) {
                Button(onClick = onLoginClick) {
                    Text("Sign Up / Login")
                }
            } else {
                TextButton(onClick = onDismissRequest) {
                    Text("OK, Got it")
                }
            }
        },
        dismissButton = if (onLoginClick != null) {
            {
                TextButton(onClick = onDismissRequest) { 
                    Text("Keep looking") 
                }
            }
        } else null
    )
}
