package com.example.lab8
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class MainActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        val userStatusTV = findViewById<TextView>(R.id.textViewUserStatus)
        userStatusTV.text = "You need to sign up or sign in!"

        //remote config
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val appTheme = remoteConfig.getString("app_theme")

                if (appTheme == "dark") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else if (appTheme == "light") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                val value1 = remoteConfig.getString("value_1")
                val value2 = remoteConfig.getString("value_2")
                val value3 = remoteConfig.getString("value_3")
                val value4 = remoteConfig.getString("value_4")
                val value5 = remoteConfig.getString("value_5")

                findViewById<TextView>(R.id.textView).text = value1
                findViewById<TextView>(R.id.textView2).text = value2
                findViewById<TextView>(R.id.textView3).text = value3
                findViewById<TextView>(R.id.textView4).text = value4
                findViewById<TextView>(R.id.textView5).text = value5
            } else {
                Log.w("RemoteConfig", "Failed to fetch initial config.")
            }
        }

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d("RemoteConfig", "Updated keys: ${configUpdate.updatedKeys}")

                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "Config activation successful")

                        runOnUiThread {
                            configUpdate.updatedKeys.forEach { key ->
                                when (key) {
                                    "app_theme" -> {
                                        val appTheme = remoteConfig.getString("app_theme")
                                        if (appTheme == "dark") {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                        } else if (appTheme == "light") {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                        }
                                    }
                                    "value_1" -> {
                                        val value1 = remoteConfig.getString("value_1")
                                        findViewById<TextView>(R.id.textView).text = value1
                                    }
                                    "value_2" -> {
                                        val value2 = remoteConfig.getString("value_2")
                                        findViewById<TextView>(R.id.textView2).text = value2
                                    }
                                    "value_3" -> {
                                        val value3 = remoteConfig.getString("value_3")
                                        findViewById<TextView>(R.id.textView3).text = value3
                                    }
                                    "value_4" -> {
                                        val value4 = remoteConfig.getString("value_4")
                                        findViewById<TextView>(R.id.textView4).text = value4
                                    }
                                    "value_5" -> {
                                        val value5 = remoteConfig.getString("value_5")
                                        findViewById<TextView>(R.id.textView5).text = value5
                                    }
                                }
                            }
                        }
                    } else {
                        Log.w("RemoteConfig", "Failed to activate config update.")
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("RemoteConfig", "Error occurred while fetching remote config", error)
            }
        })

    }

    fun updateUI(msg: String)
    {
        val userStatusTV = this.findViewById<TextView>(R.id.textViewUserStatus)
        userStatusTV.text = msg
    }

    fun signInOnClick(view: View) {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("FIREBASE_SIGNIN", "signInWithEmail:success")
                        updateUI("You successfully Signed In: " + auth.currentUser!!.email.toString())
                        Toast.makeText(
                            this,
                            "Sign in successful.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.w("FIREBASE_SIGNIN", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI("Authentication failed.")
                        Firebase.auth.signOut()
                    }
                }
        } else {
            Toast.makeText(this, "Email and Password cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }

    fun signUpOnClick(view: View) {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this)
                {
                    task ->
                    if (task.isSuccessful) {
                        Log.d("FIREBASE_SIGNUP", "createUserWithEmail:success")
                        updateUI("You successfully Signed Up: " + auth.currentUser!!.email.toString())
                        Toast.makeText(
                            this,
                            "Authentication successful.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.w("FIREBASE_SIGNUP", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI("Sign Up failed.")
                        Firebase.auth.signOut()
                    }
                }
        } else {
            Toast.makeText(this, "Email and Password cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }

    fun logoutOnClick(view: View) {
        Firebase.auth.signOut()
        updateUI("You need to sign in or to sign up!")
    }

    fun testCrashOnClick(view: View) {
        throw RuntimeException("Test Crash")
    }

    //...
    //CRUD
    fun createBook(view: View) {
        val title = findViewById<EditText>(R.id.editTextTitle).text.toString()
        val author = findViewById<EditText>(R.id.editTextAuthor).text.toString()
        val publishedYear = findViewById<EditText>(R.id.editTextPublishedYear).text.toString()

        if (title.isNotEmpty() && author.isNotEmpty() && publishedYear.isNotEmpty()) {
            val book = hashMapOf("title" to title, "author" to author, "publishedYear" to publishedYear)
            db.collection("books").add(book)
                .addOnSuccessListener {
                    Toast.makeText(this, "Book created successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_CREATE", "Error adding document", e)
                    Toast.makeText(this, "Failed to create book", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
        }
    }

    fun readBook(view: View) {
        val bookId = findViewById<EditText>(R.id.editTextBookId).text.toString().filter{ !it.isWhitespace() }

        if (bookId.isNotEmpty()) {
            db.collection("books").document(bookId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        findViewById<EditText>(R.id.editTextTitle).setText(document.getString("title"))
                        findViewById<EditText>(R.id.editTextAuthor).setText(document.getString("author"))
                        findViewById<EditText>(R.id.editTextPublishedYear).setText(document.getString("publishedYear"))
                        Toast.makeText(this, "Book read successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No such book found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_READ", "Error reading document", e)
                    Toast.makeText(this, "Failed to read book", Toast.LENGTH_SHORT).show()
                }
        }
        else
        {
            Toast.makeText(this, "Book ID must be provided", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateBook(view: View) {
        val bookId = findViewById<EditText>(R.id.editTextBookId).text.toString().filter{ !it.isWhitespace() }
        val title = findViewById<EditText>(R.id.editTextTitle).text.toString()
        val author = findViewById<EditText>(R.id.editTextAuthor).text.toString()
        val publishedYear = findViewById<EditText>(R.id.editTextPublishedYear).text.toString()

        if (bookId.isNotEmpty() && title.isNotEmpty() && author.isNotEmpty() && publishedYear.isNotEmpty()) {
            val book = hashMapOf("title" to title, "author" to author, "publishedYear" to publishedYear)
            db.collection("books").document(bookId).set(book)
                .addOnSuccessListener {
                    Toast.makeText(this, "Book updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_UPDATE", "Error updating document", e)
                    Toast.makeText(this, "Failed to update book", Toast.LENGTH_SHORT).show()
                }
        }
        else {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteBook(view: View) {
        val bookId = findViewById<EditText>(R.id.editTextBookId).text.toString().filter{ !it.isWhitespace() }

        if (bookId.isNotEmpty()) {
            db.collection("books").document(bookId).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Book deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_DELETE", "Error deleting document", e)
                    Toast.makeText(this, "Failed to delete book", Toast.LENGTH_SHORT).show()
                }
        }
        else {
            Toast.makeText(this, "Book ID must be provided", Toast.LENGTH_SHORT).show()
        }
    }
}