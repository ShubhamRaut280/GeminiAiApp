package com.shubham.geminiaiapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.shubham.geminiaiapp.Models.ChatModel
import com.shubham.geminiaiapp.R
import com.shubham.geminiaiapp.Utils.TextItemDiffCallback

class ChatAdapter : ListAdapter<ChatModel, ChatAdapter.TextItemViewHolder>(TextItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
        return TextItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.promptTextView.text = item.prompt
        holder.promptTextView.visibility = VISIBLE
        if(item.response!=null) {
            holder.responseTextView.text = item.response
            holder.responseTextView.visibility = VISIBLE
        }        else
            holder.responseTextView.visibility = GONE

    }


    class TextItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val promptTextView: MaterialTextView = view.findViewById(R.id.prompt)
        val responseTextView: MaterialTextView = view.findViewById(R.id.response)
    }

}
