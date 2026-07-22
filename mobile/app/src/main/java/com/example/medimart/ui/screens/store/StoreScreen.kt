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
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val branches by viewModel.branches.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showMapView by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            requestLocation(context) { lat, lon ->
                viewModel.updateUserLocation(lat, lon)
            }
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            requestLocation(context) { lat, lon ->
                viewModel.updateUserLocation(lat, lon)
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MediMartOrange)
            }
        } else if (showMapView) {
            StoreMapView(
                branches = branches,
                userLocation = userLocation,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            StoreListView(
                branches = branches,
                userLocation = userLocation,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun StoreListView(
    branches: List<StoreBranchWithDistance>,
    userLocation: Pair<Double, Double>?,
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
                hasUserLocation = userLocation != null,
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
    hasUserLocation: Boolean,
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
                if (hasUserLocation) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.2f km".format(branchWithDistance.distanceKm),
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

    // Initialize OSMDroid configuration before creating MapView
    remember {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            val center = userLocation?.let { GeoPoint(it.first, it.second) }
                ?: GeoPoint(10.845, 106.645) // Default: HCM City
            controller.setCenter(center)
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
        mapView.overlays.clear()

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
                if (userLocation != null) {
                    snippet = "%.2f km".format(item.distanceKm)
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

@SuppressLint("MissingPermission")
private fun requestLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Try last known location from available providers
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    for (provider in providers) {
        try {
            if (locationManager.isProviderEnabled(provider)) {
                val lastKnown = locationManager.getLastKnownLocation(provider)
                if (lastKnown != null) {
                    onLocationReceived(lastKnown.latitude, lastKnown.longitude)
                    return
                }
            }
        } catch (_: Exception) { }
    }

    // Request fresh location update
    val provider = providers.firstOrNull {
        try { locationManager.isProviderEnabled(it) } catch (_: Exception) { false }
    } ?: return

    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onLocationReceived(location.latitude, location.longitude)
            try { locationManager.removeUpdates(this) } catch (_: Exception) { }
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
}
