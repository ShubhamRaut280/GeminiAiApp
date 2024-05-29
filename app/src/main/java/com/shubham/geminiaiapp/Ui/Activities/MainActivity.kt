package com.shubham.geminiaiapp.Ui.Activities

import android.graphics.ColorSpace.Model
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.shubham.geminiaiapp.R
import com.shubham.geminiaiapp.Utils.Constants.Companion.API_KEY
import com.shubham.geminiaiapp.Utils.Constants.Companion.MODEL_NAME
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init();
    }

    private fun init(){
        val generativeModel = GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY
        )

        MainScope().launch {
            val prompt = "hello "
            val response = generativeModel.generateContent(prompt)
                Toast.makeText(this@MainActivity, response.text, Toast.LENGTH_SHORT).show()
        }
    }
}