package com.shubham.geminiaiapp.Ui.Activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
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
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleActivity"
    }
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

        auth = FirebaseAuth.getInstance()

        generativeModel = GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY
        )

        init()
        setUpDrawer()

        onclickListners()

        login()


        val textWatcher  = object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty()) {
                    binding.send.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().isNotEmpty()){
                    binding.send.visibility = View.VISIBLE
                }else
                    binding.send.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0.toString().isNotEmpty()) {
                    binding.send.visibility = View.VISIBLE
                }
            }

        }


        binding.pmpText.addTextChangedListener(textWatcher);

    }

    private fun login() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set the dimensions of the sign-in button.
        val signInButton = binding.navView.getHeaderView(0)
        signInButton.setOnClickListener {
            if(auth.currentUser==null)
                signIn()

        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            setupProfile()
        }
    }

    private fun onclickListners() {
        binding.more.setOnClickListener {
            binding.drawerlayout.openDrawer(GravityCompat.START)
        }
    }

    private fun init() {
        adapter = ChatAdapter(this)
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
            binding.stop.visibility = View.VISIBLE

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
                binding.stop.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "Please enter something", Toast.LENGTH_SHORT).show()
        }
        hideKeyboard()
    }


    private fun addItemToRecycler(prompt: String, res: String?) {
        val imgUrl = auth.currentUser?.photoUrl.toString()
        val chatModel = ChatModel(prompt, res, imgUrl )
        chatList.removeIf { it.prompt == prompt && it.response == null }
        chatList.add(chatModel)
        adapter.submitList(chatList.toList())
        binding.chatRecycler.scrollToPosition(chatList.size)
    }

    private fun setUpRecycler(recycler: RecyclerView, adapter: ChatAdapter) {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }


    private fun setupProfile(){
        val currentUser = auth.currentUser
        if (currentUser!=null){
            val name = currentUser.displayName
            val imgUrl = currentUser.photoUrl
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.profileName).text = name
            val imgview  =  binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.profileImg)
            Glide.with(this).load(imgUrl).into(imgview)
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

    // Handle Google sign in

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    setupProfile()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    // Update UI or navigate to another activity
                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()

                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // Update UI
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.shutdownTTS()
    }
}
