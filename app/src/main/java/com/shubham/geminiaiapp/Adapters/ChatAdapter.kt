package com.shubham.geminiaiapp.Adapters

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.shubham.geminiaiapp.Models.ChatModel
import com.shubham.geminiaiapp.R
import com.shubham.geminiaiapp.Utils.TextItemDiffCallback
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ChatAdapter(private val context: Context) : ListAdapter<ChatModel, ChatAdapter.TextItemViewHolder>(TextItemDiffCallback()), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
        return TextItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = getItem(position)

        holder.promptTextView.text = item.prompt
        holder.promptTextView.visibility = VISIBLE
        if (item.profileUrl != null)
            Glide.with(holder.itemView.context).load(item.profileUrl).into(holder.userImage)

        if (item.response != null) {
            holder.responseTextView.text = item.response
            holder.responseLayout.visibility = VISIBLE
        } else {
            holder.responseTextView.visibility = GONE
        }

        holder.speakButton.setOnClickListener {
            speak(item.response ?: "No response available")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdownTTS() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
    }

    class TextItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val promptTextView: MaterialTextView = view.findViewById(R.id.prompt)
        val responseTextView: MaterialTextView = view.findViewById(R.id.response)
        val responseLayout: RelativeLayout = view.findViewById(R.id.responseLayout)
        val userImage: CircleImageView = view.findViewById(R.id.userImg)
        val speakButton: MaterialButton = view.findViewById(R.id.botImg)
    }
}
