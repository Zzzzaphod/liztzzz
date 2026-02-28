package zzz.projects.liztzzz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zzz.projects.liztzzz.data.Lizt

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
