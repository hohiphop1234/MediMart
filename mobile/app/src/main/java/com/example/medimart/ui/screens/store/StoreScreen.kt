package com.example.medimart.ui.screens.store

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.medimart.data.model.StoreBranchWithDistance
import com.example.medimart.theme.MediMartBg
import com.example.medimart.theme.MediMartOrange
import com.example.medimart.theme.MediMartOrangeSoft
import com.example.medimart.theme.MediMartTextSecondary
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

private val STORE_TILE_SOURCE = XYTileSource(
    "MediMartOpenStreetMap",
    0,
    19,
    256,
    ".png",
    arrayOf("https://tile.openstreetmap.org/"),
    "© OpenStreetMap contributors"
)

private const val LOCATION_TIMEOUT_MS = 15_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showMapView by remember { mutableStateOf(false) }
    var locationRequestVersion by remember { mutableIntStateOf(0) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationRequestVersion++
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            locationRequestVersion++
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(locationRequestVersion) {
        if (locationRequestVersion == 0 || !hasLocationPermission(context)) {
            onDispose { }
        } else {
            viewModel.beginLocationLookup()
            val cancelLocationRequest = requestLocation(
                context = context,
                onLocationReceived = viewModel::updateUserLocation,
                onLocationUnavailable = viewModel::onLocationUnavailable
            )
            onDispose(cancelLocationRequest)
        }
    }

    val retryLocation: () -> Unit = {
        if (hasLocationPermission(context)) {
            locationRequestVersion++
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hệ thống Cửa hàng", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { showMapView = !showMapView }) {
                        Icon(
                            imageVector = if (showMapView) Icons.AutoMirrored.Filled.List
                            else Icons.Filled.Map,
                            contentDescription = if (showMapView) "Danh sách" else "Bản đồ",
                            tint = MediMartOrange
                        )
                    }
                }
            )
        },
        containerColor = MediMartBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LocationStatusBanner(
                status = uiState.locationStatus,
                onRetry = retryLocation
            )

            if (showMapView) {
                StoreMapView(
                    branches = uiState.branches,
                    userLocation = uiState.userLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                StoreListView(
                    branches = uiState.branches,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LocationStatusBanner(
    status: StoreLocationStatus,
    onRetry: () -> Unit
) {
    if (status == StoreLocationStatus.AVAILABLE) return

    val message = when (status) {
        StoreLocationStatus.REQUESTING_PERMISSION -> "Đang yêu cầu quyền vị trí…"
        StoreLocationStatus.LOCATING -> "Đang xác định vị trí của bạn…"
        StoreLocationStatus.PERMISSION_DENIED ->
            "Chưa có quyền vị trí nên chưa thể tính khoảng cách."
        StoreLocationStatus.UNAVAILABLE ->
            "Không lấy được vị trí. Hãy bật GPS rồi thử lại."
        StoreLocationStatus.AVAILABLE -> return
    }

    Surface(color = MediMartOrangeSoft) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status == StoreLocationStatus.LOCATING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MediMartOrange
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MediMartOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MediMartTextSecondary
            )
            if (status == StoreLocationStatus.PERMISSION_DENIED ||
                status == StoreLocationStatus.UNAVAILABLE
            ) {
                TextButton(onClick = onRetry) {
                    Text("Thử lại", color = MediMartOrange)
                }
            }
        }
    }
}

@Composable
private fun StoreListView(
    branches: List<StoreBranchWithDistance>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(branches) { item ->
            StoreCard(
                branchWithDistance = item,
                onNavigateClick = {
                    val geoUri = Uri.parse(
                        "geo:${item.branch.latitude},${item.branch.longitude}" +
                                "?q=${item.branch.latitude},${item.branch.longitude}" +
                                "(${Uri.encode(item.branch.name)})"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // No map app installed
                    }
                }
            )
        }
    }
}

