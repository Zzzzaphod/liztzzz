package zzz.projects.liztzzz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import zzz.projects.liztzzz.data.Lizt
import zzz.projects.liztzzz.data.LiztItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LiztDetailScreen(
    lizt: Lizt,
    onBack: () -> Unit,
    onToggleChecked: (Int, Boolean) -> Unit,
    onAddItem: (String) -> Unit,
    onDeleteChecked: () -> Unit,
    onToggleSuggests: () -> Unit,
    onSuggestedClick: (LiztItem) -> Unit,
    onSuggestedLongClick: (LiztItem) -> Unit,
    onUncheckedLongClick: (LiztItem) -> Unit,
    onRenameLizt: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onDeleteLizt: () -> Unit
) {
    BackHandler(onBack = onBack)
    var newItemName by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    
    var itemToDelete by remember { mutableStateOf<LiztItem?>(null) }
    var itemToSuggest by remember { mutableStateOf<LiztItem?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showDeleteLiztDialog by remember { mutableStateOf(false) }

    val titleColor = remember(lizt.color) {
        try {
            Color(android.graphics.Color.parseColor(lizt.color))
        } catch (e: Exception) {
            Color.White
        }
    }

    Scaffold(
        containerColor = Color.White, // Force light background for visibility
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = titleColor),
                title = { Text(lizt.liztName, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Einstellungen", tint = Color.Black)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Abgehakte löschen") },
                                onClick = {
                                    onDeleteChecked()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Vorschläge ein/aus") },
                                onClick = {
                                    onToggleSuggests()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Listennamen ändern") },
                                onClick = {
                                    showRenameDialog = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Farbe...") },
                                onClick = {
                                    showColorPicker = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Löschen", color = Color.Red) },
                                onClick = {
                                    showDeleteLiztDialog = true
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Neues Element...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.6f),
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                onAddItem(newItemName)
                                newItemName = ""
                            }
                        },
                        enabled = newItemName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = if (newItemName.isNotBlank()) Color.Black else Color.Gray)
                    }
                }
            }
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
                                .combinedClickable(
                                    onClick = { onToggleChecked(originalIndex, !item.isChecked) },
                                    onLongClick = { itemToSuggest = item }
                                )
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { onToggleChecked(originalIndex, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.Black,
                                    uncheckedColor = Color.Black,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(item.itemName, color = Color.Black)
                        }
                    }
                }
            }

            if (lizt.hasSuggests) {
                VerticalDivider(color = Color.Black.copy(alpha = 0.2f))

                // Rechte Hälfte: liztSuggested
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Vorschläge",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        val sortedSuggested = lizt.liztSuggested.sortedBy { it.itemName }
                        items(sortedSuggested, key = { it.itemName }) { item ->
                            Text(
                                item.itemName,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onSuggestedClick(item) },
                                        onLongClick = { itemToDelete = item }
                                    )
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Lösch-Bestätigung für Vorschläge
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Vorschlag löschen") },
            text = { Text("Möchten Sie '${item.itemName}' wirklich aus den Vorschlägen löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    onSuggestedLongClick(item)
                    itemToDelete = null
                }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Übernahme-Bestätigung für Vorschläge
    itemToSuggest?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToSuggest = null },
            title = { Text("In Vorschläge übernehmen") },
            text = { Text("Möchten Sie '${item.itemName}' in die Vorschlagsliste übernehmen?") },
            confirmButton = {
                TextButton(onClick = {
                    onUncheckedLongClick(item)
                    itemToSuggest = null
                }) {
                    Text("Übernehmen")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToSuggest = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Listennamen ändern Dialog
    if (showRenameDialog) {
        RenameLiztDialog(
            currentName = lizt.liztName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onRenameLizt(newName)
                showRenameDialog = false
            }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            onDismiss = { showColorPicker = false },
            onColorSelected = { colorHex ->
                onColorSelected(colorHex)
                showColorPicker = false
            }
        )
    }

    // Liste löschen Dialog
    if (showDeleteLiztDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteLiztDialog = false },
            title = { Text("Liste löschen") },
            text = { Text("Möchten Sie die Liste '${lizt.liztName}' wirklich unwiderruflich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLizt()
                        showDeleteLiztDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Endgültig löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteLiztDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun ColorPickerDialog(onDismiss: () -> Unit, onColorSelected: (String) -> Unit) {
    val colors = listOf(
        "#FFFFFFFF", // White
        "#FFEF9A9A", // Red 200
        "#FFA5D6A7", // Green 200
        "#FF90CAF9", // Blue 200
        "#FFFFE082", // Amber 200
        "#FFCE93D8"  // Purple 200
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Farbe wählen",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(colorHex)),
                                    CircleShape
                                )
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { onColorSelected(colorHex) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
            }
        }
    }
}

@Composable
fun RenameLiztDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newName by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Listennamen ändern",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (newName.isNotBlank()) onConfirm(newName) },
                        enabled = newName.isNotBlank()
                    ) {
                        Text("Übernehmen")
                    }
                }
            }
        }
    }
}
