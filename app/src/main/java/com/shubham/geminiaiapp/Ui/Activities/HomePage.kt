package com.shubham.geminiaiapp.Ui.Activities

import android.content.Intent
import android.media.audiofx.Virtualizer
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.navigation.NavigationView
import com.shubham.geminiaiapp.Adapters.ChatAdapter
import com.shubham.geminiaiapp.Models.ChatModel
import com.shubham.geminiaiapp.R
import com.shubham.geminiaiapp.Utils.Constants.Companion.API_KEY
import com.shubham.geminiaiapp.Utils.Constants.Companion.MODEL_NAME
import com.shubham.geminiaiapp.databinding.ActivityHomepageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePage : AppCompatActivity() {
    private lateinit var generativeModel: GenerativeModel
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var adapter: ChatAdapter
    private val chatList = mutableListOf<ChatModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        generativeModel = GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY
        )
        init()
    }

    private fun init() {
        setUpDrawer()
        adapter = ChatAdapter()
        setUpRecycler(binding.chatRecycler, adapter)
        binding.send.setOnClickListener {
            if (binding.pmpText.text.isNotEmpty()) {
                val prompt = binding.pmpText.text.toString()
                addItemToRecycler(prompt, null)
                binding.pmpText.setText("")
                toggleSendButton("stop")

                lifecycleScope.launch {
                   try {
                       val response = withContext(Dispatchers.IO){
                           generativeModel.generateContent(prompt)
                       }

                           addItemToRecycler(prompt, response.text)
                   } catch (e: Exception) {
                       addItemToRecycler(prompt, e.message)
                       e.printStackTrace()
                   }
                    toggleSendButton("send")
                }
            } else {
                Toast.makeText(this, "Please enter something", Toast.LENGTH_SHORT).show()
            }
            hideKeyboard()

        }
    }
    private fun addItemToRecycler(prompt: String, res: String?) {

        val chatModel = ChatModel(prompt, res)
        for (item in chatList) {
            if (item.prompt == prompt && item.response == null) {
                chatList.remove(item)
                break
            }

        }
         chatList.add(chatModel)

        // Submit the updated list to the adapter
        adapter.submitList(chatList.toList())

        // Scroll to the last item in the RecyclerView
        binding.chatRecycler.scrollToPosition(chatList.size )
    }

    private fun setUpDrawer(){

        // Set up the navigation drawer
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item clicks here
            when (menuItem.itemId) {
                R.id.settingsmenu -> {
                    // Navigate to the settings activity or fragment
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                }
                R.id.profilemenu -> {
                    // Navigate to the profile activity or fragment
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
            }
            binding.drawerlayout.closeDrawer(GravityCompat.START)
            true
        }
    }


    private fun setUpRecycler(recycler: RecyclerView, adapter: ChatAdapter) {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun toggleSendButton(arg: String) {
        if (arg == "send") {
            binding.send.icon = getDrawable(R.drawable.send)?.apply {
                setTint(getColor(R.color.main))
            }
        } else {
            binding.send.icon = getDrawable(R.drawable.stop)?.apply {
                setTint(getColor(R.color.main))
            }
        }
    }

    fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
