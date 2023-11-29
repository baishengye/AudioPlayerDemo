package com.baishengye.audioplayerdemo

import java.time.Duration

/**
 * 播放器回调*/
interface AudioPlayerListener {
    /**
     * @param state 当前播放器状态*/
    fun onStateChanged(@PlayerState state:Int)

    /**
     * @param position 当前播放进度:毫秒*/
    fun onPositionChange(position:Int,duration: Int)

    /**
     * 服务被杀死:调用onDestroy*/
    fun onShutdown()
}
