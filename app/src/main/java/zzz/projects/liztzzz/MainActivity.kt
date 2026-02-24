package zzz.projects.liztzzz

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import zzz.projects.liztzzz.data.Lizt
import zzz.projects.liztzzz.data.LiztItem
import zzz.projects.liztzzz.ui.theme.LiztzzTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            LiztzzTheme {
                MainScreen(auth)
            }
        }
    }
}

@Composable
fun MainScreen(auth: FirebaseAuth) {
    var user by remember { mutableStateOf(auth.currentUser) }

    if (user == null) {
        LoginScreen(auth = auth, onLoginSuccess = {
            user = auth.currentUser
        })
    } else {
        // User is logged in, show the main content
        var statusMessage by remember { mutableStateOf("Writing to Realtime Database...") }

        LaunchedEffect(user) {
            // Access a Realtime Database instance
            val database = Firebase.database
            val myRef = database.getReference("liztz")

            // Create a sample Lizt object
            val sampleLizt = Lizt(
                hasSuggests = true,
                isDeletable = true,
                liztName = "My Sample Lizt",
                liztSuggested = listOf(LiztItem(itemName = "Suggested Item 1")),
                liztUnchecked = listOf(LiztItem(itemName = "Unchecked Item 1")),
                liztChecked = listOf(LiztItem(itemName = "Checked Item 1", isChecked = true))
            )

            // Add a new document with a generated ID
            myRef.push().setValue(sampleLizt).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val successMsg = "Data saved successfully."
                    Log.d("MainActivity", successMsg)
                    statusMessage = successMsg
                } else {
                    val e = task.exception
                    val errorMsg = "Error writing to Realtime Database"
                    Log.w("MainActivity", errorMsg, e)
                    statusMessage = "$errorMsg: ${e?.localizedMessage}"
                }
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Greeting(name = "Welcome ${user?.email}")
                Text(text = statusMessage)
                Button(onClick = {
                    auth.signOut()
                    user = null
                }) {
                    Text("Sign Out")
                }
            }
        }
    }
}

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
                label = { Text("Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
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
            }) {
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
            }) {
                Text("Sign Up")
            }
            Text(text = statusMessage)
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LiztzzTheme {
        Greeting("Android")
    }
}
