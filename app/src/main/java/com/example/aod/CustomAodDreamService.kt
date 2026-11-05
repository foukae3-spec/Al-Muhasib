package com.example.aod

import android.service.dreams.DreamService
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MuhasibApplication
import com.example.ui.theme.AlMuhasibTheme
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class CustomAodDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val dreamScope = CoroutineScope(Dispatchers.Main + Job())
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Configure ambient dream parameters
        isInteractive = true
        isFullscreen = true
        isScreenBright = false

        window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val repository = MuhasibApplication.instance.repository

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@CustomAodDreamService)
            setViewTreeViewModelStoreOwner(this@CustomAodDreamService)
            setViewTreeSavedStateRegistryOwner(this@CustomAodDreamService)

            setContent {
                AlMuhasibTheme(darkTheme = true) {
                    val sobrietyState by repository.getSobrietyState()
                        .collectAsState(initial = null)

                    var burnInOffsetX by remember { mutableIntStateOf(0) }
                    var burnInOffsetY by remember { mutableIntStateOf(0) }

                    AodDisplayContent(
                        istighfarCount = sobrietyState?.istighfarTodayCount ?: 0,
                        onIncrementIstighfar = {
                            dreamScope.launch {
                                repository.incrementIstighfarCount()
                                WidgetUpdateHelper.updateAllWidgets(this@CustomAodDreamService)
                            }
                        },
                        subhanAllahCount = sobrietyState?.subhanAllahTodayCount ?: 0,
                        onIncrementSubhanAllah = {
                            dreamScope.launch {
                                repository.incrementSubhanAllahCount()
                                WidgetUpdateHelper.updateAllWidgets(this@CustomAodDreamService)
                            }
                        },
                        alhamdulillahCount = sobrietyState?.alhamdulillahTodayCount ?: 0,
                        onIncrementAlhamdulillah = {
                            dreamScope.launch {
                                repository.incrementAlhamdulillahCount()
                                WidgetUpdateHelper.updateAllWidgets(this@CustomAodDreamService)
                            }
                        },
                        allahuAkbarCount = sobrietyState?.allahuAkbarTodayCount ?: 0,
                        onIncrementAllahuAkbar = {
                            dreamScope.launch {
                                repository.incrementAllahuAkbarCount()
                                WidgetUpdateHelper.updateAllWidgets(this@CustomAodDreamService)
                            }
                        },
                        laIlahaIllallahCount = sobrietyState?.laIlahaIllallahTodayCount ?: 0,
                        onIncrementLaIlahaIllallah = {
                            dreamScope.launch {
                                repository.incrementLaIlahaIllallahCount()
                                WidgetUpdateHelper.updateAllWidgets(this@CustomAodDreamService)
                            }
                        },
                        tasbi7Count = sobrietyState?.tasbi7TodayCount ?: 0,
                        onIncrementTasbi7 = {
                            dreamScope.launch {
                                repository.incrementTasbi7Count()
                                WidgetUpdateHelper.updateAllWidgets(this@CustomAodDreamService)
                            }
                        },
                        onUnlock = {
                            finish()
                        },
                        burnInOffsetX = burnInOffsetX,
                        burnInOffsetY = burnInOffsetY,
                        clockStyle = AodManager.getClockStyle(this@CustomAodDreamService),
                        accentColorStyle = AodManager.getAccentColor(this@CustomAodDreamService),
                        batteryStyle = AodManager.getBatteryStyle(this@CustomAodDreamService),
                        isNotificationPrivacy = AodManager.isNotificationPrivacyEnabled(this@CustomAodDreamService),
                        onMinuteTick = {
                            if (AodManager.isBurnInProtectionEnabled(this@CustomAodDreamService)) {
                                burnInOffsetX = Random.nextInt(-3, 4)
                                burnInOffsetY = Random.nextInt(-3, 4)
                            }
                        }
                    )
                }
            }
        }

        setContentView(composeView)
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        dreamScope.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        appViewModelStore.clear()
        super.onDestroy()
    }
}
