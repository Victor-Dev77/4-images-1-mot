package fr.esgi.app_4_images_1_word

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import androidx.core.view.setPadding
import com.squareup.picasso.Picasso
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.models.User
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var imageLevel: ImageView
    private lateinit var linearWord: LinearLayout
    private lateinit var gridLetters: GridLayout
    private val listLevels = ArrayList<Level>()
    private lateinit var user: User
    private lateinit var actualLevel: Level
    private var word = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_main)
        val actionBar = getSupportActionBar()
        actionBar!!.setTitle("Niveau: 1")

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
        if (currentUser != null)
            user = User(currentUser.uid, currentUser.displayName as String, 0, "")
        //updateUI(currentUser)
        Log.d("toto", "user: ${currentUser}")

        auth.signInAnonymously()
            .addOnFailureListener { res -> Log.d("toto", "NOP NOP")}
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

    //setting menu in action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar,menu)
        return super.onCreateOptionsMenu(menu)
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_coin -> {
            // User chose the "Print" item
            Toast.makeText(this,"Coin action",Toast.LENGTH_LONG).show()
            true
        }
        android.R.id.home ->{
            Toast.makeText(this,"Home action",Toast.LENGTH_LONG).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
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
            .addOnCanceledListener { Log.d("toto", "errerur loading data")}
            .addOnSuccessListener { result ->
                for (document in result) {
                    val map = document.data
                    val level = Level(document.id, map["image"] as String, map["word"] as String, map["difficulty"] as String)
                    listLevels.add(level)
                    Log.d("toto", "${document.id} => ${document.data}")
                }
                if (listLevels.size > 0) {

                    Picasso.get().load(listLevels.first().image).into(imageLevel)

                    //DownloadImageTask(imageLevel)
                    //    .execute(listLevels.first().image)
                    user.actualLevel = listLevels.first().id
                    actualLevel = listLevels.first()
                    word = " ".repeat(listLevels.first().word.length)
                    updateUI()
                }

            }
            .addOnFailureListener { exception ->
                Log.d("toto", "Error getting documents: ", exception)
            }
    }

    private fun updateUI() {
        // Update Word
        linearWord = findViewById(R.id.linearResultWord)
        (1..actualLevel.word.length).forEach {
            val view = createWordButton()
            linearWord.addView(view)
            Log.d("toto", "ok")
        }

        // Update Random Letters
        gridLetters = findViewById(R.id.gridRandomLetter)
        val STRING_CHARACTERS = ('a'..'z').toList().toTypedArray()
        val letterArray : ArrayList<Char> = actualLevel.word.toList() as ArrayList<Char>
        Log.d("toto", "$letterArray")
        (0 until (12 - actualLevel.word.length)).forEach {
            letterArray.add(STRING_CHARACTERS.random())
        }
        letterArray.shuffle()

        for (i in 0 until gridLetters.childCount) {
            val child = gridLetters.getChildAt(i) as Button
            child.setOnClickListener {
                when (i) {
                    6 -> Log.d("toto", "Bonus en HAUT")
                    13 -> Log.d("toto", "Bonus en BAS")
                    else -> clickRandomLetter(it, i)
                }
            }
            if (child.tag != null)
                child.text = letterArray[(child.tag as String).toInt()].toString()
        }
    }

    private fun createWordButton() : View {
        val view = Button(this)
        val params = LinearLayout.LayoutParams(
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)).toInt(),
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)).toInt()
        )
        params.setMargins(4,4,4,4)
        view.layoutParams = params
        view.setBackgroundResource(R.drawable.btn_word_shape)
        view.setPadding(8)
        view.text = " " // const final NULL_TEXT
        return view
    }

    private fun createLetterButton() : View {
        val view = Button(this)
        val params = LinearLayout.LayoutParams(
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)).toInt(),
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)).toInt()
        )
        params.setMargins(4,4,4,4)
        view.layoutParams = params
        view.setBackgroundResource(R.drawable.btn_selection_letter_shape)
        view.setBackgroundColor(Color.TRANSPARENT)
        view.isEnabled = false
        view.setPadding(8)
        view.text = " " // const final NULL_TEXT
        return view
    }

    private fun clickRandomLetter(view: View?, index: Int) {
        if (view != null) {
            val parent = view.parent as ViewGroup
            when (parent.id) {
                R.id.gridRandomLetter -> insertRandomLetter(view, index)
                R.id.linearResultWord -> removeRandomLetter(view, index)
            }

           // view.isEnabled = !view.isEnabled
        }
    }

    private fun insertRandomLetter(view: View, index: Int) {
        for (i in 0 until linearWord.childCount) {
            var child = linearWord.getChildAt(i) as Button
            if (child.text == " ") {
                replaceView(child, view)
                gridLetters.addView(createLetterButton(), index)
                val buttonLetter = view as Button
                word = StringBuilder(word).replace(i, i + 1, buttonLetter.text.toString()).toString()
                Log.d("toto", "Word: $word - lenght: ${word.length}")
                break
            }
        }
        if (verifyValidWord()) {
            Log.d("toto", "MOT TROUVE !!!")
        } else {
            Log.d("toto", "MOT ERRONNE")
        }
    }

    private fun removeRandomLetter(view: View, index: Int) {
        val viewWord = createWordButton()
        val indexLetter = (view.parent as ViewGroup).indexOfChild(view)
        replaceView(view, viewWord)
        gridLetters.removeViewAt(index)
        gridLetters.addView(view, index)
        Log.d("toto", "IndexLetter: $indexLetter")
        word = StringBuilder(word).replace(indexLetter, indexLetter + 1, " ").toString()
        Log.d("toto", "Word: $word - lenght: ${word.length}")
    }

    private fun removeView(view: View) {
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
    }

    private fun replaceView(currentView: View, newView: View) {
        val parent = currentView.parent as? ViewGroup
        val index = parent?.indexOfChild(currentView)
        if (index != null) {
            removeView(currentView)
            removeView(newView)
            parent.addView(newView, index)
        }
    }


    private fun verifyValidWord() : Boolean {
        if (word.contains(' '))
            return false
        return word.trim().toLowerCase() == actualLevel.word.trim().toLowerCase()
    }


}
