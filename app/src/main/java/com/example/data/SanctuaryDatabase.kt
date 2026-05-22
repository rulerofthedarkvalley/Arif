package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards ORDER BY id ASC")
    fun getAllBoards(): Flow<List<Board>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: Board): Long

    @Query("DELETE FROM boards WHERE id = :boardId")
    suspend fun deleteBoard(boardId: Int)

    @Query("SELECT COUNT(*) FROM boards")
    suspend fun getBoardCount(): Int
}

@Dao
interface PinItemDao {
    @Query("SELECT * FROM pin_items WHERE boardId = :boardId ORDER BY id ASC")
    fun getPinsForBoard(boardId: Int): Flow<List<PinItem>>

    @Query("SELECT * FROM pin_items WHERE boardId = 0 ORDER BY id ASC")
    fun getAllInspirationPins(): Flow<List<PinItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pinItem: PinItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPins(pins: List<PinItem>)

    @Update
    suspend fun updatePin(pinItem: PinItem)

    @Query("DELETE FROM pin_items WHERE id = :pinId")
    suspend fun deletePin(pinId: Int)
}

@Database(entities = [Board::class, PinItem::class], version = 3, exportSchema = false)
abstract class SanctuaryDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun pinItemDao(): PinItemDao

    companion object {
        @Volatile
        private var INSTANCE: SanctuaryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SanctuaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SanctuaryDatabase::class.java,
                    "sanctuary_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(boardDao: BoardDao, pinItemDao: PinItemDao) {
            // 1. Initial Boards
            val activeId = boardDao.insertBoard(
                Board(
                    id = 1,
                    title = "2026 Intentions",
                    description = "A collection of focused intents and visual memories.",
                    category = "Active",
                    coverImageUrl = ""
                )
            ).toInt()

            boardDao.insertBoard(
                Board(
                    id = 2,
                    title = "Travel Bucket List",
                    description = "14 visual goals & 3 active destinations.",
                    category = "Explore",
                    coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAwphtq4oOqdzheetvXzJq7wa_I1j5xFMke4cOOU9-Igwa2xkj0s9mElcRULOB7waVti47-sWsMBgTwOm8h4_B8n9-NampZSQZLz5XJDKzxzNzdhTYtTjlPjqxApuZSSsQgGG8A9-7MVn9ZEXSqfgMZ2Ivy3za-Oj0UoEZ460_SD-O0O0KfFF-FDDomHlgqWidBBT4-P-sXM8vpzqJSY-pUWKyMPPxolPgLVSufIJTxCwLAjCzQ9wOSgLwxJneJmeSImOLGlK-Lg14"
                )
            )

            boardDao.insertBoard(
                Board(
                    id = 3,
                    title = "Dream Studio",
                    description = "Defining the physical space for artistic focus.",
                    category = "Creative",
                    coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBUHBcS0fAXc42yYFcPclzV1o0ObHNB8fkuV4y-8Ijo6VH9s-LaGXJSiG8Z-44ztXn9PJnqTg3W2QO95PtDCCBPQ-x8xVs-6WdJHpDzsuSxx25BoIQpYxFvPw8fGN1zieNLXtgCdfloqehEsn_uFbuhgwJPVT_FeiEYzvKZoFpukpaJTSZiHmaNCfbBqG6c7HVbbIoNQ8H-SiJFUOSTFysRt8IMPOsmIfMogvKRkEPb4Wf9dGvIqY1Myn7VckBdbobBIFXJb4Z34WM"
                )
            )

            boardDao.insertBoard(
                Board(
                    id = 4,
                    title = "Healthy Habits",
                    description = "Visual reminders for daily mindfulness and vitality.",
                    category = "Growth",
                    coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCO8X_ZU_6YrYgicttjZ4HU4V7BjtO9sLWNqOToLKJF14n_Vh1ZqgIjhi0qjWRPPA4PIPWbFHFzwFTAvu0JAO24ain2dPIfBYSOSjV5A3OkWmeSHpX5qt8rqrDSA8hTTfVquNMEBWucl6QODTYTjy0C3peuh--gUAYSPAsVTcjNYJcK_Ap5XBAPzDafdf9CGAAhPGorYiRAsu19m9cEsD2k9z3t-ZXvqHkjJa3Op3TI4skIkuUEtT8jl3lsaqISL4b6WqeB3MhCpbM"
                )
            )

            boardDao.insertBoard(
                Board(
                    id = 5,
                    title = "Career Vision",
                    description = "The path towards intentional leadership.",
                    category = "Career",
                    coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDQWcSotZ3T5J31o8Ga-ws6iswljvZabMEymlF4O3ihXa6wA6xlDEtjoMWt0g33LqHuHMklqLM7fFPUL8UvAbX2nmMA3840fHZZ-FP2hh05_hNFxiZWVKuHfzPfKHxAwYcidpvvgCp4Nu9mIIvJbF68BNgagrEgqA6y6FT9_vMhyLPyDF2vTKs7KAAWYQ_-CzHcvEEJjIXyJmEMTMqVgls2l0qUUNYUT_qFAyQEcDQ-chQJG6Wfr1yCsJeZcrGnO6Zg69s75KyxtSE"
                )
            )

            // 2. Active Board (id=1) Pinned Cards
            pinItemDao.insertPins(
                listOf(
                    PinItem(
                        boardId = 1,
                        type = "IMAGE",
                        title = "Peak Serenity",
                        bodyText = "The anchor of my daily peace. A reminder to stay grounded yet reach for higher heights.",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBebG2Ugex3AA9DefmblrVBghWIYK1SNjIzE4UhAnTSCQx_q9j8e0s-9VNo-5eGdCB23jDSE5Y1ApWQQxM-nd5mfVKEoarMvwEJXP6J_tpUeDMh7vKSKXzm1odyMWaj9QnsZn7WC6rxmKJaHcZchohpV8H92cmQbJhhtHwo6XOL1MCvPXM9GSbS3C-lpsCzvs0qfaH98lzPUIgDilAKIpYva-qT5jGVK9T1lWlV9mMXrB8N8aMoEaD-boUHhVaGWkD-fqQs4HqR0ko",
                        posX = 40f,
                        posY = 60f,
                        rotation = -1.8f,
                        isPinned = true
                    ),
                    PinItem(
                        boardId = 1,
                        type = "NOTE",
                        title = "Word of the Year",
                        bodyText = "Finding quiet in the noise.",
                        subtitle = "Serenity",
                        fontSerif = true,
                        isItalic = true,
                        posX = 420f,
                        posY = 100f,
                        rotation = 1.2f,
                        isPinned = false,
                        bgColor = "cream"
                    ),
                    PinItem(
                        boardId = 1,
                        type = "IMAGE",
                        title = "Cozy Space",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBnQfv__yru0yuSn0oOc76D1KSBOgcJ3x-F0yWSOm6UXRPhv7_MOtumLu5X8Y1Nhfkg0igQpq1Idtw6tfLktiQXlO-FZIp1vSgP2i_f6EKSSlLCoxXdIv43nDNPXPmcBHQ1GqN44V4KAdn2MB0okFR0Ar9Ju3EatKdZJnLAcId9SNj1cCpgMELD3MqiBmEKvxE3ZhjrMMiEODKHDt7_c_ZFjWPJvj0IPB6bQ7WXUjlvGqhYl3g1NqtEdWxHuI2Qg63e2srb2xjMOVg",
                        tag = "Environment",
                        posX = 800f,
                        posY = 40f,
                        rotation = -1.0f,
                        isPinned = true
                    ),
                    PinItem(
                        boardId = 1,
                        type = "QUOTE",
                        title = "Inspiration",
                        bodyText = "\"The future belongs to those who believe in the beauty of their dreams.\"",
                        subtitle = "Eleanor Roosevelt",
                        bgColor = "navy",
                        posX = 400f,
                        posY = 420f,
                        rotation = -2.2f,
                        isPinned = true
                    ),
                    PinItem(
                        boardId = 1,
                        type = "IMAGE",
                        title = "Lush Sanctuary",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC0mGB6VM3HqduPPn9wghbOwpD1rwz0wG2p1k05EYPr5kUDDPL1obHXf20Cybi1WjS2ikVFfkkJIInuMgQE0FW7gAAvo2wXCDB0FadGtjzFDIhQQz4c-tWPxXeO4LaIR1YHr9MQoJPPFAlI9HJbKm5uca4M7WdMmN5Pe4CoWpsIojw-tNncFWhshV7AassD0ekhXf-CYwJ0Mmv9rJm-wWqSfiG9mCSmMz2nVn2Qiza10hl1yHQpSPhFv4y4jLwkeAGvin7IVqbeF0c",
                        posX = 40f,
                        posY = 480f,
                        rotation = 2.4f,
                        isPinned = true
                    ),
                    PinItem(
                        boardId = 1,
                        type = "READING_LIST",
                        title = "Reading List",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBZJh5kbcDZVN1jlgJy-dHeWNoNVQOHLd5Hi4xpvpUO8QqlTunr6uONZljPYvs06cyIyVgG_7wQZ3AeI3q2lTe9XcjGFsTOKVpyXrtpxfPyq445cc559rr5TyouBNriEzjwJBjjYnadPbDmMrErLKGj5t9vu2wre70EwWvrNCUW3cJuEviqhpUc3B_18dY4v0mUO0TOp56Kuomfc6B-dRGyYc5LQ20oDZ_SFZqULK7PLUW1p7F7qyzd8zeED9T_smpfuj5s-7CW8xQ",
                        itemsList = "Digital Minimalism,The Art of Stillness,Walden (Thoreau)",
                        completedList = "",
                        posX = 820f,
                        posY = 480f,
                        rotation = 1.0f,
                        isPinned = true
                    )
                )
            )

            // 3. Curated Inspiration Feed items (boardId = 0)
            pinItemDao.insertPins(
                listOf(
                    PinItem(
                        boardId = 0,
                        type = "IMAGE",
                        title = "Architecture of Thought",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCJB49lfajNPV18hyX8fVrUZclTODP7K_vQxxSoCkZX8ghUNfj8hk3998vMdqyhu_urhsvJbNUywgPEtn0vLO1AoJFp9ACvEbUv5NIm5vjI5GNDoryILCxXieCK0SQrDzjlfYQdbnS2HYyaJytw21xSkY6EDO5lrAHPfO8pnl4ewvrGAAFHI0WPgfHCY5VW1F-TRsyBwQyo1tWx3Sy-ge6K2dTMzYlDf2uAYIiV1B7NDkGosyLdRzI14lJIv6a2OIb_hWmipYM-gE8",
                        tag = "Minimalism",
                        isPinned = true
                    ),
                    PinItem(
                        boardId = 0,
                        type = "QUOTE",
                        title = "Silence",
                        bodyText = "\"The soul usually knows what to do to heal itself. The challenge is to silence the mind.\"",
                        subtitle = "Caroline Myss",
                        tag = "Minimalism",
                        bgColor = "navy"
                    ),
                    PinItem(
                        boardId = 0,
                        type = "IMAGE",
                        title = "Tactile Peace",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAmMmBKwG4i_yRpflWaRZnvtNYg8m3r9ozybigvG0eTxLC54MhTbD1CY6k77Umz7uWxGPVa-lIcER39XzBuwSJJcHKyyWPFWJrCFfu-NlFbNAW6H4insnf0GPJY1LDJuXalZNUa0x9b50m7quv1yjxod9Sp_R652JbhL_lalBrtbnSsPkv4vuv8zEeLtJBVpL4QkpGcAERNDCqkyvtMh3cL0CoOLX-LAojQ20pXqsrG0lFRcOH5C5dX0aqDs2JFYM0A0w4zIfuseUk",
                        tag = "Urban Calm",
                        isPinned = false
                    ),
                    PinItem(
                        boardId = 0,
                        type = "HABIT",
                        title = "Mindful Habits",
                        itemsList = "10m Digital Silence,Morning Journaling,One Intentional Meal",
                        completedList = "10m Digital Silence",
                        tag = "Growth"
                    ),
                    PinItem(
                        boardId = 0,
                        type = "IMAGE",
                        title = "Vast Openness",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCui8HSq6lw4zJG2p-p5ZAARuMpuKvXYo3_horcuz1MX53RyxzM3vTOZvhKzVrhYGLKN_aSVosY518KlVKV5OVWjOB4zVMG6PEeunnn1Bhoy7cH_MlnHYGAs0oOSQ98LcoWPZw6QTpg-xDFqRn-0oH61CdrkB8330CBe9RdWk-4lHm7NywUbHciPwo96YR_8wJdN1syzg6hpC0-CwRbNaay8ms4BU0bD57pwWiiR7L5PrYngA4RXXo-_n0tyWzc_KtK9CVM3fwlTb0",
                        tag = "Nature",
                        isPinned = false
                    ),
                    PinItem(
                        boardId = 0,
                        type = "IMAGE",
                        title = "Grounding Roots",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAT7vpFFnOM0iQxdLH0ZQHMD1_C0K59BmzKY6JoiryGE4KNAGZR8cVh9uTJYKSVqDq-pDXwVRe1rl4UtPMvyAdgCgILBLQFYEbWWU0vmGxMdI2bRIuIgOI6cdcOOiAEm_GbLwXPPzI-s6-hEv4MOiAkLXpuSY4yG158GCgb3qJHjPUNGuC20KT51Xy4Ei5Nak9j_gEOyuAkStlPM7C1riVlJuHZaQIjCswGvMHSQBPkb187Rm8vV21kHtQs7l6Kn3PKeQWTROsPHRw",
                        tag = "Nature",
                        isPinned = false
                    ),
                    PinItem(
                        boardId = 0,
                        type = "IMAGE",
                        title = "Abstract Fluidity",
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAHTp4j2XwPk_rone-vmKrAyVZqm6aNM8IcC645Geolb6MqHFhdxCnHtuW2p2g8KsA6cKObKLXYZHC7iCWt1xa6NLUmg-i9l3Ae_OOYd_pnTWeArItJA6qihmsOgn6Qj5WiDJ49cwqHqAlDLg2_lNLqqa3iwV3DOiq0UxZ1HUKJloyB_V83jNTzjFqzL1_avgg5DA8dcldsN7GnAc1B3R_AeL3p0v-h4opgq49h9PCt-9VtbueA-UMfMUgQNyQ9Z9Rd018U3ZF6Fdc",
                        tag = "Minimalism",
                        isPinned = false
                    )
                )
            )
        }
    }
}
