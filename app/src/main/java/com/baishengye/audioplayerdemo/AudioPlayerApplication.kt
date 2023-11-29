package com.baishengye.audioplayerdemo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.boolbird.keepalive.KeepAlive
import com.boolbird.keepalive.KeepAliveConfigs

class AudioPlayerApplication:Application() {
    private val TAG = "AudioPlayerApplication"

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        Log.d(TAG, "attachBaseContext")
//        val configs = KeepAliveConfigs(
//            KeepAliveConfigs.Config(
//                packageName,
//                AudioPlayerService::class.java.canonicalName
//            )
//        )
//        configs.ignoreBatteryOptimization()
//        configs.rebootThreshold(10*1000, 3);
//        configs.setOnBootReceivedListener { context, intent ->
//            Log.d(TAG, "onReceive boot")
//            // 设置服务自启
//            context.startService(Intent(context, AudioPlayerService::class.java))
//        }
//        KeepAlive.init(base, configs)
    }

    override fun onCreate() {
        super.onCreate()

        AudioPlayerManager.init(this)
    }
}