package com.ar.utilities

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object Utils {
    fun printToast(input: String, context: Context) {
        val toast =
            Toast.makeText(context, input, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}