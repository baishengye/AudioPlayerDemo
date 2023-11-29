package com.baishengye.audioplayerdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.core.util.isEmpty
import androidx.core.util.isNotEmpty
import java.time.Duration
import java.time.LocalDate
import java.util.ArrayDeque
import java.util.TreeMap
import java.util.TreeSet

class AudioPlayerManager(context: Context) :AudioPlayerListener,AudioFocusListener {

    companion object{

        @Volatile
        private var mInstance:AudioPlayerManager?=null

        fun getInstance():AudioPlayerManager{
            return mInstance!!
        }

        @JvmStatic
        fun init(context: Context){
            if(mInstance==null){
                synchronized(AudioPlayerManager::class.java){
                    if(mInstance==null){
                        mInstance = AudioPlayerManager(context)
                    }
                }
            }
        }
    }

    private val TAG:String = "AudioPlayerService"

    private val mAppContext:Context
    private val mAudioFocusManager:AudioFocusManager
    private var mAudioService:AudioPlayerService?=null
    private val mAudioServiceConnection:AudioServiceConnection = AudioServiceConnection()

    private var mAudioPlayerListener:AudioPlayerListener?=null
    fun setAudioPlayerListener(listener: AudioPlayerListener){
        mAudioPlayerListener = listener
    }

    private var mCurrAudioUrl:String?=null
    private val mAudioUrls:TreeSet<String> = TreeSet<String>()

    init {
        mAppContext = context.applicationContext
        mAudioFocusManager = AudioFocusManager()
        mAudioFocusManager.setAudioFocusListener(this)
    }

    /**
     * 重置播放列表，并且填充新的播放列表*/
    fun playUrlsReset(urls:List<String>){
        mAudioUrls.clear()
        mAudioUrls.addAll(urls)
        playAudio(true)
    }

    /**
     * 重置播放列表，并且填充新的播放列表*/
    fun playUrlsReset(url:String){
        mAudioUrls.clear()
        mAudioUrls.add(url)
        playAudio(true)
    }

    /**
     * 有没有下一首
     * @return true 有下一首*/
    fun hasNext(isUserAction:Boolean):Boolean {
        return mAudioUrls.isNotEmpty()
    }

    fun nextAudio(isUserAction: Boolean){
        mCurrAudioUrl = null
        playAudio(isUserAction)
    }

    /**
     * 开始播放音乐*/
    fun playAudio(isUserAction: Boolean){
        //currAudioUrl是空的说明音频不是接着播放的
        if(mCurrAudioUrl==null){
            if(!hasNext(isUserAction)){
                Toast.makeText(mAppContext, "全部播放完毕", Toast.LENGTH_SHORT).show()
                mCurrAudioUrl = null
                return
            }

            //获取第一个并移除确保列表中只有没播放过的
            mCurrAudioUrl = mAudioUrls.pollFirst()
        }

        //服务如果还没有绑定的话先绑定服务
        if(mAudioService==null){
            mAppContext.bindService(Intent(mAppContext,AudioPlayerService::class.java),mAudioServiceConnection, Context.BIND_AUTO_CREATE)
        }else{
            //先请求焦点，能请求着就播放
            if(requestAudioFocus()){
                mCurrAudioUrl?.let {
                    mAudioService?.playUrl(it)
                }
            }
        }
    }

    fun pauseAudio(isUserAction: Boolean){
        mAudioService?.pausePlayer()
    }

    fun stopAudio(){
        mAudioService?.stopPlayer()
    }

    /**
     * 请求音频焦点是否成功*/
    private fun requestAudioFocus():Boolean{
        return if(mCurrAudioUrl==null){
            false
        }else{
            //需要播放就先请求音频焦点
            mAudioFocusManager.requestAudioFocus(mAppContext)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**
     * 放弃焦点是否成功*/
    private fun releaseAudioFocus():Boolean{
        return mAudioFocusManager.releaseFocus()==AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun focusChange(@PlayerFocusConstant focusState: Int) {
        when(focusState){
            PlayerFocusConstant.GOT_FOCUS -> {
                playAudio(false)
            }

            PlayerFocusConstant.LOSS_FOCUS -> {
                pauseAudio(false)
            }
        }
    }

    override fun onStateChanged(@PlayerState state: Int) {
        mAudioPlayerListener?.onStateChanged(state)
        when(state){
            PlayerState.STATE_COMPLETED -> {
                nextAudio(false)
            }

            PlayerState.STATE_ERROR -> {
                releaseAudioFocus()
            }

            PlayerState.STATE_IDLE -> {}
            PlayerState.STATE_INITIALIZED -> {}
            PlayerState.STATE_PAUSED -> {}
            PlayerState.STATE_PREPARED -> {}
            PlayerState.STATE_PREPARING -> {}
            PlayerState.STATE_RELEASED -> {
                releaseAudioFocus()
            }
            PlayerState.STATE_STARTED -> {}
            PlayerState.STATE_STOPPED -> {}
        }
    }

    override fun onPositionChange(position: Int,duration: Int) {
        Log.d(TAG,"onPositionChange:position:${position},duration:${duration}")
        mAudioPlayerListener?.onPositionChange(position,duration)
    }

    override fun onShutdown() {
        mAudioPlayerListener?.onShutdown()
    }

    inner class AudioServiceConnection:ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if(service==null) return
            Log.d(TAG,"onServiceConnected")

            mAudioService = (service as AudioPlayerService.AudioPlayerBinder).getService()
            mAudioService?.setPlayerStateListener(this@AudioPlayerManager)

            if(requestAudioFocus()){
                Log.d(TAG,"onServiceConnected#requestAudioFocus")
                mCurrAudioUrl?.let {
                    Log.d(TAG,"onServiceConnected#requestAudioFocus#mCurrAudioUrl")
                    mAudioService?.playUrl(it)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mAppContext.bindService(Intent(mAppContext,AudioPlayerService::class.java),mAudioServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }
}