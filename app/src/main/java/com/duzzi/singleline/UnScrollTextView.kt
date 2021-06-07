package com.duzzi.singleline

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView

class UnScrollTextView(context: Context,attributeSet: AttributeSet):
    TextView(context,attributeSet) {


    override fun canScrollHorizontally(direction: Int): Boolean {
        val canScrollHorizontally = super.canScrollHorizontally(direction)
        Log.d("TAG", "canScrollHorizontally() $canScrollHorizontally")
        return false
    }

}