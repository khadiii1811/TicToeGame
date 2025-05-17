package com.example.tictoe.utils

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)

    @Update
    suspend fun updateUserStats(userStats: UserStats)

    @Query("SELECT * FROM userstats WHERE id = :id")
    suspend fun getUserStatsById(id: Int): UserStats?

    @Query("SELECT * FROM userstats")
    fun getAllStats(): Flow<List<UserStats>>
}