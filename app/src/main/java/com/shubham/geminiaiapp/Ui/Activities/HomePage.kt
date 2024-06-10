package com.shubham.geminiaiapp.Ui.Activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shubham.geminiaiapp.Adapters.ChatAdapter
import com.shubham.geminiaiapp.R
import com.shubham.geminiaiapp.ViewModels.HomePageViewModel
import com.shubham.geminiaiapp.databinding.ActivityHomepageBinding

class HomePage : AppCompatActivity() {
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel: HomePageViewModel by viewModels()

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleActivity"
        private const val RESULT_SPEECH = 1;
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

        init()
        setUpDrawer()
        onclickListners()
        login()

        binding.mic.setOnClickListener {
            var intent : Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            try {
                startActivityForResult(intent, RESULT_SPEECH)
                binding.pmpText.setText("")
            } catch (e: ActivityNotFoundException) {
                Toast.makeText( this@HomePage,"Your device doesn't support speech to text", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty()) {
                    binding.send.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty()) {
                    binding.send.visibility = View.VISIBLE
                } else
                    binding.send.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    binding.send.visibility = View.VISIBLE
                }
            }
        }

        binding.pmpText.addTextChangedListener(textWatcher)

        viewModel.chatList.observe(this, Observer { chatList ->
            adapter.submitList(chatList)
            binding.chatRecycler.scrollToPosition(chatList.size)
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.stop.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }





    private fun login() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInButton = binding.navView.getHeaderView(0)
        signInButton.setOnClickListener {
            if (auth.currentUser == null)
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
            val imgUrl = auth.currentUser?.photoUrl.toString()

            val prompt = binding.pmpText.text.toString()
            viewModel.addPrompt(prompt,imgUrl)
            binding.pmpText.setText("")
            viewModel.generateResponse(prompt, imgUrl)
        } else {
            Toast.makeText(this, "Please enter something", Toast.LENGTH_SHORT).show()
        }
        hideKeyboard()
    }

    private fun setUpRecycler(recycler: RecyclerView, adapter: ChatAdapter) {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun setupProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val name = currentUser.displayName
            val imgUrl = currentUser.photoUrl
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.profileName).text = name
            val imgview = binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.profileImg)
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

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        if(requestCode== RESULT_SPEECH && resultCode== RESULT_OK && data!=null){
            val list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (list!=null && list.isNotEmpty()){
                binding.pmpText.setText(list.get(0));
            }
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
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.shutdownTTS()
    }
}
