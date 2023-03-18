package com.example.chatgptservice

import android.content.Context

class LeptonVideoDecodeThread : Thread() {

    private var mForceStop = false

    private var mContext: Context? = null
    private var mLeptonUDPSocket: UDPSocket? = null

    fun init(context: Context?, socket: UDPSocket?) {
        mContext = context
        mLeptonUDPSocket = socket
        //mAppPrefs = DroidPlannerPrefs.getInstance(context)
    }

    override fun run() {
        while (!mForceStop) {
            if (mLeptonUDPSocket != null) mLeptonUDPSocket!!.receiveMessage()
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

    fun handleLeptonDecode(data: ByteArray?, len: Int) {

    }
}