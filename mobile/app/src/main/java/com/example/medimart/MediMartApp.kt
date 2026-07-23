package com.example.medimart

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class MediMartApp : Application() {

    override fun onCreate() {
        super.onCreate()
        configureMapTiles()
    }

    private fun configureMapTiles() {
        Configuration.getInstance().apply {
            load(
                this@MediMartApp,
                getSharedPreferences(OSMDROID_PREFERENCES, MODE_PRIVATE)
            )
            // OSM blocks generic library/package User-Agents. Identify this app and
            // provide a contact URL as required by the public tile usage policy.
            userAgentValue =
                "MediMart/${BuildConfig.VERSION_NAME} (Android; +$PROJECT_URL)"
        }
    }

    private companion object {
        const val OSMDROID_PREFERENCES = "osmdroid"
        const val PROJECT_URL = "https://github.com/hohiphop1234/MediMart"
    }
}
