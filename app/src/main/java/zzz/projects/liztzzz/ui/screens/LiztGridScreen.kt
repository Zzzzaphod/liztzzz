package zzz.projects.liztzzz.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import zzz.projects.liztzzz.data.Lizt
import zzz.projects.liztzzz.data.LiztViewModel
import kotlin.math.roundToInt

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
