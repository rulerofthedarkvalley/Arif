package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Board
import com.example.data.GeminiClient
import com.example.data.GeminiSparkResult
import com.example.data.PinItem
import com.example.data.SanctuaryDatabase
import com.example.data.SanctuaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SanctuaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SanctuaryRepository

    // Reactive lists
    val allBoards: StateFlow<List<Board>>
    val allInspirationPins: StateFlow<List<PinItem>>

    // Filters and Active IDs
    private val _activeBoardId = MutableStateFlow(1) // Defaults to "2026 Intentions"
    val activeBoardId: StateFlow<Int> = _activeBoardId.asStateFlow()

    private val _selectedGalleryCategory = MutableStateFlow("All")
    val selectedGalleryCategory: StateFlow<String> = _selectedGalleryCategory.asStateFlow()

    // Active board pins stream, updates automatically when activeBoardId changes
    val activeBoardPins: StateFlow<List<PinItem>>

    // Rich Editor Active state
    private val _editingPin = MutableStateFlow<PinItem?>(null)
    val editingPin: StateFlow<PinItem?> = _editingPin.asStateFlow()

    // Gemini Daily Spark state
    private val _dailySpark = MutableStateFlow(
        GeminiSparkResult(
            quote = "Intentionality is the bridge between aspiration and reality. Focus on what brings genuine peace inside.",
            author = "Sanctuary Guide"
        )
    )
    val dailySpark: StateFlow<GeminiSparkResult> = _dailySpark.asStateFlow()

    private val _isGeneratingSpark = MutableStateFlow(false)
    val isGeneratingSpark: StateFlow<Boolean> = _isGeneratingSpark.asStateFlow()

    init {
        val database = SanctuaryDatabase.getDatabase(application, viewModelScope)
        repository = SanctuaryRepository(database.boardDao(), database.pinItemDao())

        // Create cold flows to shared StateFlows
        val boardsFlow = repository.allBoards
        val inspirationsFlow = repository.allInspirationPins

        // Convert the Room streams into state flows
        val tempBoards = MutableStateFlow<List<Board>>(emptyList())
        val tempInspirations = MutableStateFlow<List<PinItem>>(emptyList())
        val tempActivePins = MutableStateFlow<List<PinItem>>(emptyList())

        allBoards = tempBoards
        allInspirationPins = tempInspirations
        activeBoardPins = tempActivePins

        viewModelScope.launch {
            boardsFlow.collect { tempBoards.value = it }
        }
        viewModelScope.launch {
            inspirationsFlow.collect { tempInspirations.value = it }
        }

        // FlatMapLatest automatically binds the stream when board changes
        viewModelScope.launch {
            _activeBoardId.flatMapLatest { boardId ->
                repository.getPinsForBoard(boardId)
            }.collect {
                tempActivePins.value = it
            }
        }
    }

    fun selectBoard(boardId: Int) {
        _activeBoardId.value = boardId
    }

    fun setGalleryCategory(category: String) {
        _selectedGalleryCategory.value = category
    }

    // Insert new board
    fun createNewBoard(title: String, description: String, category: String, coverImageUrl: String) {
        viewModelScope.launch {
            repository.insertBoard(
                Board(
                    title = title,
                    description = description,
                    category = category,
                    coverImageUrl = coverImageUrl
                )
            )
        }
    }

    // Update existing board
    fun updateBoard(board: Board) {
        viewModelScope.launch {
            repository.insertBoard(board)
        }
    }

    // Add new pin item
    fun addNewPin(
        boardId: Int,
        type: String,
        title: String,
        subtitle: String = "",
        bodyText: String = "",
        imageUrl: String = "",
        tag: String = "",
        bgColor: String = "cream",
        itemsList: String = "",
        fontSerif: Boolean = false,
        isBold: Boolean = false,
        isItalic: Boolean = false,
        isUnderline: Boolean = false,
        posX: Float = 100f,
        posY: Float = 150f
    ) {
        viewModelScope.launch {
            repository.insertPin(
                PinItem(
                    boardId = boardId,
                    type = type,
                    title = title,
                    subtitle = subtitle,
                    bodyText = bodyText,
                    imageUrl = imageUrl,
                    tag = tag,
                    bgColor = bgColor,
                    itemsList = itemsList,
                    fontSerif = fontSerif,
                    isBold = isBold,
                    isItalic = isItalic,
                    isUnderline = isUnderline,
                    posX = posX,
                    posY = posY,
                    rotation = (-3..3).random().toFloat()
                )
            )
        }
    }

    // Direct update
    fun updatePin(pinItem: PinItem) {
        viewModelScope.launch {
            repository.updatePin(pinItem)
        }
    }

    // Update canvas coords
    fun updatePinPosition(pin: PinItem, x: Float, y: Float) {
        viewModelScope.launch {
            repository.updatePin(pin.copy(posX = x, posY = y))
        }
    }

    // Deletion
    fun deletePin(pinId: Int) {
        viewModelScope.launch {
            repository.deletePin(pinId)
        }
    }

    // Clone inspiration pin to any of the workspace boards!
    fun clonePinToBoard(pin: PinItem, targetBoardId: Int) {
        viewModelScope.launch {
            repository.insertPin(
                pin.copy(
                    id = 0, // autoGenerate
                    boardId = targetBoardId,
                    posX = (80..300).random().toFloat(),
                    posY = (80..400).random().toFloat(),
                    rotation = (-3..3).random().toFloat()
                )
            )
        }
    }

    // Edit states
    fun startEditingPin(pin: PinItem) {
        _editingPin.value = pin
    }

    fun stopEditingPin() {
        _editingPin.value = null
    }

    // Toggle Habit checklists
    fun toggleHabitItem(pin: PinItem, itemName: String) {
        val completed = pin.completedList.split(",").filter { it.isNotBlank() }.toMutableList()
        if (completed.contains(itemName)) {
            completed.remove(itemName)
        } else {
            completed.add(itemName)
        }
        val pinUpdated = pin.copy(completedList = completed.joinToString(","))
        updatePin(pinUpdated)
    }

    // Toggle Reading list checklists
    fun toggleReadingItem(pin: PinItem, itemName: String) {
        val completed = pin.completedList.split(",").filter { it.isNotBlank() }.toMutableList()
        if (completed.contains(itemName)) {
            completed.remove(itemName)
        } else {
            completed.add(itemName)
        }
        val pinUpdated = pin.copy(completedList = completed.joinToString(","))
        updatePin(pinUpdated)
    }

    // AI Call
    fun generateNewDailySpark(mood: String) {
        _isGeneratingSpark.value = true
        viewModelScope.launch {
            val result = GeminiClient.generateDailySpark(mood)
            _dailySpark.value = result
            _isGeneratingSpark.value = false
        }
    }
}
