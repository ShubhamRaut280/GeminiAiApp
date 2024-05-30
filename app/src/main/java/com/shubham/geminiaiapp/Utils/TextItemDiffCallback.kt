package com.shubham.geminiaiapp.Utils

import androidx.recyclerview.widget.DiffUtil
import com.shubham.geminiaiapp.Models.ChatModel

class TextItemDiffCallback : DiffUtil.ItemCallback<ChatModel>() {
    override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
        // Assuming each item has a unique identifier
        return oldItem.response == newItem.response && oldItem.prompt == newItem.prompt
    }

    override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
        // Check if the contents of the items are the same
        return oldItem == newItem
    }
}
