package com.baishengye.audioplayerdemo


/**
 * 焦点变化回调*/
interface AudioFocusListener {
    fun focusChange(@PlayerFocusConstant focusState:Int)
}