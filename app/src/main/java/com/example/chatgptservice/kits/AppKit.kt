package com.example.chatgptservice.kits

import android.app.ActivityManager
import android.content.Context

object AppKit {

    /**
     * ServiceRunning
     * @param ctx Context
     * @param serviceClz String
     * @return Boolean
     */
    fun isServiceRunning(ctx: Context, serviceClz: String): Boolean {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val services = am.getRunningServices(Integer.MAX_VALUE) ?: return false
        for (info in services) {
            if (serviceClz == info.service.className) {
                return true
            }
        }
        return false
    }

}