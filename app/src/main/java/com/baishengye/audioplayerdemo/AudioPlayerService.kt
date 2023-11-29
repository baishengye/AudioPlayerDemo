package com.baishengye.audioplayerdemo

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log


class AudioPlayerService:Service(), MediaPlayer.OnInfoListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener {

    private val TAG = "AudioPlayerService"

    private var mPlayer: MediaPlayer? = null

    private var mPlayerStateListener:AudioPlayerListener ?=null
    fun setPlayerStateListener(listener: AudioPlayerListener){
        mPlayerStateListener = listener
    }

    /**
     * 回调播放状态到主线程*/
    private val mMainHandler = Handler(Looper.getMainLooper()) {
        when(it.what){
            PlayerHandlerConstant.STATE_CHANGE_WHAT->{
                mPlayerStateListener?.onStateChanged(it.obj as Int)
            }

            PlayerHandlerConstant.POSITION_CHANGE_WHAT->{
                mPlayerStateListener?.onPositionChange(mPlayer?.currentPosition?:0,mPlayer?.duration?:0)
                handlerPositionChangeDelay(HANDLER_DELAY_1S)
            }
        }

        return@Handler true
    }


    private var mPlayerState:Int = PlayerState.STATE_IDLE
        set(value) {
            field = value
            Log.d(TAG,"playerState:${value}")
            handlerStateChange(value)
            handlerPositionChange()
        }

    private val mBinder:AudioPlayerBinder = AudioPlayerBinder()

    inner class AudioPlayerBinder:Binder(){
        fun getService():AudioPlayerService{
            return this@AudioPlayerService
        }
    }

    /**
     * handler传递状态变化*/
    private fun handlerStateChange(@PlayerState state:Int){
        val obtain = Message.obtain(mMainHandler,PlayerHandlerConstant.STATE_CHANGE_WHAT)
        obtain.obj = state
        mMainHandler.sendMessage(obtain)
    }

    /**
     * handler传递进度变化,只有在播放开始后才会进度变化*/
    private fun handlerPositionChange(){
        handlerPositionChangeDelay(0)
    }

    /**
     * 延时一定时间*/
    private fun handlerPositionChangeDelay(mill:Long){
        if(mPlayerState == PlayerState.STATE_STARTED){
            //是播放状态就开始传递进度
            val obtain = Message.obtain(mMainHandler,PlayerHandlerConstant.POSITION_CHANGE_WHAT)
            obtain.obj = mPlayer?.currentPosition?:0
            mMainHandler.sendMessageDelayed(obtain,mill)
        }else{
            /**
             * 不是播放状态就将传递进度的信号移除*/
            mMainHandler.removeMessages(PlayerHandlerConstant.POSITION_CHANGE_WHAT)
        }
    }


    /**
     * 播放音频url*/
    fun playUrl(url:String){
        if(mPlayer==null){
            mPlayer = MediaPlayer()
        }

        mPlayer?.let {
            it.stop()
            it.reset()

            it.setDataSource(url)
            it.isLooping = false
            it.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mPlayerState = PlayerState.STATE_INITIALIZED

            it.setOnInfoListener(this);
            it.setOnPreparedListener(this);
            it.setOnCompletionListener(this);
            it.setOnErrorListener(this);
            it.setOnSeekCompleteListener(this);

            it.prepareAsync()

            mPlayerState = PlayerState.STATE_PREPARING
        }
    }

    /**
     * 开始播放*/
    private fun startPlayer(){
        if(mPlayerState==PlayerState.STATE_PREPARED||mPlayerState==PlayerState.STATE_PAUSED){
            mPlayer?.let {
                it.start()
                mPlayerState = PlayerState.STATE_STARTED
            }
        }
    }

    /**
     * 恢复播放*/
    fun resumePlayer(){
        if(mPlayerState==PlayerState.STATE_PAUSED){
            startPlayer()
        }
    }

    /**
     * 暂停播放*/
    fun pausePlayer(){
        if(mPlayerState==PlayerState.STATE_STARTED){
            mPlayerState = PlayerState.STATE_PAUSED
            mPlayer?.pause()
        }
    }

    fun stopPlayer(){
        if(mPlayerState==PlayerState.STATE_PAUSED||mPlayerState==PlayerState.STATE_PAUSED){
            mPlayer?.stop()
        }
    }

    override fun onCreate() {
        Log.d(TAG,"onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand")

        if(mPlayer==null){
            mPlayer = MediaPlayer()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG,"onBind")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG,"onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
        mPlayerStateListener?.onShutdown()
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mPlayerState = PlayerState.STATE_PREPARED
        startPlayer()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mPlayerState = PlayerState.STATE_COMPLETED
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mPlayerState = PlayerState.STATE_ERROR
        return true
    }

    override fun onSeekComplete(mp: MediaPlayer?) {}
}