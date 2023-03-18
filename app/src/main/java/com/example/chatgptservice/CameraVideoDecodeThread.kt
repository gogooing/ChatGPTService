package com.example.chatgptservice

import android.content.Context

class CameraVideoDecodeThread : Thread() {

    private var mForceStop = false

    private var mContext: Context? = null
    private var mCameraUDPSocket: UDPSocket? = null

    fun init(context: Context?, socket: UDPSocket?) {
        mContext = context
        mCameraUDPSocket = socket
        //mAppPrefs = DroidPlannerPrefs.getInstance(context)
    }

    override fun run() {
        while (!mForceStop) {
            if (mCameraUDPSocket != null) mCameraUDPSocket!!.receiveMessage()
        }
    }

    fun finish() {
        mForceStop = true
        try {
            if (isAlive && !isInterrupted) {
                interrupt()
                join()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleCameraDecode(data: ByteArray?, len: Int) {

    }
}