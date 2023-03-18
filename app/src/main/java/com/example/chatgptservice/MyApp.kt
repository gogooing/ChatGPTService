package com.example.chatgptservice

import android.app.Application

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        DroneService.start(this)
    }
}