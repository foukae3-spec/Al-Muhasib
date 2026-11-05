package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.PillShape
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualEmeraldBright
import com.example.util.HapticFeedbackUtil
import com.example.util.prayer.CityCatalog
import com.example.util.prayer.CityLocation
import com.example.util.prayer.PrayerPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerDialog(
    currentCityId: String,
    onSelectCity: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }
    var onlineCities by remember { mutableStateOf<List<CityLocation>>(emptyList()) }
    var isSearchingOnline by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cities, 1: Sunni Adhan Sound
    val prayerSettings = remember { PrayerPreferences.getSettings(context) }
    var currentAdhanTone by remember { mutableStateOf(prayerSettings.adhanToneId) }
    var previewPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var playingToneId by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            previewPlayer = null
        }
    }

    val localFilteredCities = remember(searchQuery) {
        CityCatalog.searchCities(searchQuery)
    }

    // ponytail: debounced worldwide online geocoding via OpenStreetMap
    androidx.compose.runtime.LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 2) {
            isSearchingOnline = true
            kotlinx.coroutines.delay(450)
            val fetched = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.util.prayer.OnlineCitySearcher.searchOnline(q)
            }
            onlineCities = fetched
            isSearchingOnline = false
        } else {
            onlineCities = emptyList()
            isSearchingOnline = false
        }
    }

    val combinedCities = remember(localFilteredCities, onlineCities) {
        (localFilteredCities + onlineCities).distinctBy { 
            "${(it.latitude * 100).toInt()}_${(it.longitude * 100).toInt()}"
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            detectGpsLocation(context, scope, { isLocating = it }) { lat, lng, resolvedName ->
                PrayerPreferences.setGpsLocation(context, lat, lng, resolvedName)
                HapticFeedbackUtil.performMilestoneDoublePulse(context)
                onDismiss()
            }
        } else {
            android.widget.Toast.makeText(context, "تم رفض إذن الموقع", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            try { previewPlayer?.stop(); previewPlayer?.release() } catch (_: Exception) {}
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = appColors.surface,
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = appColors.textSecondary)
                }

                Text(
                    text = if (selectedTab == 0) "المدينة ومواقيت الصلاة" else "صوت الأذان المبارك",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                )

                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Tab Selector (المدينة / صوت الأذان)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(appColors.glassCard)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (selectedTab == 0) SpiritualEmeraldBright else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "المدينة والموقع 📍",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.Black else appColors.textSecondary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (selectedTab == 1) SpiritualEmeraldBright else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "صوت الأذان 🕌",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.Black else appColors.textSecondary
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Auto-detect GPS button with loading spinner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShapeMedium)
                        .background(Color(0x2210B981))
                        .border(1.dp, SpiritualEmeraldBright.copy(alpha = 0.5f), SquircleShapeMedium)
                        .clickable(enabled = !isLocating) {
                            HapticFeedbackUtil.performLightTick(context)
                            val hasFine = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasFine || hasCoarse) {
                                detectGpsLocation(context, scope, { isLocating = it }) { lat, lng, resolvedName ->
                                    PrayerPreferences.setGpsLocation(context, lat, lng, resolvedName)
                                    HapticFeedbackUtil.performMilestoneDoublePulse(context)
                                    onDismiss()
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpiritualEmeraldBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLocating) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SpiritualEmeraldBright,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isLocating) "جاري تحديد موقعك الجغرافي بدقة..." else "تحديد موقعي تلقائياً (GPS)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpiritualEmeraldBright
                                )
                            )
                            Text(
                                text = if (isLocating) "فحص إشارات الشبكة والأقمار الصناعية" else "حساب فلكي دقيق حسب خطوط الطول والعرض الحالية",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = appColors.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Search Bar with Worldwide Online indicator
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ابحث عن أي مدينة أو قرية في العالم...", color = appColors.textTertiary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = SpiritualEmeraldBright) },
                    trailingIcon = {
                        if (isSearchingOnline) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = SpiritualEmeraldBright,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true,
                    shape = PillShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpiritualEmeraldBright,
                        unfocusedBorderColor = appColors.glassBorder,
                        focusedContainerColor = appColors.glassCard,
                        unfocusedContainerColor = appColors.glassCard
                    )
                )

                Spacer(Modifier.height(10.dp))

                // Cities List (Local + Worldwide OpenStreetMap)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(combinedCities, key = { it.id }) { city ->
                        val isSelected = city.id == currentCityId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeMedium)
                                .background(if (isSelected) Color(0x2210B981) else appColors.glassCard)
                                .border(
                                    1.dp,
                                    if (isSelected) SpiritualEmeraldBright else appColors.glassBorder,
                                    SquircleShapeMedium
                                )
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    PrayerPreferences.selectCity(context, city)
                                    onSelectCity(city)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${city.arabicName} (${city.englishName})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary
                                    )
                                )
                                Text(
                                    text = "${city.countryArabic} • ${city.defaultMethod.arabicName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = appColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Sunni Adhan Voice Selector
                Text(
                    text = "اختر المؤذن المفضل (مكتبة الأذان السُّنِّي المعتمدة)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = appColors.textSecondary,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.example.util.prayer.SUNNI_ADHANS, key = { it.id }) { tone ->
                        val isSelected = tone.id == currentAdhanTone
                        val isPreviewing = playingToneId == tone.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShapeMedium)
                                .background(if (isSelected) Color(0x2210B981) else appColors.glassCard)
                                .border(
                                    1.dp,
                                    if (isSelected) SpiritualEmeraldBright else appColors.glassBorder,
                                    SquircleShapeMedium
                                )
                                .clickable {
                                    HapticFeedbackUtil.performLightTick(context)
                                    currentAdhanTone = tone.id
                                    PrayerPreferences.updateAdhanTone(context, tone.id)
                                    android.widget.Toast.makeText(context, "تم تعيين: ${tone.arabicName}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Play / Stop preview button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPreviewing) SpiritualEmeraldBright else Color(0x2010B981))
                                        .clickable {
                                            if (isPreviewing) {
                                                try {
                                                    previewPlayer?.stop()
                                                    previewPlayer?.release()
                                                } catch (_: Exception) {}
                                                previewPlayer = null
                                                playingToneId = null
                                            } else {
                                                try {
                                                    previewPlayer?.stop()
                                                    previewPlayer?.release()
                                                    previewPlayer = android.media.MediaPlayer.create(context, tone.rawResId)?.apply {
                                                        setOnCompletionListener {
                                                            playingToneId = null
                                                        }
                                                        start()
                                                    }
                                                    playingToneId = tone.id
                                                } catch (_: Exception) {}
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        contentDescription = "استماع",
                                        tint = if (isPreviewing) Color.Black else SpiritualEmeraldBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = tone.arabicName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SpiritualEmeraldBright else appColors.textPrimary
                                        )
                                    )
                                    Text(
                                        text = "${tone.reciterName} • أذان أهل السنّة والجماعة",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = appColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SpiritualEmeraldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ponytail: robust multi-provider location detection with Geocoder + Online + Nearest-Wilaya fallback
private fun detectGpsLocation(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onStatusChange: (Boolean) -> Unit,
    onLocationFound: (lat: Double, lng: Double, cityName: String) -> Unit
) {
    onStatusChange(true)
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onStatusChange(false)
            android.widget.Toast.makeText(context, "خدمة تحديد الموقع غير متوفرة", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Check if device location toggle is enabled
        val isLocationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        if (!isLocationEnabled) {
            onStatusChange(false)
            android.widget.Toast.makeText(context, "يرجى تفعيل خدمة الموقع (GPS) في هاتفك أولاً", android.widget.Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
            return
        }

        // Check recent cached locations (network, GPS, passive, fused)
        val cachedLocations = mutableListOf<android.location.Location>()
        runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()?.let { cachedLocations.add(it) }
        runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()?.let { cachedLocations.add(it) }
        runCatching { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()?.let { cachedLocations.add(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lm.hasProvider(LocationManager.FUSED_PROVIDER)) {
            runCatching { lm.getLastKnownLocation(LocationManager.FUSED_PROVIDER) }.getOrNull()?.let { cachedLocations.add(it) }
        }

        val bestCached = cachedLocations.maxByOrNull { it.time }
        val now = System.currentTimeMillis()

        // If we have a cached fix less than 20 minutes old with decent accuracy, deliver immediately!
        if (bestCached != null && (now - bestCached.time < 20 * 60 * 1000L) && bestCached.accuracy < 2000f) {
            resolveAndDeliver(context, scope, bestCached.latitude, bestCached.longitude, onStatusChange, onLocationFound)
            return
        }

        // Fresh location request with multiple providers
        val listener = object : android.location.LocationListener {
            var delivered = false
            override fun onLocationChanged(location: android.location.Location) {
                if (delivered) return
                delivered = true
                try { lm.removeUpdates(this) } catch (_: Exception) {}
                resolveAndDeliver(context, scope, location.latitude, location.longitude, onStatusChange, onLocationFound)
            }
            @Deprecated("Deprecated") override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
            override fun onProviderEnabled(p: String) {}
            override fun onProviderDisabled(p: String) {}
        }

        var registered = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lm.hasProvider(LocationManager.FUSED_PROVIDER)) {
            try {
                lm.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 0L, 0f, listener, android.os.Looper.getMainLooper())
                registered = true
            } catch (_: Exception) {}
        }
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            try {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener, android.os.Looper.getMainLooper())
                registered = true
            } catch (_: Exception) {}
        }
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener, android.os.Looper.getMainLooper())
                registered = true
            } catch (_: Exception) {}
        }

        // Modern Android 11+ single shot current location request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val provider = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lm.hasProvider(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                    else -> LocationManager.GPS_PROVIDER
                }
                lm.getCurrentLocation(provider, null, androidx.core.content.ContextCompat.getMainExecutor(context)) { location ->
                    if (location != null && !listener.delivered) {
                        listener.delivered = true
                        try { lm.removeUpdates(listener) } catch (_: Exception) {}
                        resolveAndDeliver(context, scope, location.latitude, location.longitude, onStatusChange, onLocationFound)
                    }
                }
                registered = true
            } catch (_: Exception) {}
        }

        if (!registered) {
            // If cannot register updates but have any cached location, fall back to it
            if (bestCached != null) {
                resolveAndDeliver(context, scope, bestCached.latitude, bestCached.longitude, onStatusChange, onLocationFound)
                return
            }
            onStatusChange(false)
            android.widget.Toast.makeText(context, "يرجى تشغيل الـ GPS وخدمات الموقع في هاتفك", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        // Timeout handler: if fresh fix takes more than 7s, fall back to bestCached if present, or alert
        scope.launch {
            kotlinx.coroutines.delay(7000L)
            if (!listener.delivered) {
                try { lm.removeUpdates(listener) } catch (_: Exception) {}
                if (bestCached != null) {
                    resolveAndDeliver(context, scope, bestCached.latitude, bestCached.longitude, onStatusChange, onLocationFound)
                } else {
                    onStatusChange(false)
                    android.widget.Toast.makeText(context, "استغرق تحديد الموقع وقتاً طويلاً، يمكنك اختيار ولايتك مباشرة من القائمة", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    } catch (_: SecurityException) {
        onStatusChange(false)
        android.widget.Toast.makeText(context, "إذن تحديد الموقع مطلوب لتحديد القبلة ومواقيت الصلاة بدقة", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun resolveAndDeliver(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    lat: Double,
    lng: Double,
    onStatusChange: (Boolean) -> Unit,
    onLocationFound: (lat: Double, lng: Double, cityName: String) -> Unit
) {
    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
        var resolvedName: String? = null

        // 1. Android System Geocoder
        try {
            if (android.location.Geocoder.isPresent()) {
                val geocoder = android.location.Geocoder(context, java.util.Locale("ar"))
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lng, 1)
                val addr = list?.firstOrNull()
                if (addr != null) {
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val country = addr.countryName
                    if (city != null && country != null) {
                        resolvedName = "$city، $country"
                    } else if (city != null) {
                        resolvedName = city
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Online reverse geocode fallback
        if (resolvedName == null) {
            resolvedName = com.example.util.prayer.OnlineCitySearcher.reverseGeocodeOnline(lat, lng)
        }

        // 3. Offline nearest city from catalog fallback (e.g. closest Wilaya)
        if (resolvedName == null) {
            val nearest = CityCatalog.findNearestCity(lat, lng)
            resolvedName = "${nearest.arabicName}، ${nearest.countryArabic}"
        }

        val finalName = resolvedName ?: "موقعي الحالي"

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            onStatusChange(false)
            android.widget.Toast.makeText(context, "تم تحديد الموقع: $finalName", android.widget.Toast.LENGTH_SHORT).show()
            onLocationFound(lat, lng, finalName)
        }
    }
}
