package zzz.projects.liztzzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import zzz.projects.liztzzz.data.Lizt
import zzz.projects.liztzzz.data.LiztViewModel
import zzz.projects.liztzzz.ui.theme.LiztzzTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val viewModel: LiztViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = Firebase.auth

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
    var selectedLiztUid by remember { mutableStateOf<String?>(null) }

    if (user == null) {
        LoginScreen(auth = auth, onLoginSuccess = {
            user = auth.currentUser
        })
    } else {
        if (selectedLiztUid == null) {
            LiztGridScreen(
                viewModel = viewModel,
                onLiztClick = { selectedLiztUid = it.uid },
                onSignOut = {
                    auth.signOut()
                    user = null
                }
            )
        } else {
            val lizts by viewModel.lizts.collectAsState()
            val selectedLizt = lizts.find { it.uid == selectedLiztUid }
            if (selectedLizt != null) {
                LiztDetailScreen(
                    lizt = selectedLizt,
                    onBack = { selectedLiztUid = null },
                    onToggleChecked = { index, isChecked ->
                        viewModel.updateLiztItemChecked(selectedLizt.uid, index, isChecked)
                    }
                )
            } else {
                selectedLiztUid = null
            }
        }
    }
}

@Composable
fun LiztGridScreen(
    viewModel: LiztViewModel,
    onLiztClick: (Lizt) -> Unit,
    onSignOut: () -> Unit
) {
    val lizts by viewModel.lizts.collectAsState()
    var orderedLizts by remember { mutableStateOf<List<Lizt>>(emptyList()) }
    
    var draggedLizt by remember { mutableStateOf<Lizt?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    val gridState = rememberLazyGridState()
    val currentOrderedLizts by rememberUpdatedState(orderedLizts)

    LaunchedEffect(lizts) {
        if (draggedLizt == null) {
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
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column {
                Button(onClick = onSignOut, modifier = Modifier.padding(8.dp)) {
                    Text("Sign Out")
                }
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(orderedLizts, key = { it.uid }) { lizt ->
                        val isBeingDragged = lizt.uid == draggedLizt?.uid
                        LiztCard(
                            lizt = lizt,
                            isBeingDragged = isBeingDragged,
                            onClick = { onLiztClick(lizt) },
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isBeingDragged) 0f else 1f
                                }
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { _ ->
                                            draggedLizt = lizt
                                            dragOffset = Offset.Zero
                                        },
                                        onDragEnd = {
                                            viewModel.updateOrder(currentOrderedLizts)
                                            draggedLizt = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggedLizt = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            val currentDragged = draggedLizt ?: return@detectDragGesturesAfterLongPress
                                            val list = currentOrderedLizts
                                            val currentDraggedIndex = list.indexOfFirst { it.uid == currentDragged.uid }
                                            if (currentDraggedIndex == -1) return@detectDragGesturesAfterLongPress

                                            val draggedItemInfo = gridState.layoutInfo.visibleItemsInfo.find { it.key == currentDragged.uid } ?: return@detectDragGesturesAfterLongPress
                                            
                                            val ghostCenter = Offset(
                                                x = draggedItemInfo.offset.x + draggedItemInfo.size.width / 2f + dragOffset.x,
                                                y = draggedItemInfo.offset.y + draggedItemInfo.size.height / 2f + dragOffset.y
                                            )

                                            val targetItem = gridState.layoutInfo.visibleItemsInfo.find { item ->
                                                ghostCenter.x in item.offset.x.toFloat()..(item.offset.x.toFloat() + item.size.width) &&
                                                ghostCenter.y in item.offset.y.toFloat()..(item.offset.y.toFloat() + item.size.height)
                                            }

                                            if (targetItem != null && targetItem.key != currentDragged.uid) {
                                                val targetIndex = list.indexOfFirst { it.uid == targetItem.key }
                                                if (targetIndex != -1) {
                                                    val oldOffset = draggedItemInfo.offset
                                                    val targetOffset = targetItem.offset
                                                    
                                                    dragOffset += Offset((oldOffset.x - targetOffset.x).toFloat(), (oldOffset.y - targetOffset.y).toFloat())

                                                    val mutableLizts = list.toMutableList()
                                                    mutableLizts.removeAt(currentDraggedIndex)
                                                    mutableLizts.add(targetIndex, currentDragged)
                                                    orderedLizts = mutableLizts
                                                }
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            draggedLizt?.let { lizt ->
                val itemInfo = gridState.layoutInfo.visibleItemsInfo.find { it.key == lizt.uid }
                if (itemInfo != null) {
                    val density = LocalDensity.current
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (itemInfo.offset.x + dragOffset.x).roundToInt(),
                                    (itemInfo.offset.y + dragOffset.y).roundToInt()
                                )
                            }
                            .width(with(density) { itemInfo.size.width.toDp() })
                            .height(with(density) { itemInfo.size.height.toDp() })
                            .zIndex(1f)
                            .graphicsLayer {
                                alpha = 0.7f
                                scaleX = 1.1f
                                scaleY = 1.1f
                                shadowElevation = 12.dp.toPx()
                            }
                    ) {
                        LiztCard(lizt = lizt, isBeingDragged = false, onClick = {})
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiztDetailScreen(
    lizt: Lizt,
    onBack: () -> Unit,
    onToggleChecked: (Int, Boolean) -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lizt.liztName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Linke Hälfte: liztUnchecked
            Column(modifier = Modifier.weight(1f)) {
                val itemsWithOriginalIndex = lizt.liztUnchecked.mapIndexed { index, item -> index to item }
                val sortedUnchecked = itemsWithOriginalIndex.sortedBy { it.second.isChecked }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sortedUnchecked, key = { it.second.itemName }) { (originalIndex, item) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .clickable { onToggleChecked(originalIndex, !item.isChecked) }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { onToggleChecked(originalIndex, it) }
                            )
                            Text(item.itemName)
                        }
                    }
                }
            }

            VerticalDivider()

            // Rechte Hälfte: liztSuggested
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Vorschläge",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(lizt.liztSuggested, key = { it.itemName }) { item ->
                        Text(
                            item.itemName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiztCard(
    lizt: Lizt,
    isBeingDragged: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LiztzzTheme {
        AddLiztDialog(onDismiss = {}, onAdd = {})
    }
}
