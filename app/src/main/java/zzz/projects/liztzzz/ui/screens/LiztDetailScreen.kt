package zzz.projects.liztzzz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zzz.projects.liztzzz.data.Lizt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiztDetailScreen(
    lizt: Lizt,
    onBack: () -> Unit,
    onToggleChecked: (Int, Boolean) -> Unit,
    onAddItem: (String) -> Unit,
    onDeleteChecked: () -> Unit,
    onToggleSuggests: () -> Unit
) {
    BackHandler(onBack = onBack)
    var newItemName by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lizt.liztName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Einstellungen")
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
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
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
                        singleLine = true
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
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
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

            if (lizt.hasSuggests) {
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
}
