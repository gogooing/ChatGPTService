package com.example.chatgptservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.chatgptservice.kits.AppKit

class DroneService : Service(), UDPReceiveListener {

    companion object {
        val TAG: String = DroneService::class.java.simpleName

        private const val CAMERA_VIDEO_SEND_PORT = 30000 //摄像机视频发送端口
        const val CAMERA_VIDEO_RECEIVE_PORT = 30001 //摄像机视频接收端口

        const val LEPTON_DATA_SEND_PORT = 30006 //热成像数据发送端口
        const val LEPTON_DATA_RECEIVE_PORT = 30007 //热成像数据接收端口

        /**
         * 是否正在运行中
         * @param ctx Context
         */
        fun isRunning(ctx: Context): Boolean {
            return AppKit.isServiceRunning(ctx, DroneService::class.java.name)
        }

        /**
         * 开始
         */
        @JvmStatic
        fun start(ctx: Context) {
            try {
                if (isRunning(ctx)) {
                    return
                }
                val intent = Intent(ctx, DroneService::class.java)
                ctx.startService(intent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private var mCtx: Context? = null

    private var mCameraUDPSocket: UDPSocket? = null //camera视频数据socket
    private var mLeptonUDPSocket: UDPSocket? = null //lepton热成像socket

    private var mCameraVideoDecodeThread: CameraVideoDecodeThread? = null //camera视频解码线程
    private var mLeptonDecodeThread: LeptonVideoDecodeThread? = null //lepton热成像线程

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        mCtx = this

        //camera视频数据socket
        mCameraUDPSocket = UDPSocket()
        mCameraUDPSocket!!.startUDPSocket(CAMERA_VIDEO_RECEIVE_PORT)
        mCameraUDPSocket!!.registerReceiveListener(this)

        //lepton热成像socket
        mLeptonUDPSocket = UDPSocket()
        mLeptonUDPSocket!!.startUDPSocket(LEPTON_DATA_RECEIVE_PORT)
        mLeptonUDPSocket!!.registerReceiveListener(this)

        mCameraVideoDecodeThread = CameraVideoDecodeThread()
        mCameraVideoDecodeThread!!.init(mCtx, mCameraUDPSocket)
        mCameraVideoDecodeThread!!.priority = Thread.MAX_PRIORITY
        mCameraVideoDecodeThread!!.start()

        mLeptonDecodeThread = LeptonVideoDecodeThread()
        mLeptonDecodeThread!!.init(mCtx, mLeptonUDPSocket)
        mLeptonDecodeThread!!.priority = Thread.MAX_PRIORITY
        mLeptonDecodeThread!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        mCameraVideoDecodeThread?.finish()

        mLeptonDecodeThread?.finish()

        mCameraUDPSocket?.unRegisterReceiveListener()
        mCameraUDPSocket?.stopUDPSocket()

        mLeptonUDPSocket?.unRegisterReceiveListener()
        mLeptonUDPSocket?.stopUDPSocket()

    }

    override fun receiveData(ip: String?, port: Int, data: ByteArray?, len: Int) {
        try {
            when (port) {
                CAMERA_VIDEO_RECEIVE_PORT -> {
                    mCameraVideoDecodeThread?.handleCameraDecode(data, len)
                }
                LEPTON_DATA_RECEIVE_PORT -> {
                    mLeptonDecodeThread?.handleLeptonDecode(data, len)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}