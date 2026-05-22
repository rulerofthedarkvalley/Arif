package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import com.example.data.Board
import com.example.data.PinItem
import com.example.ui.theme.*
import com.example.viewmodel.SanctuaryViewModel

// --- Dotted Background Canvas ---
@Composable
fun DottedCanvasBackground(modifier: Modifier = Modifier) {
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val spacing = 24.dp.toPx()
        val radius = 1.5.dp.toPx()
        
        if (spacing > 0f) {
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = Offset(x, y)
                    )
                    y += spacing
                }
                x += spacing
            }
        }
    }
}

// --- Custom Beautiful Zero-Dependency Sanctuary Chip ---
@Composable
fun SanctuaryChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(if (selected) SageGreen else SurfaceCream)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextDark
        )
    }
}

// --- SCREEN 1: Active Board / Draggable Canvas ---
@Composable
fun BoardCanvasScreen(
    viewModel: SanctuaryViewModel,
    onEditNote: (PinItem) -> Unit,
    onAddNewPinClick: () -> Unit,
    isTablet: Boolean = false
) {
    val boards by viewModel.allBoards.collectAsState()
    val activeBoardId by viewModel.activeBoardId.collectAsState()
    val activePins by viewModel.activeBoardPins.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()
    
    val activeBoard = boards.find { it.id == activeBoardId } ?: Board(title = "Inspiration", description = "", category = "", coverImageUrl = "")
    var viewModeCanvas by remember { mutableStateOf(true) } // true = Drag Canvas, false = Staggered Grid
    var showCreateBoardInCanvas by remember { mutableStateOf(false) }
    var showEditWorkspaceDialog by remember { mutableStateOf(false) }
    
    Row(modifier = Modifier.fillMaxSize()) {
        if (isTablet && !isFullScreen) {
            // Elegant Vision Boards Side Panel
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                    ),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 20.dp, horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY WORKSPACES",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.toggleFullScreen() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Turn Workspace Vision Board into Full Screen",
                                    tint = SageGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showCreateBoardInCanvas = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Create Board",
                                    tint = SageGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCreateBoardInCanvas = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCream.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("New Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SageGreen)
                                }
                            }
                        }

                        items(boards) { b ->
                            val isActive = b.id == activeBoardId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectBoard(b.id) }
                                    .border(
                                        width = if (isActive) 1.5.dp else 0.5.dp,
                                        color = if (isActive) TerracottaWarm else SageGreen.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) SurfaceCream.copy(alpha = 0.5f) else Color.White
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (b.coverImageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = b.coverImageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (isActive) SageGreen else SageGreen.copy(alpha = 0.25f),
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = b.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = b.category,
                                            fontSize = 9.sp,
                                            color = SageGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(TerracottaWarm, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            DottedCanvasBackground()
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Elegant Canvas Header Info
                if (!isFullScreen) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Active Sanctuary",
                                            tint = SageGreen,
                                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                                        )
                                        Text(
                                            text = activeBoard.title.uppercase(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SageGreen,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { showEditWorkspaceDialog = true },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Workspace Title",
                                                tint = SageGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (activeBoard.description.isBlank()) "Your space for creative visualization." else activeBoard.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                                
                                // Header Action Cluster
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (viewModeCanvas) "Canvas Mode" else "Grid Mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    IconButton(onClick = { viewModeCanvas = !viewModeCanvas }) {
                                        Icon(
                                            imageVector = if (viewModeCanvas) Icons.Default.List else Icons.Default.Home,
                                            contentDescription = "Toggle Grid/Canvas View",
                                            tint = SageGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    TextButton(
                                        onClick = { viewModel.toggleFullScreen() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = SageGreen),
                                        modifier = Modifier.testTag("toggle_fullscreen_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Toggle Full Screen",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Full Screen",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = onAddNewPinClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("add_intent_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Intent", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Intent", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (activePins.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Empty Sanctuary",
                                tint = SageGreen.copy(alpha = 0.35f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "A Peaceful Mind Starts with Intention",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add note, quote, checklist, or inspiration image cards to your daily workspace.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 280.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onAddNewPinClick,
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                            ) {
                                Text("Create First Pin")
                            }
                        }
                    }
                } else if (viewModeCanvas) {
                    // DRAG CANVAS sorted by zIndex ascending, so higher zIndex is drawn on top
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RectangleShape)
                    ) {
                        val canvasWidth = constraints.maxWidth.toFloat()
                        val canvasHeight = constraints.maxHeight.toFloat()
                        
                        val safeWidth = if (canvasWidth > 0f) canvasWidth else 1080f
                        val safeHeight = if (canvasHeight > 0f) canvasHeight else 800f

                        activePins.sortedWith(compareBy({ it.zIndex }, { it.id })).forEach { pin ->
                            DraggablePinCard(
                                pin = pin,
                                canvasWidth = safeWidth,
                                canvasHeight = safeHeight,
                                onUpdatePosition = { x, y -> viewModel.updatePinPosition(pin, x, y) },
                                onDelete = { viewModel.deletePin(pin.id) },
                                onEdit = { onEditNote(pin) },
                                onToggleHabit = { item -> viewModel.toggleHabitItem(pin, item) },
                                onToggleBook = { item -> viewModel.toggleReadingItem(pin, item) },
                                onBringToFront = {
                                    val maxZIndex = activePins.maxOfOrNull { it.zIndex } ?: 1f
                                    if (pin.zIndex <= maxZIndex) {
                                        viewModel.updatePin(pin.copy(zIndex = maxZIndex + 1f))
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // GRID
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(minSize = 240.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalItemSpacing = 16.dp
                        ) {
                            items(activePins, key = { it.id }) { pin ->
                                StaticPinItemCard(
                                    pin = pin,
                                    onEdit = { onEditNote(pin) },
                                    onDelete = { viewModel.deletePin(pin.id) },
                                    onToggleHabit = { item -> viewModel.toggleHabitItem(pin, item) },
                                    onToggleBook = { item -> viewModel.toggleReadingItem(pin, item) }
                                )
                            }
                        }
                    }
                }
            }

            if (isFullScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { viewModel.setFullScreen(false) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .testTag("exit_fullscreen_floating_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Full Screen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
    
    if (showCreateBoardInCanvas) {
        CreateBoardDialog(
            onDismiss = { showCreateBoardInCanvas = false },
            onSubmit = { title, desc, category, coverImageUrl ->
                viewModel.createNewBoard(
                    title = title,
                    description = desc,
                    category = category,
                    coverImageUrl = coverImageUrl
                )
                showCreateBoardInCanvas = false
            }
        )
    }
    
    if (showEditWorkspaceDialog) {
        EditBoardDialog(
            board = activeBoard,
            onDismiss = { showEditWorkspaceDialog = false },
            onSubmit = { title, desc, category, coverImageUrl ->
                viewModel.updateBoard(
                    activeBoard.copy(
                        title = title,
                        description = desc,
                        category = category,
                        coverImageUrl = coverImageUrl
                    )
                )
                showEditWorkspaceDialog = false
            }
        )
    }
}

// --- Draggable Physical Card Component ---
@Composable
fun DraggablePinCard(
    pin: PinItem,
    canvasWidth: Float,
    canvasHeight: Float,
    onUpdatePosition: (Float, Float) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleHabit: (String) -> Unit,
    onToggleBook: (String) -> Unit,
    onBringToFront: () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val cardWidthPx = remember(pin.width, density) {
        with(density) { pin.width.dp.toPx() }
    }
    val cardHeightPx = remember(pin.height, density) {
        with(density) { if (pin.height > 0f) pin.height.dp.toPx() else 300.dp.toPx() }
    }

    val initialX = remember(pin.posX, canvasWidth) {
        val normX = if (pin.posX > 1.2f) {
            val ratio = ((pin.posX - 40f) / (820f - 40f)).coerceIn(0f, 1f)
            0.02f + ratio * 0.96f
        } else {
            pin.posX.coerceIn(0f, 1f)
        }
        val maxAvailableX = (canvasWidth - cardWidthPx).coerceAtLeast(0f)
        normX * maxAvailableX
    }

    val initialY = remember(pin.posY, canvasHeight) {
        val normY = if (pin.posY > 1.2f) {
            val ratio = ((pin.posY - 40f) / (480f - 40f)).coerceIn(0f, 1f)
            0.03f + ratio * 0.92f
        } else {
            pin.posY.coerceIn(0f, 1f)
        }
        val maxAvailableY = (canvasHeight - cardHeightPx).coerceAtLeast(0f)
        normY * maxAvailableY
    }

    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }
    
    LaunchedEffect(initialX, initialY) {
        offsetX = initialX
        offsetY = initialY
    }
    
    val safeOffsetX = if (offsetX.isFinite()) offsetX else 0f
    val safeOffsetY = if (offsetY.isFinite()) offsetY else 0f
    
    val baseModifier = Modifier
        .offset { IntOffset(safeOffsetX.roundToInt(), safeOffsetY.roundToInt()) }
        .pointerInput(pin.id, canvasWidth, canvasHeight) {
            detectDragGestures(
                onDragStart = { onBringToFront() },
                onDragEnd = { 
                    if (safeOffsetX.isFinite() && safeOffsetY.isFinite() && canvasWidth > 0f && canvasHeight > 0f) {
                        val maxAvailableX = (canvasWidth - cardWidthPx).coerceAtLeast(1f)
                        val maxAvailableY = (canvasHeight - cardHeightPx).coerceAtLeast(1f)
                        val finalNormX = (safeOffsetX / maxAvailableX).coerceIn(0f, 1f)
                        val finalNormY = (safeOffsetY / maxAvailableY).coerceIn(0f, 1f)
                        onUpdatePosition(finalNormX, finalNormY) 
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val nextX = offsetX + dragAmount.x
                    val nextY = offsetY + dragAmount.y
                    if (nextX.isFinite() && nextY.isFinite() && canvasWidth > 0f && canvasHeight > 0f) {
                        offsetX = nextX.coerceIn(0f, (canvasWidth - cardWidthPx).coerceAtLeast(0f))
                        offsetY = nextY.coerceIn(0f, (canvasHeight - cardHeightPx).coerceAtLeast(0f))
                    }
                }
            )
        }
        .graphicsLayer(rotationZ = if (pin.rotation.isFinite()) pin.rotation else 0f)

    val sizeModifier = if (pin.height > 0f) {
        baseModifier.size(pin.width.dp, pin.height.dp)
    } else {
        baseModifier.width(pin.width.dp)
    }

    Box(
        modifier = sizeModifier
    ) {
        CorePinCard(
            pin = pin,
            onEdit = onEdit,
            onDelete = onDelete,
            onToggleHabit = onToggleHabit,
            onToggleBook = onToggleBook,
            isDraggable = true,
            onBringToFront = onBringToFront
        )
    }
}

@Composable
fun StaticPinItemCard(
    pin: PinItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleHabit: (String) -> Unit,
    onToggleBook: (String) -> Unit
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .graphicsLayer(rotationZ = pin.rotation * 0.5f)
        
    val sizeModifier = if (pin.height > 0f) {
        baseModifier.height(pin.height.dp)
    } else {
        baseModifier
    }

    Box(modifier = sizeModifier) {
        CorePinCard(
            pin = pin,
            onEdit = onEdit,
            onDelete = onDelete,
            onToggleHabit = onToggleHabit,
            onToggleBook = onToggleBook,
            isDraggable = false
        )
    }
}

@Composable
fun CorePinCard(
    pin: PinItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleHabit: (String) -> Unit,
    onToggleBook: (String) -> Unit,
    isDraggable: Boolean,
    onBringToFront: () -> Unit = {}
) {
    val cardBg = when (pin.bgColor.lowercase()) {
        "navy" -> DeepNavy
        "green" -> SageGreen
        "teal" -> CalmTeal
        "cream" -> SurfaceCream
        else -> CardWhite
    }
    val cardOnBg = if (pin.bgColor.lowercase() == "navy" || pin.bgColor.lowercase() == "green" || pin.bgColor.lowercase() == "teal") Color.White else TextDark
    val cardMutedOnBg = if (pin.bgColor.lowercase() == "navy" || pin.bgColor.lowercase() == "green" || pin.bgColor.lowercase() == "teal") Color.White.copy(alpha = 0.7f) else TextMuted
    
    val serifFont = if (pin.fontSerif) FontFamily.Serif else FontFamily.Default
    val textWeight = if (pin.isBold) FontWeight.Bold else FontWeight.Normal
    val textStyle = if (pin.isItalic) FontStyle.Italic else FontStyle.Normal
    val textDecor = if (pin.isUnderline) TextDecoration.Underline else TextDecoration.None
    
    val cardShape = when (pin.shape.lowercase()) {
        "square" -> RectangleShape
        "cut" -> CutCornerShape(12.dp)
        "circle" -> CircleShape
        "capsule" -> RoundedCornerShape(percent = 50)
        else -> RoundedCornerShape(12.dp)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDraggable) 6.dp else 2.dp,
                shape = cardShape,
                clip = false
            )
            .border(
                width = 1.dp,
                color = if (pin.bgColor.lowercase() == "cream") SageGreen.copy(alpha = 0.15f) else Color.Transparent,
                shape = cardShape
            )
            .clickable { onBringToFront() },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = if (pin.height > 0f) Modifier.fillMaxSize() else Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (pin.isPinned) TerracottaWarm else CalmTeal.copy(alpha = 0.5f))
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pin.tag.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SageGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = pin.tag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pin.bgColor.lowercase() == "navy") ActiveSageGreen else SageGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        
                        // Bring to Front Option
                        if (isDraggable) {
                            IconButton(
                                onClick = onBringToFront,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Bring to Front",
                                    tint = cardMutedOnBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Card Props",
                                tint = cardMutedOnBg,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Pin",
                                tint = cardMutedOnBg,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            
            when (pin.type.uppercase()) {
                "IMAGE" -> {
                    if (pin.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = pin.imageUrl,
                            contentDescription = pin.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (pin.title.isNotBlank() || pin.bodyText.isNotBlank()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (pin.title.isNotBlank()) {
                                Text(
                                    text = pin.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = cardOnBg,
                                    fontFamily = serifFont
                                )
                            }
                            if (pin.bodyText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pin.bodyText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cardMutedOnBg,
                                    fontFamily = serifFont,
                                    fontWeight = textWeight,
                                    fontStyle = textStyle,
                                    textDecoration = textDecor
                                )
                            }
                        }
                    }
                }
                
                "NOTE" -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (pin.title.isNotBlank()) {
                            Text(
                                text = pin.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = cardMutedOnBg,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Text(
                            text = pin.bodyText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = serifFont,
                            fontWeight = textWeight,
                            fontStyle = textStyle,
                            textDecoration = textDecor,
                            color = cardOnBg,
                            lineHeight = 22.sp
                        )
                        if (pin.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "— " + pin.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = cardMutedOnBg,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                "QUOTE" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "“",
                            fontFamily = FontFamily.Serif,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pin.bgColor.lowercase() == "navy") SubtleGreen else SageGreen,
                            lineHeight = 0.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pin.bodyText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = cardOnBg,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        if (pin.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "— " + pin.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = cardMutedOnBg,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                
                "READING_LIST" -> {
                    if (pin.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = pin.imageUrl,
                            contentDescription = "Reading banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = pin.title.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardOnBg,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val items = pin.itemsList.split(",").filter { it.isNotBlank() }
                        val completed = pin.completedList.split(",").filter { it.isNotBlank() }
                        
                        items.forEach { book ->
                            val isChecked = completed.contains(book)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleBook(book) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Bespoke Premium custom circular checkbox
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, cardMutedOnBg, CircleShape)
                                        .background(if (isChecked) SageGreen else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = book,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isChecked) cardMutedOnBg else cardOnBg,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }
                    }
                }
                
                "HABIT" -> {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pin.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = cardOnBg
                            )
                            Icon(Icons.Default.Favorite, contentDescription = "Active Habit", tint = SageGreen, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val habits = pin.itemsList.split(",").filter { it.isNotBlank() }
                        val completed = pin.completedList.split(",").filter { it.isNotBlank() }
                        
                        habits.forEach { habit ->
                            val isChecked = completed.contains(habit)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleHabit(habit) }
                                    .padding(vertical = 6.dp)
                                    .background(
                                        color = if (isChecked) SageGreen.copy(alpha = 0.08f) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Custom circular tick button
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, cardMutedOnBg, CircleShape)
                                        .background(if (isChecked) SageGreen else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = habit,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isChecked) cardMutedOnBg else cardOnBg,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { /* Simulated target adaptation */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Adopt Daily Habit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: Curated Inspiration Gallery ---
@Composable
fun InspirationGalleryScreen(
    viewModel: SanctuaryViewModel,
    isTablet: Boolean = false
) {
    val sampleSpark by viewModel.dailySpark.collectAsState()
    val isGeneratingSpark by viewModel.isGeneratingSpark.collectAsState()
    val boards by viewModel.allBoards.collectAsState()
    val rawPins by viewModel.allInspirationPins.collectAsState()
    val activeCategory by viewModel.selectedGalleryCategory.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectBoardDropdownForPin by remember { mutableStateOf<PinItem?>(null) }
    
    val pins = rawPins.filter { pin ->
        val matchesCategory = (activeCategory == "All") || (pin.tag.equals(activeCategory, ignoreCase = true))
        val matchesSearch = pin.title.contains(searchQuery, ignoreCase = true) || pin.bodyText.contains(searchQuery, ignoreCase = true) || pin.tag.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }
    
    val categories = listOf("All", "Minimalism", "Growth", "Nature", "Urban Calm")
    
    Box(modifier = Modifier.fillMaxSize()) {
        DottedCanvasBackground()
        
        if (isTablet) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // LEFT SIDEBAR: Daily AI Spark Oracle & Trending themings
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "DAILY ORACLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ActiveSageGreen, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream)
                    ) {
                        Column {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDbFljJmGnj__bllNfVZoMofDG8EEYE3tQHWvgxOlrLkWW7KkilnqzTVuVpwKVu9VApqYHI-tEkRMM5Gt6q6Z8ZDbDnIpMkNVaX_IVE2Cm7VVtFm6ZOK88t9qSR4g-Ld1cmLq3YN_2bxlYrPwr_-tTmp1AEUnrIl7212j8xum6sDcJjGGYl0MxqDZ8gabXyfdoM3mj2CbevXXxWHYvjyt956KlElUzhBBW8LSbWo6ILBZzGvCIkKUqaZcpMwOg5WNMyr4BT8k7vTQo",
                                contentDescription = "Daily Spark Banner",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DeepNavy)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("DAILY SPARK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.generateNewDailySpark(activeCategory) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        if (isGeneratingSpark) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                        } else {
                                            Text("AI Refresh", fontSize = 10.sp)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = sampleSpark.quote,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    color = TextDark,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "— " + sampleSpark.author,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        selectBoardDropdownForPin = PinItem(
                                            boardId = 1,
                                            type = "QUOTE",
                                            title = "AI Prompt Intention",
                                            bodyText = sampleSpark.quote,
                                            subtitle = sampleSpark.author,
                                            bgColor = "navy",
                                            tag = activeCategory
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Pin", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pin Spark to Canvas")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "TRENDING THEMINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SageGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            MiniTrendingCard(
                                image = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6ZmJoqsHG1OaXRFIU-batht1jQ6GubqhGJuCOhgVNYHyQCLcHBStSsKxOvKhXeFhUNF-ZEp_lwj0Rb3_hPusVF01pKuIew5KUT0HMl9dqPAdbLSZAbuOQ15Kagwpl8CtuGB0TVI8KAhc4GOYOvOcKANsEGaBba6iztd96QkqH2z1iG70IStdSYMkT69DnBZIi-NK3KjTPbEK_FrmrSe4qw7YAfIlqsS5VXQ-Fg0Ae4h4M3UMtea-V7PGAlNXFEI_3eF4kxekxYWE",
                                label = "Minimalist Living",
                                count = "2.4k Pins"
                            )
                        }
                        item {
                            MiniTrendingCard(
                                image = "https://lh3.googleusercontent.com/aida-public/AB6AXuBmrxWYdmGZSi_plfbszlKCBNC2ExzHa1u8jOpHSKMsNIoko2SEs4ukVmyEU6dpN97-OvnSJxpZIY0I3qBPUxujrmU1VuyTsfhrLpINrTxb0Y1SpRWnZyt_a7ecNh3pbbkAEgJnX4CqdoZjQPSNy9YCWb1Jg6PaaJ88YIrcOWrvNJV9IVCErRgBwzKN_ptd9XR8SbfhhR-_yls9VZyacMU-hqPIE1gbX99xH8TLvL2huxS5AM0U5ZFDiQeTE--viIvVt6AkOCUue8U",
                                label = "Career Growth",
                                count = "1.8k Pins"
                            )
                        }
                        item {
                            MiniTrendingCard(
                                image = "https://lh3.googleusercontent.com/aida-public/AB6AXuCPH2wwlXbj0dqg2vejLEwK1UIotNuo9QhXmRx3weJpo6WojFiLK93AQxc4Q-7USROekroiATzCndN6GLXborAC5VeWyoWCBbb5458qwTSknNfl4Y-8AlyEer5yOfZp7TB-YWWjcW_fhWxGT6LJVGmvvplouZwjEM384YUn5ku_pXJN-a3Hf7hfuT-RoK07bD3V7Jt86bltdBzvMiyjvCvjOwn8sohpY6Wo7BRO5Mm2VHIPVM32rkIL8rYm-sh9nKChyJyl4gFNUrg",
                                label = "Travel Sanctuary",
                                count = "3.1k Pins"
                            )
                        }
                    }
                }
                
                // RIGHT SIDE: Curated board Masonry list & Headers
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INSPIRATION GALLERY",
                                style = MaterialTheme.typography.labelMedium,
                                color = SageGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Explore comforting concepts, quotes, and layouts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search intents...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .width(220.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = CardWhite,
                                unfocusedBorderColor = SageGreen.copy(alpha = 0.2f),
                                focusedBorderColor = SageGreen
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = activeCategory == category
                            SanctuaryChip(
                                selected = isSelected,
                                onClick = { viewModel.setGalleryCategory(category) },
                                label = category
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = 220.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 16.dp
                    ) {
                        items(pins) { pin ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite)
                            ) {
                                Column {
                                    Box {
                                        if (pin.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = pin.imageUrl,
                                                contentDescription = pin.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(90.dp)
                                                    .background(
                                                        if (pin.bgColor.lowercase() == "navy") DeepNavy else SageGreen
                                                    )
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = { selectBoardDropdownForPin = pin },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Pin to workspace",
                                                tint = TerracottaWarm,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        if (pin.tag.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(8.dp)
                                                    .background(CardWhite.copy(alpha = 0.92f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(pin.tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SageGreen)
                                            }
                                        }
                                    }
                                    
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        if (pin.title.isNotBlank()) {
                                            Text(
                                                text = pin.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                        }
                                        if (pin.bodyText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = pin.bodyText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                fontFamily = if (pin.fontSerif) FontFamily.Serif else FontFamily.Default,
                                                maxLines = 4
                                            )
                                        }
                                        
                                        if (pin.itemsList.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val tasks = pin.itemsList.split(",").filter { it.isNotBlank() }
                                            tasks.forEach { task ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = SageGreen.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(task, fontSize = 11.sp, color = TextMuted)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INSPIRATION GALLERY",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Explore comforting concepts, quotes, and layouts of intent.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search intents, colors, moods...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                        modifier = Modifier
                            .width(280.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            unfocusedBorderColor = SageGreen.copy(alpha = 0.2f),
                            focusedBorderColor = SageGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = activeCategory == category
                        SanctuaryChip(
                            selected = isSelected,
                            onClick = { viewModel.setGalleryCategory(category) },
                            label = category
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 250.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalItemSpacing = 16.dp
                ) {
                    // FEATURED ITEM: Gemini AI Spark Banner
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ActiveSageGreen, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCream)
                        ) {
                            Column {
                                AsyncImage(
                                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDbFljJmGnj__bllNfVZoMofDG8EEYE3tQHWvgxOlrLkWW7KkilnqzTVuVpwKVu9VApqYHI-tEkRMM5Gt6q6Z8ZDbDnIpMkNVaX_IVE2Cm7VVtFm6ZOK88t9qSR4g-Ld1cmLq3YN_2bxlYrPwr_-tTmp1AEUnrIl7212j8xum6sDcJjGGYl0MxqDZ8gabXyfdoM3mj2CbevXXxWHYvjyt956KlElUzhBBW8LSbWo6ILBZzGvCIkKUqaZcpMwOg5WNMyr4BT8k7vTQo",
                                    contentDescription = "Daily Spark Banner",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DeepNavy)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("DAILY SPARK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Button(
                                            onClick = { viewModel.generateNewDailySpark(activeCategory) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            if (isGeneratingSpark) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                            } else {
                                                Text("AI Refresh", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        text = sampleSpark.quote,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        color = TextDark,
                                        lineHeight = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "— " + sampleSpark.author,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            selectBoardDropdownForPin = PinItem(
                                                boardId = 1,
                                                type = "QUOTE",
                                                title = "AI Prompt Intention",
                                                bodyText = sampleSpark.quote,
                                                subtitle = sampleSpark.author,
                                                bgColor = "navy",
                                                tag = activeCategory
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Pin", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pin Spark to Canvas")
                                    }
                                }
                            }
                        }
                    }
                    
                    // TRENDING CARDS LIST
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "TRENDING THEMINGS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SageGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    MiniTrendingCard(
                                        image = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6ZmJoqsHG1OaXRFIU-batht1jQ6GubqhGJuCOhgVNYHyQCLcHBStSsKxOvKhXeFhUNF-ZEp_lwj0Rb3_hPusVF01pKuIew5KUT0HMl9dqPAdbLSZAbuOQ15Kagwpl8CtuGB0TVI8KAhc4GOYOvOcKANsEGaBba6iztd96QkqH2z1iG70IStdSYMkT69DnBZIi-NK3KjTPbEK_FrmrSe4qw7YAfIlqsS5VXQ-Fg0Ae4h4M3UMtea-V7PGAlNXFEI_3eF4kxekxYWE",
                                        label = "Minimalist Living",
                                        count = "2.4k Pins"
                                    )
                                }
                                item {
                                    MiniTrendingCard(
                                        image = "https://lh3.googleusercontent.com/aida-public/AB6AXuBmrxWYdmGZSi_plfbszlKCBNC2ExzHa1u8jOpHSKMsNIoko2SEs4ukVmyEU6dpN97-OvnSJxpZIY0I3qBPUxujrmU1VuyTsfhrLpINrTxb0Y1SpRWnZyt_a7ecNh3pbbkAEgJnX4CqdoZjQPSNy9YCWb1Jg6PaaJ88YIrcOWrvNJV9IVCErRgBwzKN_ptd9XR8SbfhhR-_yls9VZyacMU-hqPIE1gbX99xH8TLvL2huxS5AM0U5ZFDiQeTE--viIvVt6AkOCUue8U",
                                        label = "Career Growth",
                                        count = "1.8k Pins"
                                    )
                                }
                                item {
                                    MiniTrendingCard(
                                        image = "https://lh3.googleusercontent.com/aida-public/AB6AXuCPH2wwlXbj0dqg2vejLEwK1UIotNuo9QhXmRx3weJpo6WojFiLK93AQxc4Q-7USROekroiATzCndN6GLXborAC5VeWyoWCBbb5458qwTSknNfl4Y-8AlyEer5yOfZp7TB-YWWjcW_fhWxGT6LJVGmvvplouZwjEM384YUn5ku_pXJN-a3Hf7hfuT-RoK07bD3V7Jt86bltdBzvMiyjvCvjOwn8sohpY6Wo7BRO5Mm2VHIPVM32rkIL8rYm-sh9nKChyJyl4gFNUrg",
                                        label = "Travel Sanctuary",
                                        count = "3.1k Pins"
                                    )
                                }
                                item {
                                    MiniTrendingCard(
                                        image = "https://lh3.googleusercontent.com/aida-public/AB6AXuBqAGF05gtyac74bVUrfGZDo2urahibn8YGj1kVZbUQmYnSZewh2Mz_dSk3S8iln3m2OE4CGN1q2SbZwB4YhjpAx26oVPR9neEvfybPzt8zKW1YOJT-L64NJsjQ_bdmzrvd6S747j98mmfphsOLtQIGYr9_4XQS1LevGsJ72CI2Yo0aDvyDik5oeMqf6S9hFEYg2w1izZ3AV4GaNsRZ9DNpQL7gwjoRYbdhaUvUHyq4bU3MZMfbCzwdqAADZJePJV5_74rgTjyeYTo",
                                        label = "Nature's Rhythm",
                                        count = "940 Pins"
                                    )
                                }
                            }
                        }
                    }
                    
                    // MASONRY CURATED ITEMS
                    items(pins) { pin ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite)
                        ) {
                            Column {
                                Box {
                                    if (pin.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = pin.imageUrl,
                                            contentDescription = pin.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .background(
                                                    if (pin.bgColor.lowercase() == "navy") DeepNavy else SageGreen
                                                )
                                        )
                                    }
                                    
                                    // Float Pin Overlay Action
                                    IconButton(
                                        onClick = { selectBoardDropdownForPin = pin },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Pin to workspace",
                                            tint = TerracottaWarm,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    if (pin.tag.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                                .background(CardWhite.copy(alpha = 0.92f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(pin.tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SageGreen)
                                        }
                                    }
                                }
                                
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (pin.title.isNotBlank()) {
                                        Text(
                                            text = pin.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                    }
                                    if (pin.bodyText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pin.bodyText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted,
                                            fontFamily = if (pin.fontSerif) FontFamily.Serif else FontFamily.Default,
                                            maxLines = 4
                                        )
                                    }
                                    
                                    if (pin.itemsList.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val tasks = pin.itemsList.split(",").filter { it.isNotBlank() }
                                        tasks.forEach { task ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = SageGreen.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(task, fontSize = 11.sp, color = TextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Select board wrapper
    selectBoardDropdownForPin?.let { pinToClone ->
        Dialog(onDismissRequest = { selectBoardDropdownForPin = null }) {
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Pin Intention to Board",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a personal workspace to mount this inspiration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    boards.forEach { b ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.clonePinToBoard(pinToClone, b.id)
                                    selectBoardDropdownForPin = null
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = b.title, tint = SageGreen, modifier = Modifier.size(18.dp))
                            Column {
                                Text(b.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextDark)
                                if (b.category.isNotBlank()) {
                                    Text(b.category, fontSize = 10.sp, color = SageGreen)
                                }
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceCream))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { selectBoardDropdownForPin = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", color = SageGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniTrendingCard(image: String, label: String, count: String) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(110.dp)
            .shadow(1.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = image,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(count, fontSize = 9.sp, color = ActiveSageGreen)
            }
        }
    }
}

// --- SCREEN 3: My Boards Bento Grid ---
@Composable
fun MyBoardsScreen(
    viewModel: SanctuaryViewModel,
    onNavigateBackToCanvas: () -> Unit,
    onCreateNewBoardClick: () -> Unit,
    isTablet: Boolean = false
) {
    val boards by viewModel.allBoards.collectAsState()
    val activeId by viewModel.activeBoardId.collectAsState()
    
    var selectedPreviewBoardId by remember(boards) {
        mutableStateOf<Int?>(boards.firstOrNull()?.id ?: activeId)
    }
    
    val selectedBoard = boards.find { it.id == selectedPreviewBoardId } ?: boards.firstOrNull()
    
    Box(modifier = Modifier.fillMaxSize()) {
        DottedCanvasBackground()
        
        if (isTablet) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // LEFT SIDEBAR: List of boards
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .padding(top = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MY SANCTUARIES",
                                style = MaterialTheme.typography.labelMedium,
                                color = SageGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "${boards.size} vision spaces",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        
                        IconButton(
                            onClick = onCreateNewBoardClick,
                            modifier = Modifier.background(SageGreen.copy(alpha = 0.1f), CircleShape).size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Board", tint = SageGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            DashedCreateBoardCard(onClick = onCreateNewBoardClick)
                        }
                        
                        items(boards) { b ->
                            val isChosenPreview = b.id == selectedPreviewBoardId
                            val isActive = b.id == activeId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPreviewBoardId = b.id }
                                    .border(
                                        width = if (isChosenPreview) 2.dp else if (isActive) 1.dp else 0.dp,
                                        color = if (isChosenPreview) TerracottaWarm else if (isActive) SageGreen.copy(alpha = 0.35f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChosenPreview) SurfaceCream else CardWhite
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (b.coverImageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = b.coverImageUrl,
                                            contentDescription = b.title,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isActive) DeepNavy else SageGreen.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Menu, contentDescription = null, tint = if (isActive) Color.White else SageGreen, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = b.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = b.category,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SageGreen
                                        )
                                    }
                                    
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(TerracottaWarm)
                                                .size(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // RIGHT DETAIL PANE: Show curated info of the selected board
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(top = 20.dp, bottom = 20.dp)
                ) {
                    if (selectedBoard != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(3.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                ) {
                                    if (selectedBoard.coverImageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = selectedBoard.coverImageUrl,
                                            contentDescription = selectedBoard.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(DeepNavy),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                                        }
                                    }
                                    
                                    // Header status floating badges
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CardWhite.copy(alpha = 0.9f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(selectedBoard.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SageGreen)
                                        }
                                        
                                        if (selectedBoard.id == activeId) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(TerracottaWarm)
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("CURRENT ACTIVE WORKSPACE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = selectedBoard.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        fontFamily = FontFamily.Serif
                                    )
                                    
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = selectedBoard.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        lineHeight = 26.sp,
                                        color = TextDark.copy(alpha = 0.85f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "SANCTUARY PRECEPTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SageGreen,
                                        letterSpacing = 1.2.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Custom visual tip card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Default.Favorite, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = "Every board is a unique room of focus. Add customized quotes, active goals, checklist notes, and adaptive custom reminders in this digital garden.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextDark.copy(alpha = 0.7f),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.height(20.dp))
                                    
                                    Button(
                                        onClick = {
                                            viewModel.selectBoard(selectedBoard.id)
                                            onNavigateBackToCanvas()
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = "Enter Sanctuary")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ENTER ${selectedBoard.title.uppercase()} SANCTUARY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty State details
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No board selected. Choose a workspace or create a new one.")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MY BOARDS",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Your segmented vision boards and digital sanctuaries.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                    
                    Button(
                        onClick = onCreateNewBoardClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("create_board_action")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Board", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Board", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 260.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalItemSpacing = 16.dp
                ) {
                    item {
                        DashedCreateBoardCard(onClick = onCreateNewBoardClick)
                    }
                    
                    items(boards) { b ->
                        val isActive = b.id == activeId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectBoard(b.id)
                                    onNavigateBackToCanvas()
                                }
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) TerracottaWarm else SageGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite)
                        ) {
                            Column {
                                if (b.coverImageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = b.coverImageUrl,
                                        contentDescription = b.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(
                                                if (isActive) DeepNavy else SageGreen.copy(alpha = 0.5f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = b.title,
                                            tint = Color.White.copy(alpha = 0.35f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = b.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(TerracottaWarm.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Active", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TerracottaWarm)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = b.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        maxLines = 3
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SageGreen.copy(alpha = 0.08f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(b.category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SageGreen)
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Open Board",
                                            tint = SageGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashedCreateBoardCard(onClick: () -> Unit) {
    val stroke = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val color = SageGreen.copy(alpha = 0.35f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick)
            .background(color = SurfaceCream.copy(alpha = 0.3f), shape = RoundedCornerShape(14.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = stroke
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
            )
        }
        
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Dashboard",
                tint = SageGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create New Vision Board",
                style = MaterialTheme.typography.labelLarge,
                color = SageGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Define an independent workspace category",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

// --- SCREEN 4: Create new Board Dialog Sheet ---
@Composable
fun CreateBoardDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, category: String, coverImage: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Explore") }
    var coverUrl by remember { mutableStateOf("") }
    
    val categories = listOf("Explore", "Creative", "Growth", "Career")
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "CREATE NEW INTENT BOARD",
                    style = MaterialTheme.typography.labelMedium,
                    color = SageGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Board Title") },
                    modifier = Modifier.fillMaxWidth().testTag("board_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Brief Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("Cover Image Link (URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                Text("Board Classification:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        SanctuaryChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = cat
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSubmit(title, desc, category, coverUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_board_creation")
                    ) {
                        Text("Mantle Sanctuary")
                    }
                }
            }
        }
    }
}

// --- Custom Workspace Editor Dialog ---
@Composable
fun EditBoardDialog(
    board: Board,
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, category: String, coverImage: String) -> Unit
) {
    var title by remember { mutableStateOf(board.title) }
    var desc by remember { mutableStateOf(board.description) }
    var category by remember { mutableStateOf(board.category) }
    var coverUrl by remember { mutableStateOf(board.coverImageUrl) }
    
    val categories = listOf("Explore", "Creative", "Growth", "Career", "Active")
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "EDIT INTENT WORKSPACE",
                    style = MaterialTheme.typography.labelMedium,
                    color = SageGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workspace Title") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_board_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Brief Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("Cover Image Link (URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                Text("Workspace Classification:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        SanctuaryChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = cat
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSubmit(title, desc, category, coverUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_board_edit")
                    ) {
                        Text("Save Workspace")
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: Rich Text Editor Dialog ---
@Composable
fun RichTextEditorSheet(
    pin: PinItem,
    onDismiss: () -> Unit,
    onSave: (PinItem) -> Unit
) {
    var title by remember { mutableStateOf(pin.title) }
    var bodyText by remember { mutableStateOf(pin.bodyText) }
    var subtitle by remember { mutableStateOf(pin.subtitle) }
    
    var fontSerif by remember { mutableStateOf(pin.fontSerif) }
    var isBold by remember { mutableStateOf(pin.isBold) }
    var isItalic by remember { mutableStateOf(pin.isItalic) }
    var isUnderline by remember { mutableStateOf(pin.isUnderline) }
    var bgColor by remember { mutableStateOf(pin.bgColor) }
    
    // Custom size/shape options
    var widthScale by remember { mutableStateOf(pin.width) }
    var heightScale by remember { mutableStateOf(pin.height) }
    var shape by remember { mutableStateOf(pin.shape) }
    var zIndex by remember { mutableStateOf(pin.zIndex) }
    
    val bgOptions = listOf("cream", "navy", "green", "teal", "white")
    val shapesList = listOf("rounded", "cut", "circle", "square", "capsule")
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Scrollable container for all controls inside
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "CARD STYLINGS & FONTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // FORMAT CONTROLS ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCream, RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Serif",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = if (fontSerif) SageGreen else TextMuted,
                            modifier = Modifier
                                .clickable { fontSerif = !fontSerif }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "B",
                            fontWeight = FontWeight.Bold,
                            color = if (isBold) SageGreen else TextMuted,
                            modifier = Modifier
                                .clickable { isBold = !isBold }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "I",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = if (isItalic) SageGreen else TextMuted,
                            modifier = Modifier
                                .clickable { isItalic = !isItalic }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "U",
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnderline) SageGreen else TextMuted,
                            modifier = Modifier
                                .clickable { isUnderline = !isUnderline }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Paint Color:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(modifier = Modifier.width(8.dp))
                        bgOptions.forEach { opt ->
                            val circleColor = when (opt) {
                                "cream" -> SurfaceCream
                                "navy" -> DeepNavy
                                "green" -> SageGreen
                                "teal" -> CalmTeal
                                else -> CardWhite
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(circleColor)
                                    .border(
                                        width = if (bgColor == opt) 2.dp else 1.dp,
                                        color = if (bgColor == opt) TerracottaWarm else SageGreen.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { bgColor = opt }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "DIMENSIONS & GEOMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // CARD WIDTH SLIDER
                    Text("Card Width: ${widthScale.toInt()} dp", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Slider(
                        value = widthScale,
                        onValueChange = { widthScale = it },
                        valueRange = 120f..400f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // CARD HEIGHT SLIDER
                    Text(
                        text = if (heightScale == 0f) "Card Height: Auto (Wrap Content)" else "Card Height: ${heightScale.toInt()} dp",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Slider(
                        value = if (heightScale == 0f) 80f else heightScale,
                        onValueChange = { 
                            heightScale = if (it <= 90f) 0f else it
                        },
                        valueRange = 80f..400f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // CARD SILHOUETTE SELECTOR
                    Text("Silhouettes & Shapes:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        shapesList.forEach { s ->
                            SanctuaryChip(
                                selected = shape.lowercase() == s,
                                onClick = { shape = s },
                                label = s.replaceFirstChar { it.uppercase() }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "DEPTH & LAYERING",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // LAYER ELEVATION (Z-INDEX)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { zIndex += 1f },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen.copy(alpha = 0.12f), contentColor = SageGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bring Forward")
                        }
                        
                        Button(
                            onClick = { zIndex = if (zIndex > 1f) zIndex - 1f else 1f },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen.copy(alpha = 0.12f), contentColor = SageGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Backward")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Layer Level: ${zIndex.toInt()}", style = MaterialTheme.typography.bodySmall, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "CONTENT DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = bodyText,
                        onValueChange = { bodyText = it },
                        label = { Text("Note content / Quote text") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        textStyle = TextStyle(
                            fontFamily = if (fontSerif) FontFamily.Serif else FontFamily.Default,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
                        )
                    )
                    
                    if (pin.type.uppercase() != "IMAGE") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = subtitle,
                            onValueChange = { subtitle = it },
                            label = { Text("Author / Caption Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Dialog Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onSave(
                                pin.copy(
                                    title = title,
                                    bodyText = bodyText,
                                    subtitle = subtitle,
                                    fontSerif = fontSerif,
                                    isBold = isBold,
                                    isItalic = isItalic,
                                    isUnderline = isUnderline,
                                    bgColor = bgColor,
                                    width = widthScale,
                                    height = heightScale,
                                    shape = shape,
                                    zIndex = zIndex
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_rich_edit")
                    ) {
                        Text("Save Intent")
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: Add Content Overlay / Bottom Dialog ---
@Composable
fun AddContentDialog(
    activeBoardId: Int,
    boards: List<Board>,
    onDismiss: () -> Unit,
    onSubmit: (
        boardId: Int,
        type: String,
        title: String,
        subtitle: String,
        bodyText: String,
        imageUrl: String,
        tag: String,
        bgColor: String,
        itemsList: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf("NOTE") }
    var selectedBoardId by remember { mutableStateOf(activeBoardId) }
    
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var bodyText by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var listItemsRaw by remember { mutableStateOf("") }
    var bgColor by remember { mutableStateOf("cream") }
    
    val types = listOf(
        "NOTE" to "Simple Note",
        "IMAGE" to "Image Link",
        "QUOTE" to "Serene Quote",
        "HABIT" to "Mindful Habit",
        "READING_LIST" to "Reading List"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = CardWhite,
            tonalElevation = 8.dp
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // LEFT COLUMN
                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .background(SurfaceCream)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "CHOOSE TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    types.forEach { (typeKey, label) ->
                        val isSelected = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 10.dp)
                                .background(
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DeepNavy else TextMuted
                            )
                        }
                    }
                }
                
                // RIGHT COLUMN
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "PIN DETAILS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SageGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Workspace Board:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCream, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentB = boards.find { it.id == selectedBoardId } ?: boards.firstOrNull() ?: Board(title = "", description = "", category = "", coverImageUrl = "")
                        Text(currentB.title, fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                        
                        Row {
                            boards.take(3).forEach { b ->
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedBoardId == b.id) TerracottaWarm else SageGreen.copy(alpha = 0.2f))
                                        .clickable { selectedBoardId = b.id }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Purpose") },
                        modifier = Modifier.fillMaxWidth().testTag("add_intent_title_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when (selectedType) {
                        "IMAGE" -> {
                            OutlinedTextField(
                                value = imageUrl,
                                onValueChange = { imageUrl = it },
                                label = { Text("Image Link (URL)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bodyText,
                                onValueChange = { bodyText = it },
                                label = { Text("Photo Annotation (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        
                        "QUOTE" -> {
                            OutlinedTextField(
                                value = bodyText,
                                onValueChange = { bodyText = it },
                                label = { Text("Enter beautiful Quote text...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = subtitle,
                                onValueChange = { subtitle = it },
                                label = { Text("Author / Speaker name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        
                        "HABIT" -> {
                            OutlinedTextField(
                                value = listItemsRaw,
                                onValueChange = { listItemsRaw = it },
                                label = { Text("Habit tasks (comma separated list)") },
                                placeholder = { Text("e.g. Morning Meditate, Daily Walk") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            bgColor = "green"
                        }
                        
                        "READING_LIST" -> {
                            OutlinedTextField(
                                value = imageUrl,
                                onValueChange = { imageUrl = it },
                                label = { Text("Optional Cover Image URL") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = listItemsRaw,
                                onValueChange = { listItemsRaw = it },
                                label = { Text("Books (comma separated list)") },
                                placeholder = { Text("e.g. Digital Silence, Stillness Speak") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            bgColor = "cream"
                        }
                        
                        else -> { // NOTE
                            OutlinedTextField(
                                value = bodyText,
                                onValueChange = { bodyText = it },
                                label = { Text("Note content...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = subtitle,
                                onValueChange = { subtitle = it },
                                label = { Text("Subtitle description (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text("Mood category tag (Growth, Career, etc)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSubmit(
                                    selectedBoardId,
                                    selectedType,
                                    title,
                                    subtitle,
                                    bodyText,
                                    imageUrl,
                                    tag,
                                    bgColor,
                                    listItemsRaw
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_intent_creation")
                        ) {
                            Text("Pin Intent")
                        }
                    }
                }
            }
        }
    }
}
