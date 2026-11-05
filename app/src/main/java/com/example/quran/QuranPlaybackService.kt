package com.example.quran

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.example.MainActivity
import java.io.FileInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Native Android Foreground Playback Service for the Holy Quran.
 * Supports:
 * - MediaSessionCompat & PlaybackStateCompat (Native MIUI/HyperOS top drawer & lockscreen player)
 * - androidx.media.app.NotificationCompat.MediaStyle with direct notification playback controls
 * - Partial wake lock for seamless screen-off AMOLED background playback
 * - Audio focus management (ducking, transient pauses, incoming calls)
 * - Repeat modes (OFF, ONE, ALL)
 * - Sleep timer (15, 30, 45, 60 min, end of surah)
 * - Automatic offline file vs online CDN streaming fallback
 */
class QuranPlaybackService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "quran_playback_channel"

        const val ACTION_PLAY = "com.example.quran.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.quran.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY = "com.example.quran.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT = "com.example.quran.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.quran.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.quran.ACTION_STOP"
        const val ACTION_SEEK_TO = "com.example.quran.ACTION_SEEK_TO"
        const val ACTION_SET_REPEAT = "com.example.quran.ACTION_SET_REPEAT"
        const val ACTION_SET_SLEEP_TIMER = "com.example.quran.ACTION_SET_SLEEP_TIMER"

        const val EXTRA_RECITER_ID = "EXTRA_RECITER_ID"
        const val EXTRA_SURAH_NUMBER = "EXTRA_SURAH_NUMBER"
        const val EXTRA_SEEK_POSITION = "EXTRA_SEEK_POSITION"
        const val EXTRA_REPEAT_MODE = "EXTRA_REPEAT_MODE"
        const val EXTRA_SLEEP_MINUTES = "EXTRA_SLEEP_MINUTES"

        private val _playerState = MutableStateFlow(QuranPlayerState())
        val playerState: StateFlow<QuranPlayerState> = _playerState.asStateFlow()

        private val _playbackPositionMs = MutableStateFlow(0L)
        val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

        fun playSurah(context: Context, reciter: Reciter, surah: Surah) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_RECITER_ID, reciter.id)
                putExtra(EXTRA_SURAH_NUMBER, surah.number)
            }
            startServiceCompat(context, intent)
        }

        fun togglePlayPause(context: Context) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_TOGGLE_PLAY
            }
            startServiceCompat(context, intent)
        }

        fun skipNext(context: Context) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_NEXT
            }
            startServiceCompat(context, intent)
        }

        fun skipPrevious(context: Context) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_PREVIOUS
            }
            startServiceCompat(context, intent)
        }

        fun seekTo(context: Context, positionMs: Long) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_SEEK_TO
                putExtra(EXTRA_SEEK_POSITION, positionMs)
            }
            startServiceCompat(context, intent)
        }

        fun setRepeatMode(context: Context, repeatMode: RepeatMode) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_SET_REPEAT
                putExtra(EXTRA_REPEAT_MODE, repeatMode.name)
            }
            startServiceCompat(context, intent)
        }

        fun setSleepTimer(context: Context, duration: SleepTimerDuration?) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_SET_SLEEP_TIMER
                putExtra(EXTRA_SLEEP_MINUTES, duration?.minutes ?: -1)
            }
            startServiceCompat(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            startServiceCompat(context, intent)
        }

        private fun startServiceCompat(context: Context, intent: Intent) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore background start exceptions
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var resumeOnFocusGain = false
    private var isNoisyReceiverRegistered = false

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pausePlayback()
            }
        }
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            try {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                registerReceiver(noisyReceiver, filter)
                isNoisyReceiverRegistered = true
            } catch (e: Exception) {
                // Ignore receiver registration error
            }
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(noisyReceiver)
            } catch (e: Exception) {
                // Ignore
            }
            isNoisyReceiverRegistered = false
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressTrackingJob: Job? = null
    private var sleepTimerJob: Job? = null

    private var cachedAlbumArt: Bitmap? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        // WakeLock for screen-off playback on Xiaomi Poco F4 / HyperOS AMOLED
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlMuhasib:QuranWakeLock")?.apply {
            setReferenceCounted(false)
        }

        initMediaSession()
        createNotificationChannel()
        cachedAlbumArt = generateQuranEmblemBitmap()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "QuranPlaybackSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    resumePlayback()
                }

                override fun onPause() {
                    pausePlayback()
                }

                override fun onSkipToNext() {
                    handleSkipNext()
                }

                override fun onSkipToPrevious() {
                    handleSkipPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    performSeek(pos)
                }

                override fun onStop() {
                    stopPlayback()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        when (intent?.action) {
            ACTION_PLAY -> {
                val reciterId = intent.getIntExtra(EXTRA_RECITER_ID, _playerState.value.currentReciter.id)
                val surahNumber = intent.getIntExtra(EXTRA_SURAH_NUMBER, _playerState.value.currentSurah?.number ?: 1)
                val reciter = ReciterCatalog.getReciterById(reciterId)
                val surah = SurahCatalog.getSurahByNumber(surahNumber) ?: SurahCatalog.SURAHS[0]
                startPlaybackForSurah(reciter, surah)
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_TOGGLE_PLAY -> {
                if (_playerState.value.isPlaying) {
                    pausePlayback()
                } else {
                    resumePlayback()
                }
            }
            ACTION_NEXT -> handleSkipNext()
            ACTION_PREVIOUS -> handleSkipPrevious()
            ACTION_SEEK_TO -> {
                val pos = intent.getLongExtra(EXTRA_SEEK_POSITION, 0L)
                performSeek(pos)
            }
            ACTION_SET_REPEAT -> {
                val modeName = intent.getStringExtra(EXTRA_REPEAT_MODE)
                val mode = RepeatMode.entries.find { it.name == modeName } ?: RepeatMode.OFF
                _playerState.update { it.copy(repeatMode = mode) }
            }
            ACTION_SET_SLEEP_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_SLEEP_MINUTES, -1)
                handleSleepTimerConfig(minutes)
            }
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    private fun startPlaybackForSurah(reciter: Reciter, surah: Surah) {
        if (!requestAudioFocus()) return

        resumeOnFocusGain = false
        registerNoisyReceiver()
        wakeLock?.acquire(3 * 60 * 60 * 1000L) // 3-hour max safe hold
        _playerState.update {
            it.copy(
                currentReciter = reciter,
                currentSurah = surah,
                isPlaying = false,
                isBuffering = true,
                currentPositionMs = 0L,
                durationMs = 0L,
                errorMessage = null
            )
        }

        updateNotification()

        mediaPlayer?.release()
        mediaPlayer = null

        val downloadManager = QuranDownloadManager.getInstance(this)
        val localFile = downloadManager.getLocalSurahFile(reciter.id, surah.number)
        val isOffline = localFile != null

        _playerState.update { it.copy(isLocalFile = isOffline) }

        try {
            val player = MediaPlayer()
            mediaPlayer = player

            player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            if (localFile != null && localFile.exists()) {
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(localFile)
                    player.setDataSource(fis.fd)
                } catch (e: Exception) {
                    // Fall back to remote stream if local file descriptor fails
                    val remoteUrl = ReciterCatalog.getSurahAudioUrl(reciter, surah.number)
                    player.reset()
                    player.setDataSource(remoteUrl)
                    _playerState.update { it.copy(isLocalFile = false) }
                } finally {
                    fis?.close()
                }
            } else {
                val remoteUrl = ReciterCatalog.getSurahAudioUrl(reciter, surah.number)
                player.setDataSource(remoteUrl)
            }

            player.setOnPreparedListener { mp ->
                mp.start()
                val duration = mp.duration.toLong().coerceAtLeast(0L)
                _playerState.update {
                    it.copy(
                        isPlaying = true,
                        isBuffering = false,
                        durationMs = duration,
                        currentPositionMs = 0L
                    )
                }
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                updateMediaMetadata(reciter, surah, duration)
                updateNotification()
                startProgressTracker()
            }

            player.setOnCompletionListener {
                handleTrackCompletion()
            }

            player.setOnErrorListener { _, what, extra ->
                _playerState.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "تعذر تشغيل السورة (رمز $what:$extra)"
                    )
                }
                updatePlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
                updateNotification()
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            _playerState.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = e.localizedMessage ?: "حدث خطأ أثناء تشغيل التلاوة"
                )
            }
            updatePlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
            updateNotification()
        }
    }

    private fun resumePlayback() {
        resumeOnFocusGain = false
        registerNoisyReceiver()
        val player = mediaPlayer
        if (player != null && !player.isPlaying) {
            if (!requestAudioFocus()) return
            wakeLock?.acquire(3 * 60 * 60 * 1000L)
            player.start()
            _playerState.update { it.copy(isPlaying = true) }
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition.toLong())
            updateNotification()
            startProgressTracker()
        } else if (_playerState.value.currentSurah != null) {
            val reciter = _playerState.value.currentReciter
            val surah = _playerState.value.currentSurah ?: SurahCatalog.SURAHS[0]
            startPlaybackForSurah(reciter, surah)
        }
    }

    private fun pausePlayback(dueToTransientLoss: Boolean = false) {
        if (!dueToTransientLoss) {
            resumeOnFocusGain = false
        }
        unregisterNoisyReceiver()
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }
        val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
        _playbackPositionMs.value = pos
        _playerState.update { it.copy(isPlaying = false, currentPositionMs = pos) }
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, pos)
        stopProgressTracker()
        updateNotification()
        releaseWakeLock()
    }

    private fun handleSkipNext() {
        val currentSurah = _playerState.value.currentSurah ?: return
        val currentReciter = _playerState.value.currentReciter
        val nextNumber = if (currentSurah.number >= 114) {
            if (_playerState.value.repeatMode == RepeatMode.ALL) 1 else return
        } else {
            currentSurah.number + 1
        }
        val nextSurah = SurahCatalog.getSurahByNumber(nextNumber) ?: return
        startPlaybackForSurah(currentReciter, nextSurah)
    }

    private fun handleSkipPrevious() {
        val player = mediaPlayer
        if (player != null && player.currentPosition > 5000) {
            // If already played more than 5s, restart current track
            performSeek(0L)
            return
        }
        val currentSurah = _playerState.value.currentSurah ?: return
        val currentReciter = _playerState.value.currentReciter
        val prevNumber = if (currentSurah.number <= 1) 114 else currentSurah.number - 1
        val prevSurah = SurahCatalog.getSurahByNumber(prevNumber) ?: return
        startPlaybackForSurah(currentReciter, prevSurah)
    }

    private fun performSeek(pos: Long) {
        val player = mediaPlayer ?: return
        try {
            player.seekTo(pos.toInt())
            _playbackPositionMs.value = pos
            _playerState.update { it.copy(currentPositionMs = pos) }
            val state = if (player.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            updatePlaybackState(state, pos)
        } catch (e: Exception) {
            // Ignore seek errors
        }
    }

    private fun handleTrackCompletion() {
        val currentState = _playerState.value

        // Check Sleep Timer "End of Surah" condition
        if (currentState.sleepAtEndOfSurah) {
            _playerState.update {
                it.copy(
                    sleepAtEndOfSurah = false,
                    isSleepTimerActive = false,
                    sleepTimerSecondsRemaining = null
                )
            }
            pausePlayback()
            return
        }

        when (currentState.repeatMode) {
            RepeatMode.ONE -> {
                val reciter = currentState.currentReciter
                val surah = currentState.currentSurah ?: SurahCatalog.SURAHS[0]
                startPlaybackForSurah(reciter, surah)
            }
            RepeatMode.ALL -> {
                handleSkipNext()
            }
            RepeatMode.OFF -> {
                val surah = currentState.currentSurah
                if (surah != null && surah.number < 114) {
                    handleSkipNext()
                } else {
                    pausePlayback()
                }
            }
        }
    }

    private fun handleSleepTimerConfig(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes < 0) {
            // Cancel timer
            _playerState.update {
                it.copy(
                    isSleepTimerActive = false,
                    sleepTimerMinutesLeft = null,
                    sleepTimerSecondsRemaining = null,
                    sleepAtEndOfSurah = false
                )
            }
            return
        }

        if (minutes == 0) {
            // End of surah
            _playerState.update {
                it.copy(
                    isSleepTimerActive = true,
                    sleepAtEndOfSurah = true,
                    sleepTimerMinutesLeft = 0,
                    sleepTimerSecondsRemaining = null
                )
            }
            return
        }

        val totalSeconds = minutes * 60L
        _playerState.update {
            it.copy(
                isSleepTimerActive = true,
                sleepAtEndOfSurah = false,
                sleepTimerMinutesLeft = minutes,
                sleepTimerSecondsRemaining = totalSeconds
            )
        }

        sleepTimerJob = serviceScope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _playerState.update {
                    it.copy(
                        sleepTimerSecondsRemaining = remaining,
                        sleepTimerMinutesLeft = ((remaining + 59) / 60).toInt()
                    )
                }
            }
            // Sleep timer finished: pause playback
            _playerState.update {
                it.copy(
                    isSleepTimerActive = false,
                    sleepTimerSecondsRemaining = null,
                    sleepTimerMinutesLeft = null
                )
            }
            pausePlayback()
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = serviceScope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    val dur = player.duration.toLong().coerceAtLeast(0L)
                    _playbackPositionMs.value = pos
                    if (dur > 0 && dur != _playerState.value.durationMs) {
                        _playerState.update { it.copy(durationMs = dur) }
                    }
                }
                delay(200L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    private fun stopPlayback() {
        resumeOnFocusGain = false
        unregisterNoisyReceiver()
        stopProgressTracker()
        sleepTimerJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        _playbackPositionMs.value = 0L
        _playerState.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = 0L
            )
        }

        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
        releaseWakeLock()
        abandonAudioFocus()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updatePlaybackState(state: Int, position: Long) {
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP

        val playbackState = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, position, 1.0f)
            .build()

        mediaSession?.setPlaybackState(playbackState)
    }

    private fun updateMediaMetadata(reciter: Reciter, surah: Surah, durationMs: Long) {
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "سورة ${surah.nameArabic} (${surah.nameEnglish})")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciter.nameArabic)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "القرآن الكريم - ${reciter.rewayaArabic}")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)

        cachedAlbumArt?.let {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, it)
        }

        mediaSession?.setMetadata(metadataBuilder.build())
    }

    private fun buildNotification(): Notification {
        val state = _playerState.value
        val surah = state.currentSurah ?: SurahCatalog.SURAHS[0]
        val reciter = state.currentReciter

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, QuranPlaybackService::class.java).setAction(ACTION_PREVIOUS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val togglePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, QuranPlaybackService::class.java).setAction(ACTION_TOGGLE_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, QuranPlaybackService::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (state.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل"

        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val offlineBadge = if (state.isLocalFile) " • بدون إنترنت" else " • بث مباشر"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("سورة ${surah.nameArabic} - ${surah.nameEnglish}")
            .setContentText("${reciter.nameArabic}$offlineBadge")
            .setSubText("القرآن الكريم")
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(state.isPlaying)
            .setStyle(mediaStyle)
            .addAction(android.R.drawable.ic_media_previous, "السابق", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "التالي", nextPendingIntent)

        cachedAlbumArt?.let {
            notificationBuilder.setLargeIcon(it)
        }

        return notificationBuilder.build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تشغيل القرآن الكريم"
            val desc = "إشعارات التحكم بتشغيل سور القرآن الكريم والخلفية"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(this)
                .setAcceptsDelayedFocusGain(true)
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnFocusGain = false
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                val wasPlaying = _playerState.value.isPlaying
                resumeOnFocusGain = wasPlaying
                pausePlayback(dueToTransientLoss = true)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (resumeOnFocusGain) {
                    resumeOnFocusGain = false
                    resumePlayback()
                }
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun generateQuranEmblemBitmap(): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Deep emerald background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#062419")
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

        // Gold decorative ring
        val ringPaint = Paint().apply {
            color = Color.parseColor("#10B981")
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 10f, ringPaint)

        // Inner glowing ring
        val innerRingPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 22f, innerRingPaint)

        // Center Arabic Calligraphy / Text representation
        val textPaint = Paint().apply {
            color = Color.parseColor("#FBBF24")
            textSize = 64f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText("القرآن", size / 2f, yPos, textPaint)

        return bitmap
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!_playerState.value.isPlaying) {
            stopPlayback()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        resumeOnFocusGain = false
        unregisterNoisyReceiver()
        stopProgressTracker()
        sleepTimerJob?.cancel()
        mediaSession?.run {
            isActive = false
            release()
        }
        mediaPlayer?.release()
        mediaPlayer = null
        releaseWakeLock()
        abandonAudioFocus()
        _playerState.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = 0L,
                isSleepTimerActive = false,
                sleepTimerMinutesLeft = null,
                sleepTimerSecondsRemaining = null,
                sleepAtEndOfSurah = false
            )
        }
        super.onDestroy()
    }
}
