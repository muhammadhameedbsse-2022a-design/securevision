package com.securevision

import dagger.hilt.android.HiltAndroidApp
import android.app.Application

@HiltAndroidApp
class SecureVisionApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
