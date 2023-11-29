package com.baishengye.audioplayerdemo

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log


class AudioFocusManager {
    private var mAudioManager: AudioManager? = null
    private var mAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private var mListener: AudioFocusListener? = null
    private var mAudioFocusRequest: AudioFocusRequest? = null

    /**
     * 设置焦点监听*/
    fun setAudioFocusListener(listener: AudioFocusListener){
        mListener = listener
    }

    /**
     * 请求音频焦点*/
    fun requestAudioFocus(context: Context): Int {
        mListener?.focusChange(PlayerFocusConstant.LOSS_FOCUS)

        if (mAudioManager == null) {
            mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener =
                AudioManager.OnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            // 重新获取
                            // 网易云音乐暂停,通话结束，微信聊天结束，录音播放完毕 // 8.0测试机可以收到 audio_focus_gain ，10.0不可以
                            mListener?.focusChange(PlayerFocusConstant.GOT_FOCUS)
                        }

                        AudioManager.AUDIOFOCUS_LOSS -> {
                            // 长时间丢失
                            // 网易云音乐播放 微信聊天
                            mListener?.focusChange(PlayerFocusConstant.LOSS_FOCUS)
                        }

                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            mListener?.focusChange(PlayerFocusConstant.LOSS_FOCUS)
                        }

                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->                             // 暂时获得焦点，不需要暂停其它已经申请的音频播放，但是需要降低音量
                            // 放大 or 放小音量处理
                            Log.e("YogaAudioManager", "audio_focus_loss_transient_can_duck")
                    }
                }
        }

        //0表示获取焦点失败
        if (mAudioManager == null) return AudioManager.AUDIOFOCUS_REQUEST_FAILED

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0及其以上
            if (mAudioFocusRequest == null) {
                mAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mAudioFocusChangeListener!!)
                    .build()
            }
            // AUDIOFOCUS_REQUEST_FAILED == 0
            // AUDIOFOCUS_REQUEST_GRANTED == 1
            // AUDIOFOCUS_REQUEST_DELAYED == 2
            mAudioManager!!.requestAudioFocus(mAudioFocusRequest!!)
        } else {
            // 8.0以下
            // AUDIOFOCUS_REQUEST_FAILED == 0
            // AUDIOFOCUS_REQUEST_GRANTED == 1
            mAudioManager!!.requestAudioFocus(
                mAudioFocusChangeListener,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    fun releaseFocus(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioFocusRequest != null && mAudioManager != null) {
            mAudioManager!!.abandonAudioFocusRequest(mAudioFocusRequest!!)
        } else {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }
    }
}