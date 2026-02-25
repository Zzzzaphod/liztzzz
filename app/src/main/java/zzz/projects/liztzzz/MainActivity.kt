package zzz.projects.liztzzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import zzz.projects.liztzzz.data.Lizt
import zzz.projects.liztzzz.data.LiztViewModel
import zzz.projects.liztzzz.ui.theme.LiztzzTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LiztViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        enableEdgeToEdge()
        setContent {
            LiztzzTheme {
                MainScreen(auth, viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(auth: FirebaseAuth, viewModel: LiztViewModel) {
    var user by remember { mutableStateOf(auth.currentUser) }

    if (user == null) {
        LoginScreen(auth = auth, onLoginSuccess = {
            user = auth.currentUser
        })
    } else {
        LiztGridScreen(viewModel = viewModel) {
            auth.signOut()
            user = null
        }
    }
}

@Composable
fun LiztGridScreen(viewModel: LiztViewModel, onSignOut: () -> Unit) {
    val lizts by viewModel.lizts.collectAsState()
    var orderedLizts by remember { mutableStateOf<List<Lizt>>(emptyList()) }
    var draggedLizt by remember { mutableStateOf<Lizt?>(null) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(lizts) {
        if (draggedLizt == null) { // Only update if not currently dragging
            orderedLizts = lizts
        }
    }

    var showAddLiztDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Button(onClick = { showAddLiztDialog = true }) {
                Text("Add Lizt")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Button(onClick = onSignOut) {
                Text("Sign Out")
            }
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding
            ) {
                items(orderedLizts, key = { it.uid }) { lizt ->
                    LiztCard(
                        lizt = lizt,
                        isBeingDragged = lizt.uid == draggedLizt?.uid,
                        modifier = Modifier.pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggedLizt = lizt },
                                onDragEnd = {
                                    if (orderedLizts != lizts) { // Check if order actually changed
                                        viewModel.updateOrder(orderedLizts)
                                    }
                                    draggedLizt = null
                                },
                                onDragCancel = { draggedLizt = null },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val currentDragged = draggedLizt ?: return@detectDragGesturesAfterLongPress
                                    val currentDraggedIndex = orderedLizts.indexOf(currentDragged)

                                    val targetItem = gridState.layoutInfo.visibleItemsInfo.find { item ->
                                        change.position.x in item.offset.x.toFloat()..(item.offset.x.toFloat() + item.size.width) &&
                                                change.position.y in item.offset.y.toFloat()..(item.offset.y.toFloat() + item.size.height)
                                    }

                                    if (targetItem != null && targetItem.index != currentDraggedIndex) {
                                        val from = currentDraggedIndex
                                        val to = targetItem.index
                                        orderedLizts = orderedLizts.toMutableList().apply {
                                            add(to, removeAt(from))
                                        }
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showAddLiztDialog) {
        AddLiztDialog(
            onDismiss = { showAddLiztDialog = false },
            onAdd = {
                viewModel.addLizt(it.liztName, it.hasSuggests)
                showAddLiztDialog = false
            }
        )
    }
}

@Composable
fun LiztCard(lizt: Lizt, isBeingDragged: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (isBeingDragged) 0.8f else 1f
                shadowElevation = if (isBeingDragged) 8f else 0f
                scaleX = if (isBeingDragged) 1.05f else 1f
                scaleY = if (isBeingDragged) 1.05f else 1f
            }
    ) {
        Text(text = lizt.liztName, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun AddLiztDialog(onDismiss: () -> Unit, onAdd: (Lizt) -> Unit) {
    var liztName by remember { mutableStateOf("") }
    var hasSuggests by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                OutlinedTextField(
                    value = liztName,
                    onValueChange = { liztName = it },
                    label = { Text("Lizt Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = hasSuggests, onCheckedChange = { hasSuggests = it })
                    Text("mit Vorschlägen")
                }
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onAdd(Lizt(liztName = liztName, hasSuggests = hasSuggests)) }) {
                        Text("Add")
                    }
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LiztzzTheme {
        AddLiztDialog(onDismiss = {}, onAdd = {})
    }
}