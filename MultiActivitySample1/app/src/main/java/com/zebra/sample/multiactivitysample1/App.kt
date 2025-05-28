package com.zebra.sample.multiactivitysample1

import android.app.Application

class App : Application() {

    // Companion object to hold the singleton instance
    companion object {
        // Volatile ensures that the instance is visible to all threads
        @Volatile
        private var instance: App? = null

        // Synchronized method to get the singleton instance
        fun getInstance(): App =
            instance ?: synchronized(this) {
                instance ?: throw IllegalStateException("Application is not created yet!")
            }
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the instance variable when the application is created
        instance = this
    }
}
