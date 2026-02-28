package zzz.projects.liztzzz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    statusMessage = "Signing in..."
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                statusMessage = "Sign in successful!"
                                onLoginSuccess()
                            } else {
                                statusMessage = "Authentication failed: ${task.exception?.localizedMessage}"
                            }
                        }
                } else {
                    statusMessage = "Please enter email and password."
                }
            }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Sign In")
            }
            Button(onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    statusMessage = "Creating account..."
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                statusMessage = "Account created successfully! Please sign in."
                            } else {
                                statusMessage = "Registration failed: ${task.exception?.localizedMessage}"
                            }
                        }
                } else {
                    statusMessage = "Please enter email and password."
                }
            }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Sign Up")
            }
            Text(text = statusMessage, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
