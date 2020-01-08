package fr.esgi.app_4_images_1_word

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setPadding
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.models.User


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var imageLevel: ImageView
    private lateinit var linearWord: LinearLayout
    private lateinit var btnTest: Button
    private val listLevels = ArrayList<Level>()
    private lateinit var user: User
    private lateinit var actualLevel: Level

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_main)
        imageLevel = findViewById(R.id.imageLevel)
        auth = FirebaseAuth.getInstance()
        setup()
        setupCacheSize()
        getAllLevels()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        user = User(auth.currentUser?.uid as String, auth.currentUser?.displayName as String, 0, "")
        //updateUI(currentUser)
        Log.d("toto", "user: ${currentUser}")

        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("toto", "signInAnonymously:success")
                    this.user = User(auth.currentUser?.uid as String, auth.currentUser?.displayName as String, 0, "")
                    Log.d("toto", "user: ${auth.currentUser!!.isAnonymous}")
                   // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("toto", "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                // ...
            }
    }

    private fun setup() {
        // [START get_firestore_instance]
        db = FirebaseFirestore.getInstance()
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        // [END set_firestore_settings]
    }

    private fun setupCacheSize() {
        // [START fs_setup_cache]
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
        // [END fs_setup_cache]
    }

    private fun getAllLevels() {
        db.collection("levels")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val map = document.data
                    val level = Level(document.id, map["image"] as String, map["word"] as String, map["difficulty"] as String)
                    listLevels.add(level)
                    Log.d("toto", "${document.id} => ${document.data}")
                }
                DownloadImageTask(imageLevel)
                    .execute(listLevels.first().image)
                user.actualLevel = listLevels.first().id
                actualLevel = listLevels.first()
                updateUI()
            }
            .addOnFailureListener { exception ->
                Log.d("toto", "Error getting documents: ", exception)
            }
    }

    private fun updateUI() {
        linearWord = findViewById(R.id.linearResultWord)
        btnTest = findViewById(R.id.btnTest)
        (1..actualLevel.word.length).forEach {
            var view = Button(this)
            var params = LinearLayout.LayoutParams(
                100,
                100
            )
            params.setMargins(4,4,4,4)
            view.layoutParams = params
            view.setBackgroundResource(R.drawable.btn_word_shape)
            view.setPadding(8)
            view.id = it
            view.text = "A"
            view.width = 50
            view.height = 50
            linearWord.addView(view)
            Log.d("toto", "ok")
        }
    }
}



// Utiliser la librairie Picasso !!
private class DownloadImageTask(internal var bmImage: ImageView) :
    AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        val urldisplay = urls[0]
        var mIcon11: Bitmap? = null
        try {
            val `in` = java.net.URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(`in`)
        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }

        return mIcon11
    }

    override fun onPostExecute(result: Bitmap) {
        bmImage.setImageBitmap(result)
    }
}