@Composable
private fun StoreCard(
    branchWithDistance: StoreBranchWithDistance,
    onNavigateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = branchWithDistance.branch.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.example.medimart.theme.MediMartTextPrimary,
                    maxLines = 2
                )
                branchWithDistance.distanceKm?.let { distanceKm ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.2f km".format(distanceKm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediMartTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Filled.Navigation,
                contentDescription = "Navigate",
                tint = MediMartOrange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StoreMapView(
    branches: List<StoreBranchWithDistance>,
    userLocation: Pair<Double, Double>?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            // A dedicated source name avoids reusing the old cached 403 tiles.
            setTileSource(STORE_TILE_SOURCE)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            val center = userLocation?.let { GeoPoint(it.first, it.second) }
                ?: GeoPoint(10.845, 106.645) // Default: HCM City
            controller.setCenter(center)
            overlays.add(CopyrightOverlay(context))
        }
    }

    // Lifecycle management for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Update markers when data changes
    LaunchedEffect(branches, userLocation) {
        // Keep the mandatory OpenStreetMap attribution overlay in place.
        mapView.overlays.removeAll { it is Marker }

        // User location marker
        userLocation?.let { (lat, lon) ->
            val userMarker = Marker(mapView).apply {
                position = GeoPoint(lat, lon)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Vị trí của bạn"
                icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
            }
            mapView.overlays.add(userMarker)
            mapView.controller.setCenter(GeoPoint(lat, lon))
        }

        // Branch markers
        branches.forEach { item ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(item.branch.latitude, item.branch.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = item.branch.name
                item.distanceKm?.let { distanceKm ->
                    snippet = "%.2f km".format(distanceKm)
                }
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun hasLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

@SuppressLint("MissingPermission")
private fun requestLocation(
    context: Context,
    onLocationReceived: (Double, Double) -> Unit,
    onLocationUnavailable: () -> Unit
): () -> Unit {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(
        LocationManager.NETWORK_PROVIDER,
        LocationManager.GPS_PROVIDER
    ).filter { provider ->
        try {
            locationManager.isProviderEnabled(provider)
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    if (providers.isEmpty()) {
        onLocationUnavailable()
        return {}
    }

    val lastKnownLocation = providers.mapNotNull { provider ->
        try {
            locationManager.getLastKnownLocation(provider)
        } catch (_: SecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }.filter { it.hasValidCoordinates() }
        .maxByOrNull { it.time }

    if (lastKnownLocation != null) {
        onLocationReceived(lastKnownLocation.latitude, lastKnownLocation.longitude)
        return {}
    }

    val mainHandler = Handler(Looper.getMainLooper())
    var completed = false
    lateinit var listener: LocationListener
    var timeout: Runnable? = null

    fun finish(location: Location?) {
        if (completed) return
        completed = true
        timeout?.let(mainHandler::removeCallbacks)
        try {
            locationManager.removeUpdates(listener)
        } catch (_: SecurityException) {
            // Permission may have been revoked while the request was active.
        }

        if (location != null && location.hasValidCoordinates()) {
            onLocationReceived(location.latitude, location.longitude)
        } else {
            onLocationUnavailable()
        }
    }

    listener = object : LocationListener {
        override fun onLocationChanged(location: Location) = finish(location)
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    timeout = Runnable { finish(null) }

    try {
        providers.forEach { provider ->
            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                listener,
                Looper.getMainLooper()
            )
        }
        timeout?.let { mainHandler.postDelayed(it, LOCATION_TIMEOUT_MS) }
    } catch (_: SecurityException) {
        finish(null)
    } catch (_: IllegalArgumentException) {
        finish(null)
    }

    return {
        if (!completed) {
            completed = true
            timeout?.let(mainHandler::removeCallbacks)
            try {
                locationManager.removeUpdates(listener)
            } catch (_: SecurityException) {
                // Nothing else to clean up.
            }
        }
    }
}

private fun Location.hasValidCoordinates(): Boolean =
    latitude in -90.0..90.0 && longitude in -180.0..180.0
