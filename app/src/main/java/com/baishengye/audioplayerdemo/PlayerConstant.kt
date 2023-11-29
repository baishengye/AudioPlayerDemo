package com.baishengye.audioplayerdemo

import androidx.annotation.IntDef


@Retention(AnnotationRetention.SOURCE)
@IntDef(PlayerState.STATE_IDLE, PlayerState.STATE_INITIALIZED,PlayerState.STATE_PREPARING,
    PlayerState.STATE_PREPARED, PlayerState.STATE_STARTED, PlayerState.STATE_PAUSED,
    PlayerState.STATE_STOPPED, PlayerState.STATE_COMPLETED, PlayerState.STATE_RELEASED,
    PlayerState.STATE_ERROR)
annotation class PlayerState {
    companion object{
        //等待
        const val STATE_IDLE = 0
        //初始化完成
        const val STATE_INITIALIZED = 1
        //准备中
        const val STATE_PREPARING = 2
        //准备完成
        const val STATE_PREPARED = 3
        //播放影音乐了
        const val STATE_STARTED = 4
        //暂停播放了
        const val STATE_PAUSED = 5
        //停止播放
        const val STATE_STOPPED = 6
        //播放完成
        const val STATE_COMPLETED = 7
        //mediaPlayer释放了
        const val STATE_RELEASED = 8
        //出错误了
        const val STATE_ERROR = -1
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(PlayerHandlerConstant.STATE_CHANGE_WHAT,PlayerHandlerConstant.POSITION_CHANGE_WHAT)
annotation class PlayerHandlerConstant{
    companion object{
        //播放状态变化
        const val STATE_CHANGE_WHAT = 10001
        //进度变化
        const val POSITION_CHANGE_WHAT = 10002
    }
}
//延时时间
const val HANDLER_DELAY_1S = 1000L

@Retention(AnnotationRetention.SOURCE)
@IntDef(PlayerFocusConstant.GOT_FOCUS,PlayerFocusConstant.LOSS_FOCUS)
annotation class PlayerFocusConstant{
    companion object{
        //焦点获取到了
        const val GOT_FOCUS = 1
        //焦点失去了
        const val LOSS_FOCUS = 2
    }
}
