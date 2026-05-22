package com.example.data

import kotlinx.coroutines.flow.Flow

class SanctuaryRepository(
    private val boardDao: BoardDao,
    private val pinItemDao: PinItemDao
) {
    val allBoards: Flow<List<Board>> = boardDao.getAllBoards()
    val allInspirationPins: Flow<List<PinItem>> = pinItemDao.getAllInspirationPins()

    suspend fun getBoardCount(): Int {
        return boardDao.getBoardCount()
    }

    fun getPinsForBoard(boardId: Int): Flow<List<PinItem>> {
        return pinItemDao.getPinsForBoard(boardId)
    }

    suspend fun insertBoard(board: Board): Long {
        return boardDao.insertBoard(board)
    }

    suspend fun deleteBoard(boardId: Int) {
        boardDao.deleteBoard(boardId)
    }

    suspend fun insertPin(pinItem: PinItem): Long {
        return pinItemDao.insertPin(pinItem)
    }

    suspend fun updatePin(pinItem: PinItem) {
        pinItemDao.updatePin(pinItem)
    }

    suspend fun deletePin(pinId: Int) {
        pinItemDao.deletePin(pinId)
    }
}
