package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.AddContentDialog
import com.example.ui.BoardCanvasScreen
import com.example.ui.CreateBoardDialog
import com.example.ui.InspirationGalleryScreen
import com.example.ui.MyBoardsScreen
import com.example.ui.RichTextEditorSheet
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.WarmBackground
import com.example.viewmodel.SanctuaryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SanctuaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainSanctuaryShell(viewModel)
            }
        }
    }
}

@Composable
fun MainSanctuaryShell(viewModel: SanctuaryViewModel) {
    var activeTab by remember { mutableStateOf("canvas") } // "canvas", "boards"
    
    var showAddPinDialog by remember { mutableStateOf(false) }
    var showCreateBoardDialog by remember { mutableStateOf(false) }
    
    val boards by viewModel.allBoards.collectAsState()
    val activeBoardId by viewModel.activeBoardId.collectAsState()
    val editingPin by viewModel.editingPin.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            bottomBar = {
                if (!isTablet && !isFullScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        // Workspace canvas navigation
                        NavigationBarItem(
                            selected = activeTab == "canvas",
                            onClick = { activeTab = "canvas" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "My Canvas",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Workspace") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = DeepNavy,
                                indicatorColor = SageGreen,
                                unselectedIconColor = SageGreen.copy(alpha = 0.5f),
                                unselectedTextColor = SageGreen.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("nav_canvas_tab")
                        )
                        
                        // Custom boards list panel
                        NavigationBarItem(
                            selected = activeTab == "boards",
                            onClick = { activeTab = "boards" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "My Custom Boards",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("My Boards") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = DeepNavy,
                                indicatorColor = SageGreen,
                                unselectedIconColor = SageGreen.copy(alpha = 0.5f),
                                unselectedTextColor = SageGreen.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("nav_boards_tab")
                        )
                    }
                }
            },
            floatingActionButton = {
                if (activeTab == "canvas" && !isFullScreen) {
                    FloatingActionButton(
                        onClick = { showAddPinDialog = true },
                        containerColor = SageGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("floating_add_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Pinned Content")
                    }
                }
            }
        ) { innerPadding ->
            val padding = if (isFullScreen) PaddingValues(0.dp) else innerPadding
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isTablet && !isFullScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(84.dp),
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SageGreen.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Sanctuary Logo",
                                        tint = SageGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sanctuary",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        NavigationRailItem(
                            selected = activeTab == "canvas",
                            onClick = { activeTab = "canvas" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "My Canvas",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Workspace") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = DeepNavy,
                                indicatorColor = SageGreen,
                                unselectedIconColor = SageGreen.copy(alpha = 0.5f),
                                unselectedTextColor = SageGreen.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("nav_canvas_tab")
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        NavigationRailItem(
                            selected = activeTab == "boards",
                            onClick = { activeTab = "boards" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "My Custom Boards",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("My Boards") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = DeepNavy,
                                indicatorColor = SageGreen,
                                unselectedIconColor = SageGreen.copy(alpha = 0.5f),
                                unselectedTextColor = SageGreen.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("nav_boards_tab")
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (activeTab) {
                            "canvas" -> {
                                BoardCanvasScreen(
                                    viewModel = viewModel,
                                    onEditNote = { pin -> viewModel.startEditingPin(pin) },
                                    onAddNewPinClick = { showAddPinDialog = true },
                                    isTablet = isTablet
                                )
                            }
                            
                            "boards" -> {
                                MyBoardsScreen(
                                    viewModel = viewModel,
                                    onNavigateBackToCanvas = { activeTab = "canvas" },
                                    onCreateNewBoardClick = { showCreateBoardDialog = true },
                                    isTablet = isTablet
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Overlay Dialogue sheets hooks ---
    if (showAddPinDialog) {
        AddContentDialog(
            activeBoardId = activeBoardId,
            boards = boards,
            onDismiss = { showAddPinDialog = false },
            onSubmit = { boardId, type, title, subtitle, bodyText, imageUrl, tag, bgColor, itemsList ->
                viewModel.addNewPin(
                    boardId = boardId,
                    type = type,
                    title = title,
                    subtitle = subtitle,
                    bodyText = bodyText,
                    imageUrl = imageUrl,
                    tag = tag,
                    bgColor = bgColor,
                    itemsList = itemsList
                )
                showAddPinDialog = false
            }
        )
    }

    if (showCreateBoardDialog) {
        CreateBoardDialog(
            onDismiss = { showCreateBoardDialog = false },
            onSubmit = { title, desc, category, coverImageUrl ->
                viewModel.createNewBoard(
                    title = title,
                    description = desc,
                    category = category,
                    coverImageUrl = coverImageUrl
                )
                showCreateBoardDialog = false
            }
        )
    }

    editingPin?.let { pin ->
        RichTextEditorSheet(
            pin = pin,
            onDismiss = { viewModel.stopEditingPin() },
            onSave = { updatedPin ->
                viewModel.updatePin(updatedPin)
                viewModel.stopEditingPin()
            }
        )
    }
}
