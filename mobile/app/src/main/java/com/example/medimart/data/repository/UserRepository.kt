package com.example.medimart.data.repository

import android.util.Log
import com.example.medimart.data.model.Address
import com.example.medimart.data.model.AddressRequest
import com.example.medimart.data.model.User
import com.example.medimart.data.remote.ApiService

private const val TAG = "UserRepository"

class UserRepository(private val apiService: ApiService) {
    suspend fun getProfile(): Result<User> = try {
        Result.success(apiService.getProfile())
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching user profile", e)
        Result.failure(e)
    }

    suspend fun updateProfile(user: User): Result<User> = try {
        Result.success(apiService.updateProfile(user))
    } catch (e: Exception) {
        Log.e(TAG, "Error updating user profile", e)
        Result.failure(e)
    }

    suspend fun getAddresses(): Result<List<Address>> = try {
        Result.success(apiService.getAddresses())
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching addresses", e)
        Result.failure(e)
    }

    suspend fun addAddress(request: AddressRequest): Result<Address> = try {
        Result.success(apiService.addAddress(request))
    } catch (e: Exception) {
        Log.e(TAG, "Error adding address", e)
        Result.failure(e)
    }

    suspend fun deleteAddress(id: String): Result<Boolean> = try {
        val res = apiService.deleteAddress(id)
        Result.success(res["success"] ?: false)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting address: $id", e)
        Result.failure(e)
    }
}
