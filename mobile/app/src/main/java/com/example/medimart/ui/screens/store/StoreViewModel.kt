package com.example.medimart.ui.screens.store

import androidx.lifecycle.ViewModel
import com.example.medimart.data.model.StoreBranchData
import com.example.medimart.data.model.StoreBranchWithDistance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class StoreLocationStatus {
    REQUESTING_PERMISSION,
    LOCATING,
    AVAILABLE,
    PERMISSION_DENIED,
    UNAVAILABLE
}

data class StoreUiState(
    val branches: List<StoreBranchWithDistance> = branchesWithoutDistance(),
    val userLocation: Pair<Double, Double>? = null,
    val locationStatus: StoreLocationStatus = StoreLocationStatus.REQUESTING_PERMISSION
)

class StoreViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState = _uiState.asStateFlow()

    fun beginLocationLookup() {
        _uiState.value = _uiState.value.copy(locationStatus = StoreLocationStatus.LOCATING)
    }

    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            branches = branchesWithoutDistance(),
            userLocation = null,
            locationStatus = StoreLocationStatus.PERMISSION_DENIED
        )
    }

    fun onLocationUnavailable() {
        _uiState.value = _uiState.value.copy(
            branches = branchesWithoutDistance(),
            userLocation = null,
            locationStatus = StoreLocationStatus.UNAVAILABLE
        )
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        val branchesWithDistance = StoreBranchData.branches
            .map { branch ->
                StoreBranchWithDistance(
                    branch = branch,
                    distanceKm = haversine(latitude, longitude, branch.latitude, branch.longitude)
                )
            }
            .sortedBy { it.distanceKm }

        // Publish the location and its matching distances atomically so the UI
        // never renders a real location together with placeholder 0.00 km values.
        _uiState.value = StoreUiState(
            branches = branchesWithDistance,
            userLocation = latitude to longitude,
            locationStatus = StoreLocationStatus.AVAILABLE
        )
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

private fun branchesWithoutDistance(): List<StoreBranchWithDistance> =
    StoreBranchData.branches.map { StoreBranchWithDistance(it, distanceKm = null) }
