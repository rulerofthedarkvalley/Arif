package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boards")
data class Board(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Explore", "Creative", "Growth", "Career"
    val coverImageUrl: String
)

@Entity(tableName = "pin_items")
data class PinItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val boardId: Int, // if 0, this represents a feed template in the Curated Gallery
    val type: String, // "IMAGE", "NOTE", "QUOTE", "READING_LIST", "HABIT"
    val title: String,
    val subtitle: String = "",
    val bodyText: String = "",
    val imageUrl: String = "",
    val tag: String = "", // e.g., "Environment", "Health", "Career"
    val isPinned: Boolean = false,
    val itemsList: String = "", // Comma-separated strings, e.g., "Book A,Book B" or "Habit A,Habit B"
    val completedList: String = "", // Comma-separated strings of completed items
    val posX: Float = 0f,
    val posY: Float = 0f,
    val rotation: Float = 0f,
    val fontSerif: Boolean = false,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val bgColor: String = "" // "navy", "green", "cream", or hex
)
