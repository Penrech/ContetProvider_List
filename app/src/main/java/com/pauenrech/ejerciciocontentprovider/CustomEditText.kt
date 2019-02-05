package com.pauenrech.ejerciciocontentprovider

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent

class CustomEditText: AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            this.clearFocus()
            return false
        }
        return super.onKeyPreIme(keyCode, event)
    }
}