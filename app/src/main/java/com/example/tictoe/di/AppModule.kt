package com.example.tictoe.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.tictoe.model.OnlineGameRepository
import com.example.tictoe.model.StatsRepository
import com.example.tictoe.model.SoundManager
import com.example.tictoe.viewmodel.GameViewModel
import com.example.tictoe.viewmodel.LANViewModel
import com.example.tictoe.viewmodel.MenuViewModel
import com.example.tictoe.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Simple dependency injection helper to provide ViewModels and Repositories
 * In a real-world app, you'd use Hilt or Koin for proper DI
 */
object AppModule {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Provides StatsRepository instance
     */
    fun provideStatsRepository(context: Context): StatsRepository {
        return StatsRepository.getInstance(context)
    }
    
    /**
     * Provides SoundManager instance
     */
    fun provideSoundManager(context: Context): SoundManager {
        return SoundManager.getInstance(context)
    }
    
    /**
     * Provides OnlineGameRepository instance
     */
    fun provideOnlineGameRepository(context: Context): OnlineGameRepository {
        return OnlineGameRepository(context, applicationScope)
    }
    
    /**
     * Provides GameViewModel instance
     */
    fun provideGameViewModel(owner: ViewModelStoreOwner): GameViewModel {
        return ViewModelProvider(owner)[GameViewModel::class.java]
    }
    
    /**
     * Provides MenuViewModel instance
     */
    fun provideMenuViewModel(owner: ViewModelStoreOwner): MenuViewModel {
        return ViewModelProvider(owner)[MenuViewModel::class.java]
    }
    
    /**
     * Provides SettingsViewModel instance
     */
    fun provideSettingsViewModel(owner: ViewModelStoreOwner): SettingsViewModel {
        return ViewModelProvider(owner)[SettingsViewModel::class.java]
    }

    /**
     * Provides LANViewModel instance
     */
    fun provideLANViewModel(owner: ViewModelStoreOwner): LANViewModel {
        return ViewModelProvider(owner)[LANViewModel::class.java]
    }
} 