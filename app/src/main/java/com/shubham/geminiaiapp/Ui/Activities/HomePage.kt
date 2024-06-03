package com.shubham.geminiaiapp.Ui.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

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
        setUpDrawer()

        binding.more.setOnClickListener {
            binding.drawerlayout.openDrawer(GravityCompat.START)
        }
    }

    private fun init() {
        adapter = ChatAdapter()
        setUpRecycler(binding.chatRecycler, adapter)
        binding.send.setOnClickListener {
            respond()
        }
    }

    private fun respond() {
        if (binding.pmpText.text.isNotEmpty()) {
            val prompt = binding.pmpText.text.toString()
            addItemToRecycler(prompt, null)
            binding.pmpText.setText("")
            toggleSendButton("stop")

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
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

    private fun addItemToRecycler(prompt: String, res: String?) {
        val chatModel = ChatModel(prompt, res)
        chatList.removeIf { it.prompt == prompt && it.response == null }
        chatList.add(chatModel)
        adapter.submitList(chatList.toList())
        binding.chatRecycler.scrollToPosition(chatList.size)
    }

    private fun setUpRecycler(recycler: RecyclerView, adapter: ChatAdapter) {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun toggleSendButton(arg: String) {
        val icon = if (arg == "send") R.drawable.send else R.drawable.stop
        binding.send.icon = getDrawable(icon)?.apply {
            setTint(getColor(R.color.main))
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setUpDrawer() {
        binding.drawerlayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val moveFactor = binding.navView.width * slideOffset
                binding.main.translationX = moveFactor
            }

            override fun onDrawerOpened(drawerView: View) {
                // Optional: Handle drawer opened event
            }

            override fun onDrawerClosed(drawerView: View) {
                // Optional: Handle drawer closed event
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Optional: Handle drawer state change event
            }
        })

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settingsmenu -> {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                }
                R.id.profilemenu -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
            }
            binding.drawerlayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
