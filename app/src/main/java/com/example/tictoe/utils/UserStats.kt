package com.example.tictoe.utils

import androidx.room.*

@Entity(tableName = "userstats")
data class UserStats(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0
) {
    @Ignore
    val winRate: Int = calculateWinRate()

    @Ignore
    private fun calculateWinRate(): Int {
        val totalGames = wins + draws + losses
        return if (totalGames > 0) (wins * 100) / totalGames else 0
    }
}