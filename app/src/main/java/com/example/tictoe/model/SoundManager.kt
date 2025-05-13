package com.example.tictoe.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.tictoe.R

/**
 * Class quản lý các âm thanh trong ứng dụng
 */
class SoundManager(private val context: Context) {
    
    // SoundPool để quản lý và phát âm thanh ngắn
    private val soundPool: SoundPool
    
    // MediaPlayer để phát nhạc nền menu
    private var menuMusicPlayer: MediaPlayer? = null
    
    // ID của các âm thanh
    private var clickSoundId: Int = 0
    private var winSoundId: Int = 0
    private var loseSoundId: Int = 0
    private var drawSoundId: Int = 0
    
    // Trạng thái tải âm thanh
    private var soundsLoaded = false
    
    // Biến kiểm soát trạng thái âm thanh
    private var isSoundEnabled = true
    
    init {
        // Khởi tạo AudioAttributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        // Khởi tạo SoundPool
        soundPool = SoundPool.Builder()
            .setMaxStreams(4) // Số lượng âm thanh có thể phát cùng lúc
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Callback khi âm thanh được tải xong
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) { // 0 = success
                soundsLoaded = true
                Log.d("SoundManager", "Sound loaded successfully: $sampleId")
            } else {
                Log.e("SoundManager", "Failed to load sound: $sampleId, status: $status")
            }
        }
        
        // Khởi tạo MediaPlayer cho nhạc menu
        initMenuMusic()
        
        // Load âm thanh từ resources
        loadSounds()
    }
    
    /**
     * Khởi tạo MediaPlayer cho nhạc menu
     */
    private fun initMenuMusic() {
        try {
            val menuMusicRes = context.resources.getIdentifier("menu", "raw", context.packageName)
            if (menuMusicRes != 0) {
                menuMusicPlayer = MediaPlayer.create(context, menuMusicRes).apply {
                    isLooping = true // Lặp lại liên tục
                    setVolume(0.5f, 0.5f) // Âm lượng 50%
                }
                Log.d("SoundManager", "Menu music initialized successfully")
            } else {
                Log.e("SoundManager", "Could not find menu music resource")
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing menu music: ${e.message}")
        }
    }
    
    /**
     * Tải các file âm thanh từ resources
     */
    private fun loadSounds() {
        try {
            // Kiểm tra tồn tại của resource
            val clickRes = getRawResourceId("click")
            val winRes = getRawResourceId("win")
            val loseRes = getRawResourceId("lose")
            val drawRes = getRawResourceId("draw")
            
            // Tải âm thanh nếu resource tồn tại
            if (clickRes != 0) clickSoundId = soundPool.load(context, clickRes, 1)
            if (winRes != 0) winSoundId = soundPool.load(context, winRes, 1)
            if (loseRes != 0) loseSoundId = soundPool.load(context, loseRes, 1)
            if (drawRes != 0) drawSoundId = soundPool.load(context, drawRes, 1)
            
            Log.d("SoundManager", "Attempting to load sound resources")
        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading sounds: ${e.message}")
            soundsLoaded = false
        }
    }
    
    /**
     * Lấy ID của resource trong thư mục raw
     */
    private fun getRawResourceId(name: String): Int {
        val resourceId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resourceId == 0) {
            Log.w("SoundManager", "Resource not found: $name")
        }
        return resourceId
    }
    
    /**
     * Bật/tắt âm thanh
     */
    fun setSoundEnabled(enabled: Boolean) {
        val previousState = isSoundEnabled
        isSoundEnabled = enabled
        Log.d("SoundManager", "Sound enabled changed: $previousState -> $enabled")
        
        // Điều chỉnh âm thanh menu theo trạng thái
        if (enabled) {
            // If sound was just enabled, resume music
            resumeMenuMusic()
        } else {
            // If sound was just disabled, pause music
            pauseMenuMusic()
        }
    }
    
    /**
     * Phát âm thanh với ID được chỉ định
     */
    private fun playSound(soundId: Int) {
        if (isSoundEnabled && soundId != 0 && soundsLoaded) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
    
    /**
     * Phát âm thanh click
     */
    fun playClickSound() {
        playSound(clickSoundId)
        Log.d("SoundManager", "Playing click sound")
    }
    
    /**
     * Phát âm thanh chiến thắng
     */
    fun playWinSound() {
        playSound(winSoundId)
        Log.d("SoundManager", "Playing win sound")
    }
    
    /**
     * Phát âm thanh thua
     */
    fun playLoseSound() {
        playSound(loseSoundId)
        Log.d("SoundManager", "Playing lose sound")
    }
    
    /**
     * Phát âm thanh hòa
     */
    fun playDrawSound() {
        playSound(drawSoundId)
        Log.d("SoundManager", "Playing draw sound")
    }
    
    /**
     * Phát âm thanh menu
     */
    /**
     * Bắt đầu phát nhạc menu
     */
    fun startMenuMusic() {
        if (isSoundEnabled && menuMusicPlayer != null) {
            try {
                if (!menuMusicPlayer!!.isPlaying) {
                    menuMusicPlayer?.start()
                    Log.d("SoundManager", "Starting menu music")
                }
            } catch (e: Exception) {
                Log.e("SoundManager", "Error starting menu music: ${e.message}")
            }
        }
    }
    
    /**
     * Tạm dừng nhạc menu
     */
    fun pauseMenuMusic() {
        try {
            if (menuMusicPlayer?.isPlaying == true) {
                menuMusicPlayer?.pause()
                Log.d("SoundManager", "Paused menu music")
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error pausing menu music: ${e.message}")
        }
    }
    
    /**
     * Tiếp tục phát nhạc menu
     */
    fun resumeMenuMusic() {
        if (isSoundEnabled && menuMusicPlayer != null) {
            try {
                if (!menuMusicPlayer!!.isPlaying) {
                    menuMusicPlayer?.start()
                    Log.d("SoundManager", "Resumed menu music")
                }
            } catch (e: Exception) {
                Log.e("SoundManager", "Error resuming menu music: ${e.message}")
            }
        }
    }
    
    /**
     * Dừng nhạc menu
     */
    fun stopMenuMusic() {
        try {
            if (menuMusicPlayer?.isPlaying == true) {
                menuMusicPlayer?.pause()
                menuMusicPlayer?.seekTo(0)
                Log.d("SoundManager", "Stopped menu music")
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error stopping menu music: ${e.message}")
        }
    }
    
    /**
     * Giải phóng resources
     */
    fun release() {
        soundPool.release()
        try {
            menuMusicPlayer?.release()
            menuMusicPlayer = null
        } catch (e: Exception) {
            Log.e("SoundManager", "Error releasing MediaPlayer: ${e.message}")
        }
        Log.d("SoundManager", "Released SoundPool and MediaPlayer resources")
    }
    
    companion object {
        // Singleton pattern
        @Volatile
        private var INSTANCE: SoundManager? = null
        
        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SoundManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
} 