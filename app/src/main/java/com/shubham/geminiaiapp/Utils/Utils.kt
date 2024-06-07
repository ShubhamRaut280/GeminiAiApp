package com.shubham.geminiaiapp.Utils

import android.content.Context
import android.widget.Toast

class Utils {
    companion object{

        public fun stoast(context : Context, message : String){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun ltoast(context : Context, message : String){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

    }
}