package com.example.ui.viewmodel

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MuhasibApplication
import com.example.alarm.PrayerAdhanPlayer
import com.example.alarm.PrayerAlarmReceiver
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerWarningActivity
import com.example.aod.AodManager
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.SobrietyLog
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.model.AppTheme
import com.example.data.repository.OnboardingPreferences
import com.example.util.ThemePreferences
import java.time.LocalDate

data class MuhasibUiState(
    val currentTheme: AppTheme = AppTheme.DEFAULT,
    val selectedDhikrCategory: DhikrCategory = DhikrCategory.MORNING,
    val showPanicSheet: Boolean = false,
    val showOnboardingDialog: Boolean = false,
    val isAodEnabled: Boolean = true,
    val isBurnInProtectionEnabled: Boolean = true,
    val aodBrightness: Float = 0.06f,
    val isFloatingDhikrEnabled: Boolean = false,
    val isAutoBrightnessEnabled: Boolean = true,
    val aodDisplayMode: Int = AodManager.MODE_ALWAYS_ON,
    val isBatterySaverCutoffEnabled: Boolean = false,
    val misbahaHapticProfile: Int = AodManager.HAPTIC_MECHANICAL,
    val isPocketModeEnabled: Boolean = true,
    val aodClockStyle: Int = AodManager.CLOCK_DIGITAL_MINIMAL,
    val aodAccentColor: Int = AodManager.ACCENT_EMERALD,
    val aodBatteryStyle: Int = AodManager.BATTERY_PERCENTAGE,
    val isNotificationPrivacy: Boolean = false
)

class MuhasibViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MuhasibApplication).repository

    private val _uiState = MutableStateFlow(
        MuhasibUiState(
            currentTheme = ThemePreferences.getTheme(application),
            showOnboardingDialog = !OnboardingPreferences.isOnboardingCompleted(application),
            isAodEnabled = AodManager.isAodEnabled(application),
            isBurnInProtectionEnabled = AodManager.isBurnInProtectionEnabled(application),
            aodBrightness = AodManager.getScreenBrightness(application),
            isFloatingDhikrEnabled = AodManager.isFloatingDhikrEnabled(application),
            isAutoBrightnessEnabled = AodManager.isAutoBrightnessEnabled(application),
            aodDisplayMode = AodManager.getAodDisplayMode(application),
            isBatterySaverCutoffEnabled = AodManager.isBatterySaverDisableEnabled(application),
            misbahaHapticProfile = AodManager.getMisbahaHapticProfile(application),
            isPocketModeEnabled = AodManager.isPocketModeEnabled(application),
            aodClockStyle = AodManager.getClockStyle(application),
            aodAccentColor = AodManager.getAccentColor(application),
            aodBatteryStyle = AodManager.getBatteryStyle(application),
            isNotificationPrivacy = AodManager.isNotificationPrivacyEnabled(application)
        )
    )
    val uiState: StateFlow<MuhasibUiState> = _uiState.asStateFlow()

    // ponytail: reactive StateFlows reflecting real-time Adhan playback
    val isAdhanPlaying: StateFlow<Boolean> = PrayerAdhanPlayer.isPlaying
    val currentAdhanPrayer: StateFlow<String?> = PrayerAdhanPlayer.currentAlarmPrayer

    val todayPrayerLog: StateFlow<PrayerLog> = repository.getTodayPrayerLog()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PrayerLog(dateString = LocalDate.now().toString())
        )

    val sobrietyState: StateFlow<SobrietyLog> = repository.getSobrietyState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SobrietyLog()
        )

    val allDhikr: StateFlow<List<DhikrItem>> = repository.allDhikr
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val muhasabahHistory: StateFlow<List<MuhasabahScore>> = repository.getMuhasabahHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Arm next prayer alarm and start AOD service if enabled
        PrayerAlarmScheduler.scheduleNextPrayerThresholdAlarm(application)
        if (AodManager.isAodEnabled(application)) {
            AodManager.startAodLockService(application)
        }
        WidgetUpdateHelper.updateAllWidgets(application)
    }

    fun setSelectedDhikrCategory(category: DhikrCategory) {
        _uiState.value = _uiState.value.copy(selectedDhikrCategory = category)
    }

    fun setPanicSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showPanicSheet = visible)
    }

    fun completeOnboarding() {
        OnboardingPreferences.setOnboardingCompleted(getApplication(), true)
        _uiState.value = _uiState.value.copy(showOnboardingDialog = false)
    }

    fun replayOnboarding() {
        _uiState.value = _uiState.value.copy(showOnboardingDialog = true)
    }

    fun setOnboardingDialogVisible(visible: Boolean) {
        if (!visible) {
            completeOnboarding()
        } else {
            _uiState.value = _uiState.value.copy(showOnboardingDialog = true)
        }
    }

    fun togglePrayer(prayerName: String, done: Boolean) {
        viewModelScope.launch {
            repository.togglePrayer(prayerName, done)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun incrementDhikr(id: Int) {
        viewModelScope.launch {
            repository.incrementDhikr(id)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun resetAllDhikr() {
        viewModelScope.launch {
            repository.resetAllDhikr()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun incrementIstighfar() {
        viewModelScope.launch {
            repository.incrementIstighfarCount()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    // ponytail: tasbih bead counter — completely separate from istighfar
    fun incrementTasbi7() {
        viewModelScope.launch {
            repository.incrementTasbi7Count()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun incrementSubhanAllah() {
        viewModelScope.launch {
            repository.incrementSubhanAllahCount()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun incrementAlhamdulillah() {
        viewModelScope.launch {
            repository.incrementAlhamdulillahCount()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun incrementAllahuAkbar() {
        viewModelScope.launch {
            repository.incrementAllahuAkbarCount()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun incrementLaIlahaIllallah() {
        viewModelScope.launch {
            repository.incrementLaIlahaIllallahCount()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun toggleWorkout(done: Boolean) {
        viewModelScope.launch {
            repository.toggleWorkout(done)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun toggleColdShower(done: Boolean) {
        viewModelScope.launch {
            repository.toggleColdShower(done)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun recordRelapse() {
        viewModelScope.launch {
            repository.recordRelapse()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun setAodEnabled(enabled: Boolean) {
        AodManager.setAodEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isAodEnabled = enabled)
    }

    fun setBurnInProtectionEnabled(enabled: Boolean) {
        AodManager.setBurnInProtectionEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isBurnInProtectionEnabled = enabled)
    }

    fun setAodBrightness(brightness: Float) {
        AodManager.setScreenBrightness(getApplication(), brightness)
        _uiState.value = _uiState.value.copy(aodBrightness = brightness)
    }

    fun setFloatingDhikrEnabled(enabled: Boolean) {
        AodManager.setFloatingDhikrEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isFloatingDhikrEnabled = enabled)
    }

    fun setAutoBrightnessEnabled(enabled: Boolean) {
        AodManager.setAutoBrightnessEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isAutoBrightnessEnabled = enabled)
    }

    fun setAodDisplayMode(mode: Int) {
        AodManager.setAodDisplayMode(getApplication(), mode)
        _uiState.value = _uiState.value.copy(aodDisplayMode = mode)
    }

    fun setBatterySaverCutoffEnabled(enabled: Boolean) {
        AodManager.setBatterySaverDisableEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isBatterySaverCutoffEnabled = enabled)
    }

    fun setMisbahaHapticProfile(profile: Int) {
        AodManager.setMisbahaHapticProfile(getApplication(), profile)
        _uiState.value = _uiState.value.copy(misbahaHapticProfile = profile)
    }

    fun setPocketModeEnabled(enabled: Boolean) {
        AodManager.setPocketModeEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isPocketModeEnabled = enabled)
    }

    fun setAppTheme(theme: AppTheme) {
        ThemePreferences.setTheme(getApplication(), theme)
        _uiState.value = _uiState.value.copy(currentTheme = theme)
    }

    fun setAodClockStyle(style: Int) {
        AodManager.setClockStyle(getApplication(), style)
        _uiState.value = _uiState.value.copy(aodClockStyle = style)
    }

    fun setAodAccentColor(color: Int) {
        AodManager.setAccentColor(getApplication(), color)
        _uiState.value = _uiState.value.copy(aodAccentColor = color)
    }

    fun setAodBatteryStyle(style: Int) {
        AodManager.setBatteryStyle(getApplication(), style)
        _uiState.value = _uiState.value.copy(aodBatteryStyle = style)
    }

    fun setNotificationPrivacy(enabled: Boolean) {
        AodManager.setNotificationPrivacyEnabled(getApplication(), enabled)
        _uiState.value = _uiState.value.copy(isNotificationPrivacy = enabled)
    }

    // ponytail: stop adhan audio, dismiss warning activity, cancel notification
    fun stopAdhan() {
        PrayerAdhanPlayer.stop()
        PrayerWarningActivity.dismissIfActive()
        try {
            NotificationManagerCompat.from(getApplication()).cancel(PrayerAlarmReceiver.NOTIFICATION_ID_PRAYER)
            NotificationManagerCompat.from(getApplication()).cancel(PrayerAlarmReceiver.NOTIFICATION_ID_PRAYER_WARN)
        } catch (e: Exception) {
            // Ignore
        }
    }

    // ponytail: snooze adhan for 10 minutes
    fun snoozeAdhan() {
        val prayer = currentAdhanPrayer.value ?: "الصلاة"
        stopAdhan()
        PrayerAlarmScheduler.schedule10MinuteSnooze(getApplication(), prayer)
    }

    fun testEmergencyAlarm() {
        PrayerAlarmScheduler.triggerInstantAlarmTest(getApplication(), "Asr")
    }
}
