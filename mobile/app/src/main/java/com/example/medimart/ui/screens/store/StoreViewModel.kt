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

class StoreViewModel : ViewModel() {
    private val _branches = MutableStateFlow<List<StoreBranchWithDistance>>(emptyList())
    val branches = _branches.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _branches.value = StoreBranchData.branches.map {
            StoreBranchWithDistance(it, 0.0)
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
        _branches.value = StoreBranchData.branches
            .map { branch ->
                StoreBranchWithDistance(
                    branch = branch,
                    distanceKm = haversine(latitude, longitude, branch.latitude, branch.longitude)
                )
            }
            .sortedBy { it.distanceKm }
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
