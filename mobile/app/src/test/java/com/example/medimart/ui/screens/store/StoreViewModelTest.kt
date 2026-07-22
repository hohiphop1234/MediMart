package com.example.medimart.ui.screens.store

import com.example.medimart.data.model.StoreBranchData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest {
    @Test
    fun initialDistancesAreUnknownInsteadOfZero() {
        val state = StoreViewModel().uiState.value

        assertEquals(StoreLocationStatus.REQUESTING_PERMISSION, state.locationStatus)
        assertNull(state.userLocation)
        assertTrue(state.branches.all { it.distanceKm == null })
    }

    @Test
    fun locationUpdateCalculatesAndSortsDistancesAtomically() {
        val viewModel = StoreViewModel()
        val targetBranch = StoreBranchData.branches.first()

        viewModel.updateUserLocation(targetBranch.latitude, targetBranch.longitude)

        val state = viewModel.uiState.value
        assertEquals(StoreLocationStatus.AVAILABLE, state.locationStatus)
        assertEquals(targetBranch.latitude, state.userLocation?.first ?: Double.NaN, 0.0)
        assertEquals(targetBranch.longitude, state.userLocation?.second ?: Double.NaN, 0.0)
        assertEquals(targetBranch, state.branches.first().branch)
        assertEquals(0.0, state.branches.first().distanceKm ?: Double.NaN, 0.001)
        assertTrue(state.branches.all { it.distanceKm != null })
        assertTrue(
            state.branches.zipWithNext().all { (current, next) ->
                requireNotNull(current.distanceKm) <= requireNotNull(next.distanceKm)
            }
        )
    }

    @Test
    fun deniedPermissionRemovesStaleDistances() {
        val viewModel = StoreViewModel()
        val targetBranch = StoreBranchData.branches.first()
        viewModel.updateUserLocation(targetBranch.latitude, targetBranch.longitude)

        viewModel.onLocationPermissionDenied()

        val state = viewModel.uiState.value
        assertEquals(StoreLocationStatus.PERMISSION_DENIED, state.locationStatus)
        assertNull(state.userLocation)
        assertTrue(state.branches.all { it.distanceKm == null })
    }
}
