package fr.esgi.app_4_images_1_word.views

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.view.setPadding
import com.squareup.picasso.Picasso
import android.view.ViewGroup
import android.graphics.drawable.ColorDrawable
import fr.esgi.app_4_images_1_word.R
import fr.esgi.app_4_images_1_word.controllers.*


class MainActivity : AppCompatActivity() {

    //region Variable

    // Variable UI
    private lateinit var toolbarLevel: TextView
    private lateinit var toolbarCoin: TextView
    private lateinit var imageLevel: ImageView
    private lateinit var linearWord: LinearLayout
    private lateinit var gridLetters: GridLayout
    private lateinit var bonusABtn: ImageButton
    private lateinit var bonusBBtn: ImageButton
    private var sizeButton = 0

    // Variable Controller
    private var userController = UserController()
    private lateinit var levelController: LevelController
    private lateinit var firebaseAuthHelper: FirebaseAuthHelper
    private lateinit var firebaseFirestoreHelper: FirebaseFirestoreHelper

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_main)
        sizeButton = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics)).toInt()
        initView()
        initController()
    }

    public override fun onStart() {
        super.onStart()
        firebaseAuthHelper.signIn()
    }

    //region Init Method
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

    private fun initController() {
        levelController = LevelController(userController, this)
        firebaseFirestoreHelper = FirebaseFirestoreHelper(this, levelController, userController)
        firebaseFirestoreHelper.initFirestore()
        firebaseAuthHelper = FirebaseAuthHelper(this, firebaseFirestoreHelper, userController)
        levelController.setFirestore(firebaseFirestoreHelper)
    }

    fun initUI() {
        toolbarLevel.text = getString(R.string.toolbar_level, userController.getActualLevel())
        toolbarCoin.text = "${userController.getCoin()}"
        initUIWord()
        initUIRandomLetters()
        initListenerBonus()
    }

    private fun initUIWord() {
        val actualWord = levelController.getActualLevel().word
        linearWord.removeAllViews()
        (1..actualWord.length).forEach { _ ->
            val view = createWordButton()
            linearWord.addView(view)
        }
    }

    private fun initUIRandomLetters() {
        val letterArray = levelController.randomLetterList()
        for (i in 0 until gridLetters.childCount) {
            val child = gridLetters.getChildAt(i) as Button
            child.setOnClickListener { clickRandomLetter(it, i) }
            child.text = letterArray[i].toString()
        }
    }

    private fun initListenerBonus() {
        bonusABtn.setOnClickListener { bonusAddLetter() }
        bonusBBtn.setOnClickListener { bonusDeleteLetter() }
    }

    //endregion

    //region Letter Logic View

    private fun createBasicButtonView() : Button {
        val view = Button(this)
        val params = LinearLayout.LayoutParams(
            sizeButton,
            sizeButton
        )
        params.setMargins(4,4,4,4)
        view.layoutParams = params
        return view
    }

    private fun createWordButton() : View {
        val btn = createBasicButtonView()
        btn.setBackgroundResource(R.drawable.btn_word_shape)
        btn.setPadding(8)
        btn.text = EMPTY_STRING
        return btn
    }

    private fun createLetterButton(string: String) : View {
        val btn = createBasicButtonView()
        btn.setBackgroundResource(R.drawable.btn_selection_letter_shape)
        if (string.isBlank()) {
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.isEnabled = false
        }
        btn.setPadding(8)
        btn.text = string
        return btn
    }

    private fun clickRandomLetter(view: View?, index: Int) {
        view?.let { thisView ->
            val parent = thisView.parent as ViewGroup
            when (parent.id) {
                R.id.gridRandomLetter -> insertRandomLetter(thisView, index)
                R.id.linearResultWord -> removeRandomLetter(thisView, index)
            }
        }
    }

    private fun insertRandomLetter(view: View, index: Int) {
        for (i in 0 until linearWord.childCount) {
            val child = linearWord.getChildAt(i) as Button
            if (child.text.isBlank()) {
                replaceView(child, view)
                gridLetters.addView(createLetterButton(EMPTY_STRING), index)
                val buttonLetter = view as Button
                levelController.setWordTemp(i, buttonLetter.text.toString())
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
        levelController.setWordTemp(indexLetter, EMPTY_STRING)
    }

    private fun removeView(view: View) {
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
    }

    private fun replaceView(currentView: View, newView: View) {
        val parent = currentView.parent as? ViewGroup
        val index = parent?.indexOfChild(currentView)
        index?.let { thisIndex ->
            removeView(currentView)
            removeView(newView)
            parent.addView(newView, thisIndex)
        }
    }

    //endregion

    fun loadImage(image: String) = Picasso.get().load(image).into(imageLevel)

    fun alert(string: String) = Toast.makeText(this, string, Toast.LENGTH_SHORT).show()

    fun updateCoin() {
        toolbarCoin.text = "${userController.getCoin()}"
    }

    private fun nextLevelUILetters() {
        // Re init transparent button (with bonus)
        for (i in 0 until gridLetters.childCount) {
            val childGrid = gridLetters.getChildAt(i) as Button
            if (childGrid.text == EMPTY_STRING) {
                replaceView(childGrid, createLetterButton(DEFAULT_WORD_LETTER_BUTTON))
            }
        }
        for (i in 0 until linearWord.childCount) {
            val childWord = linearWord.getChildAt(i) as Button
            if (childWord.text != EMPTY_STRING) {
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

    fun winLevel() {
        toolbarCoin.text = "${userController.getCoin()}"
        toolbarLevel.text = getString(R.string.toolbar_level, levelController.getActualLevel().levelNumber)
        loadImage(levelController.getActualLevel().image)
        nextLevelUILetters()
        initUI()
    }

    fun loseLevel() {

    }

    fun finishGame() {
        toolbarLevel.text = getString(R.string.finish_game)
    }

    //region Bonus Add Letter

    private fun bonusAddLetter() = levelController.bonusAddLetter()

    fun insertLetterWithBonus(letter: Char, index: Int) {
        val childWord = linearWord.getChildAt(index) as Button
        var find = false
        for (i in 0 until gridLetters.childCount) {
            val childLetter = gridLetters.getChildAt(i) as Button
            if (childLetter.text == "$letter") {
                replaceView(childWord, childLetter)
                gridLetters.addView(createLetterButton(EMPTY_STRING), i)
                levelController.setWordTemp(index, "$letter")
                find = true
                break
            }
        }
        // lettre pas dans grille donc cherche dans mot => lettre dans mot pas bonne place
        if (!find) {
            for (i in 0 until linearWord.childCount) {
                val childLetter = linearWord.getChildAt(i) as Button
                if (childWord.text == EMPTY_STRING && childLetter.text == "$letter") {
                    levelController.setWordTemp(index, "$letter")
                    levelController.setWordTemp(i, EMPTY_STRING)
                    replaceViewBonusCas1Bis(levelController.getWordTemp())
                    break
                }
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
            if (it == EMPTY_CHAR)
                linearWord.addView(createWordButton())
            else {
                val res = array.find { value -> value.text == "$it" }
                array.remove(res)
                linearWord.addView(res)
            }
        }
    }

    //endregion

    //region Bonus Delete Letter

    private fun bonusDeleteLetter() = levelController.bonusDeleteLetter()

    fun getMissingPositionLettersGrid() : MutableMap<Int, Button> {
        val array : MutableMap<Int, Button> = mutableMapOf()
        for (i in 0 until gridLetters.childCount) {
            val child = gridLetters.getChildAt(i) as Button
            if (child.text != EMPTY_STRING && !(levelController.getActualLevel().word.contains(child.text))) {
                array[i] = child
            }
        }
        return array
    }

    fun bonusRemoveLetter(view: View) {
        val viewWord = createLetterButton(EMPTY_STRING)
        replaceView(view, viewWord)
    }

    //endregion
}
