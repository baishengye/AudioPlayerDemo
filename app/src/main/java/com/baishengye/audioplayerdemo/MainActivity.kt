package com.baishengye.audioplayerdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService( Intent(this, AudioPlayerService::class.java))

        findViewById<View>(R.id.btnCrash).setOnClickListener { v ->
            Toast.makeText(
                this@MainActivity,
                "3s后程序崩溃",
                Toast.LENGTH_SHORT
            ).show()
            v.postDelayed({ val i = 3 / 0 }, 3000)
        }
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            AudioPlayerManager.getInstance().playUrlsReset(
                listOf(
                    "http://music.163.com/song/media/outer/url?id=447925558.mp3",
                "http://www.170mv.com/kw/antiserver.kuwo.cn/anti.s?rid=MUSIC_93477122&response=res&format=mp3|aac&type=convert_url&br=128kmp3&agent=iPhone&callback=getlink&jpcallback=getlink.mp3"
                )
            )
//                    "https://m704.music.126.net/20231129152242/9b3aa0be8e21725f2a7160bfba1c4c96/jdymusic/obj/wo3DlMOGwrbDjj7DisKw/27185780744/d54e/a772/0d50/d0ce130013e19d1f83404f7f8dcfd5e0.mp3",
//                    "https://m10.music.126.net/20231129151432/8139cbd1ae62ced4a799d6145a2ec027/ymusic/obj/w5zDlMODwrDDiGjCn8Ky/14055974989/e550/15c2/ca62/8655ab85363e10e1c52f1aa1650b64e2.mp3" ))
        }
    }
}