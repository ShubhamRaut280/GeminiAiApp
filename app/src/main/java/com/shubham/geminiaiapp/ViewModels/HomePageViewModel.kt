package com.shubham.geminiaiapp.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.shubham.geminiaiapp.Models.ChatModel
import com.shubham.geminiaiapp.Utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePageViewModel : ViewModel() {
    private val _chatList = MutableLiveData<List<ChatModel>>()
    val chatList: LiveData<List<ChatModel>> get() = _chatList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val generativeModel = GenerativeModel(
        modelName = Constants.MODEL_NAME,
        apiKey = Constants.API_KEY
    )

    private val currentChatList = mutableListOf<ChatModel>()

    fun addPrompt(prompt: String) {
        currentChatList.add(ChatModel(prompt, null, ""))
        _chatList.value = currentChatList.toList()
    }

    fun generateResponse(prompt: String, userImageUrl: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                  if(chatList.value.isNullOrEmpty())
                      generativeModel.generateContent(prompt)
                    else
                        multimodalresponse(prompt);

                }
                val chatModel = ChatModel(prompt, response.text, userImageUrl)
                currentChatList.removeIf { it.prompt == prompt && it.response == null }
                currentChatList.add(chatModel)
                _chatList.value = currentChatList.toList()
            } catch (e: Exception) {
                val chatModel = ChatModel(prompt, e.message, userImageUrl)
                currentChatList.removeIf { it.prompt == prompt && it.response == null }
                currentChatList.add(chatModel)
                _chatList.value = currentChatList.toList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun multimodalresponse(prompt: String): GenerateContentResponse {
        val history = mutableListOf<Content>()

        // Iterate through the current chat list
        currentChatList.forEach { chatModel ->
            // Check if the response is not null
            chatModel.response?.let { response ->
                // Add user message to history
                history.add(content(role = "user") { text(chatModel.prompt) })
                // Add model response to history
                history.add(content(role = "model") { text(response) })
            }
        }
        val chat = generativeModel.startChat(history = history)

       val response = chat.sendMessage(prompt)
        return response;
    }




}

