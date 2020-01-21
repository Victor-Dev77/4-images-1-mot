package fr.esgi.app_4_images_1_word.views

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import androidx.core.view.setPadding
import com.squareup.picasso.Picasso
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.models.User
import android.view.ViewGroup
import android.graphics.drawable.ColorDrawable
import fr.esgi.app_4_images_1_word.R
import fr.esgi.app_4_images_1_word.controllers.FirebaseAuthHelper
import fr.esgi.app_4_images_1_word.controllers.FirebaseFirestoreHelper
import fr.esgi.app_4_images_1_word.controllers.LevelController
import fr.esgi.app_4_images_1_word.controllers.UserController


class MainActivity : AppCompatActivity() {

    // Variable UI
    private lateinit var toolbarLevel: TextView
    private lateinit var toolbarCoin: TextView
    private lateinit var imageLevel: ImageView
    private lateinit var linearWord: LinearLayout
    private lateinit var gridLetters: GridLayout
    private lateinit var bonusABtn: ImageButton
    private lateinit var bonusBBtn: ImageButton

    // Variable Controller
    private var userController = UserController()
    private lateinit var levelController: LevelController
    private lateinit var firebaseAuthHelper: FirebaseAuthHelper
    private lateinit var firebaseFirestoreHelper: FirebaseFirestoreHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_main)
        initView()

        levelController = LevelController(userController, this)
        firebaseFirestoreHelper = FirebaseFirestoreHelper(this, levelController, userController)
        firebaseFirestoreHelper.initFirestore()
        firebaseAuthHelper = FirebaseAuthHelper(this, firebaseFirestoreHelper, userController)
        levelController.setFirestore(firebaseFirestoreHelper)

    }

    public override fun onStart() {
        super.onStart()
        firebaseAuthHelper.signIn()
    }

    private fun initView() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbarLevel = findViewById(R.id.toolbar_level_tv)
        toolbarCoin = findViewById(R.id.toolbar_coin_tv)
        imageLevel = findViewById(R.id.imageLevel)
        linearWord = findViewById(R.id.linearResultWord)
        gridLetters = findViewById(R.id.gridRandomLetter)
        bonusABtn = findViewById(R.id.bonusA)
        bonusBBtn = findViewById(R.id.bonusB)
    }

    fun loadImage(image: String) {
        Picasso.get().load(image).into(imageLevel)
    }

    fun initUI() {
        toolbarLevel.text = "Niveau ${userController.getActualLevel()}"
        toolbarCoin.text = "${userController.getCoin()}"
        val actualWord = levelController.getActualLevel().getWord()
        // Update Word
        linearWord.removeAllViews()
        (1..actualWord.length).forEach {
            val view = createWordButton()
            linearWord.addView(view)
        }

        // Update Random Letters
        val letterArray = levelController.randomLetterList()
        for (i in 0 until gridLetters.childCount) {
            val child = gridLetters.getChildAt(i) as Button
            child.setOnClickListener { clickRandomLetter(it, i) }
            child.text = letterArray[i].toString()
        }

        // init bonus
        bonusABtn.setOnClickListener {
            Log.d("toto", "Bonus en HAUT")
            bonusAddLetter()
        }

        bonusBBtn.setOnClickListener {
            Log.d("toto", "Bonus en BAS")
            bonusDeleteLetter()
        }
    }

    fun updateCoin() {
        toolbarCoin.text = "${userController.getCoin()}"
    }

    private fun createWordButton() : View {
        val view = Button(this)
        val params = LinearLayout.LayoutParams(
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)).toInt(),
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)).toInt()
        )
        params.setMargins(4,4,4,4)
        view.layoutParams = params
        view.setBackgroundResource(R.drawable.btn_word_shape)
        view.setPadding(8)
        view.text = " " // const final NULL_TEXT
        return view
    }

    private fun createLetterButton(string: String) : View {
        val view = Button(this)
        val params = LinearLayout.LayoutParams(
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)).toInt(),
            (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)).toInt()
        )
        params.setMargins(4,4,4,4)
        view.layoutParams = params
        view.setBackgroundResource(R.drawable.btn_selection_letter_shape)
        if (string == " ") {
            view.setBackgroundColor(Color.TRANSPARENT)
            view.isEnabled = false
        }
        view.setPadding(8)
        view.text = string // const final NULL_TEXT
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
            val child = linearWord.getChildAt(i) as Button
            if (child.text == " ") {
                replaceView(child, view)
                gridLetters.addView(createLetterButton(" "), index)
                val buttonLetter = view as Button
                levelController.setWordTemp(i, buttonLetter.text.toString())
                Log.d("toto", "Word: ${levelController.getWordTemp()} - lenght: ${levelController.getWordTemp().length}")
                break
            }
        }
        levelController.verifyEndLevel()
    }

    private fun removeRandomLetter(view: View, index: Int) {
        val viewWord = createWordButton()
        val indexLetter = (view.parent as ViewGroup).indexOfChild(view)
        replaceView(view, viewWord)
        gridLetters.removeViewAt(index)
        gridLetters.addView(view, index)
        levelController.setWordTemp(indexLetter, " ")
        Log.d("toto", "Word: ${levelController.getWordTemp()} - lenght: ${levelController.getWordTemp().length}")
    }

    private fun removeView(view: View) {
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
    }

    private fun replaceView(currentView: View, newView: View) {
        val parent = currentView.parent as? ViewGroup
        val index = parent?.indexOfChild(currentView)
        Log.d("toto", "index replace $index")
        if (index != null) {
            removeView(currentView)
            removeView(newView)
            parent.addView(newView, index)
        }
    }

    fun winLevel() {
        toolbarCoin.text = "${userController.getCoin()}"
        toolbarLevel.text = "Niveau ${levelController.getActualLevel().getLevelNumber()}"
        loadImage(levelController.getActualLevel().getImage())
        nextLevelUILetters()
        initUI()
    }

    fun loseLevel() {

    }

    fun finishGame() {
        toolbarLevel.text = "JEU FINI !"
    }

    private fun nextLevelUILetters() {
        // Re init transparent button (with bonus)
        for (i in 0 until gridLetters.childCount) {
            val childGrid = gridLetters.getChildAt(i) as Button
            if (childGrid.text == " ") {
                replaceView(childGrid, createLetterButton("a"))
            }
        }
        for (i in 0 until linearWord.childCount) {
            val childWord = linearWord.getChildAt(i) as Button
            if (childWord.text != " ") {
                for (j in 0 until gridLetters.childCount) {
                    val child = gridLetters.getChildAt(j) as Button
                    val background = child.background
                    if (background is ColorDrawable)
                        if (background.color == Color.TRANSPARENT) {
                            removeRandomLetter(childWord, j)
                            break
                        }
                }
            }
        }
    }

    private fun bonusAddLetter() {
        levelController.bonusAddLetter()
    }

    fun alert(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }


     fun insertLetterWithBonus(letter: Char, index: Int) {
        val childWord = linearWord.getChildAt(index) as Button
        var find = false
        for (i in 0 until gridLetters.childCount) {
            val childLetter = gridLetters.getChildAt(i) as Button
            if (childLetter.text == "$letter") {
                replaceView(childWord, childLetter)
                gridLetters.addView(createLetterButton(" "), i)
                levelController.setWordTemp(index, "$letter")
                find = true
                break
            }
        }
        // lettre pas dans grille donc cherche dans mot => lettre dans mot pas bonne place
        if (!find) {
            for (i in 0 until linearWord.childCount) {
                val childLetter = linearWord.getChildAt(i) as Button
                // CAS N1: si lettre index == vide && lettre i == lettre =>
                // => remplace word index ' ' par lettre + remplace i lettre par ' '
                // CAS N2: lettre index == autre lettre && lettre i == lettre =>
                // => save word index autre lettre + remplace word index autre lettre par lettre
                // + remplace i lettre par ' ' + mettre index autre lettre dans grid

                // CAS 1
                if (childWord.text == " " && childLetter.text == "$letter") {
                    //replaceViewBonusCas1(childWord, childLetter)
                    //linearWord.addView(createWordButton(), i)
                    levelController.setWordTemp(index, "$letter")
                    levelController.setWordTemp(i, " ")
                    replaceViewBonusCas1Bis(levelController.getWordTemp())
                    break
                }

                // CAS 2
               /* else if (childWord.text != " " && childLetter.text == "$letter") {
                    replaceViewBonusCas2(childWord, childLetter)
                    linearWord.addView(createWordButton(), i)
                    word = StringBuilder(word).replace(index, index + 1, "$letter").toString()
                    word = StringBuilder(word).replace(i, i + 1, " ").toString()
                    Log.d("toto", "not find CAS 2 word bonus => $word")
                    break
                }*/
            }
        }
    }

    private fun replaceViewBonusCas1Bis(word: String) {
        val array = ArrayList<Button>()
        for (i in 0 until linearWord.childCount) {
            val child = linearWord.getChildAt(i) as Button
            array.add(child)
        }
        linearWord.removeAllViews()
        word.forEach {
            if (it == ' ')
                linearWord.addView(createWordButton())
            else {
                val res = array.find { value -> value.text == "$it" }
                array.remove(res)
                linearWord.addView(res)
            }
        }
    }

    private fun replaceViewBonusCas1(currentView: View, newView: View) {
        val parent = currentView.parent as? ViewGroup
        val index = parent?.indexOfChild(currentView)
        Log.d("toto", "index replace $index")
        if (index != null) {
            removeView(currentView)
            removeView(newView)
            parent.addView(newView, index )
        }
    }

    private fun replaceViewBonusCas2(currentView: View, newView: View) {
        val parent = currentView.parent as? ViewGroup
        val index = parent?.indexOfChild(currentView)
        Log.d("toto", "index replace $index")
        if (index != null) {
            removeView(currentView)
            removeView(newView)
            parent.addView(newView, index )
            for (i in 0 until gridLetters.childCount) {
                val child = gridLetters.getChildAt(i) as Button
                val background = child.background
                if (background is ColorDrawable)
                    if (background.color == Color.TRANSPARENT) {
                        gridLetters.removeViewAt(i)
                        gridLetters.addView(currentView, i)
                        break
                    }
            }
        }
    }


    private fun bonusDeleteLetter() {
        levelController.bonusDeleteLetter()
    }

     fun getMissingPositionLettersGrid() : MutableMap<Int, Button> {
        val array : MutableMap<Int, Button> = mutableMapOf()
        for (i in 0 until gridLetters.childCount) {
            val child = gridLetters.getChildAt(i) as Button
            if (child.text != " " && !(levelController.getActualLevel().getWord().contains(child.text))) {
                array[i] = child
            }
        }
        return array
    }

     fun bonusRemoveLetter(view: View, index: Int) {
        val viewWord = createLetterButton(" ")
        replaceView(view, viewWord)
    }

}
