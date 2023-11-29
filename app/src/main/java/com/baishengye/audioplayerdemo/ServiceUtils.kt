package com.baishengye.audioplayerdemo

import android.app.ActivityManager
import android.content.Context

object ServiceUtils {

    @JvmStatic
    fun isServiceRunning(context: Context,  serviceName:String):Boolean{
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val infos = am.getRunningServices(100);

        for(info in infos){
            val name:String = info.service.className;
            if(name == serviceName){
                return true
            }
        }
        return false;
    }

}