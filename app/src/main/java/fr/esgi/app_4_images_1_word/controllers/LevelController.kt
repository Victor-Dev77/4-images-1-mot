package fr.esgi.app_4_images_1_word.controllers

import android.util.Log
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.views.MainActivity

class LevelController(private val user: UserController,
                      private val view: MainActivity) {

    private lateinit var actualLevel: Level
    private val listLevels = ArrayList<Level>()
    private var wordTemp = ""
    private var firestore = FirebaseFirestoreHelper(view, this, user)

    fun setFirestore(firestore: FirebaseFirestoreHelper) {
        this.firestore = firestore
    }


    fun addLevel(level: Level) = listLevels.add(level)


    fun getNBLevel() : Int = listLevels.size

    fun getLevel(numLevel: Int) : Level? = if (numLevel < 0 || numLevel >= listLevels.size) null else listLevels[numLevel]


    fun getActualLevel(): Level = actualLevel

    fun setActualLevel(level: Level) {
        actualLevel = level
    }

    fun getWordTemp() : String = wordTemp

    fun setWordTemp(word: String) {
        wordTemp = word
    }

    fun setWordTemp(position: Int, word: String) {
        wordTemp = StringBuilder(wordTemp).replace(position, position + 1, word).toString()
    }

    fun randomLetterList(): ArrayList<Char> {
        val actualWord = actualLevel.word
        wordTemp = EMPTY_STRING.repeat(actualWord.length)
        val letterArray : ArrayList<Char> = actualWord.toList() as ArrayList<Char>
        (0 until (12 - actualWord.length)).forEach { _ ->
            letterArray.add(STRING_CHARACTERS.random())
        }
        letterArray.shuffle()
        return letterArray
    }

    fun verifyEndLevel() {
        if (!(wordTemp.contains(EMPTY_CHAR))) {
            if (verifyValidWord()) {
                view.alert(WORD_FIND)
                if (nextLevel())
                    view.winLevel()
                else
                    view.finishGame()
            } else {
                view.alert(WORD_ERROR)
                view.loseLevel()
            }
        }
    }

    private fun verifyValidWord() : Boolean {
        return if (wordTemp.contains(EMPTY_CHAR))
                   false
               else
                   wordTemp.trim().toLowerCase() == actualLevel.word.trim().toLowerCase()
    }

    private fun nextLevel() : Boolean {
        user.increaseCoin(PRICE_WIN_LEVEL)
        return if (listLevels.indexOf(actualLevel) + 1 < listLevels.size) {
            val level = listLevels[listLevels.indexOf(actualLevel) + 1]
            user.setActualLevel(level.levelNumber)
            actualLevel = level
            wordTemp = EMPTY_STRING.repeat(level.word.length)
            firestore.saveLevel(user.getUser())
            true

        } else {
            Log.d("toto", "JEU FINI !!!")
            view.alert(FINISH_GAME)
            false
        }
    }

    fun bonusAddLetter() {
        if (user.getCoin() - PRICE_BONUS_ADD_LETTER < 0) {
            view.alert(ALERT_MISSING_COIN)
            return
        }
        if (!(wordTemp.contains(EMPTY_CHAR))) {
            view.alert(ALERT_MANY_LETTERS)
            return
        }
        val arrayIndex = getMissingPositionWord(wordTemp)
        val newIndex = (arrayIndex.indices).random()
        val newChar = actualLevel.word[arrayIndex[newIndex]]
        Log.d("toto", "new Char -> $newChar")
        arrayIndex.forEach {
            Log.d("toto", "index array: $it")
        }

        // update UI
        view.insertLetterWithBonus(newChar, arrayIndex[newIndex])
        user.decreaseCoin(PRICE_BONUS_ADD_LETTER)
        view.updateCoin()
        verifyEndLevel()
    }

    private fun getMissingPositionWord(word: String) : Array<Int> {
        if (!(word.contains(EMPTY_CHAR)))
            return ArrayList<Int>().toTypedArray()
        val array = ArrayList<Int>()
        for (i in word.indices) {
            if (word[i] == EMPTY_CHAR) {
                array.add(i)
            }
        }
        return array.toTypedArray()
    }

    fun bonusDeleteLetter() {
        if (user.getCoin() - PRICE_BONUS_DELETE_LETTER < 0) {
            view.alert(ALERT_MISSING_COIN)
            return
        }
        if (!(wordTemp.contains(EMPTY_CHAR))) {
            view.alert(ALERT_MANY_LETTERS)
            return
        }

        val arrayIndex = view.getMissingPositionLettersGrid()
        if (arrayIndex.isEmpty()) {
            view.alert(ALERT_EMPTY_WORD)
            return
        }
        val newIndex = (arrayIndex.keys).random()
        Log.d("toto", "array: $arrayIndex, index: $newIndex")
        view.bonusRemoveLetter(arrayIndex[newIndex]!!)
        user.decreaseCoin(PRICE_BONUS_DELETE_LETTER)
        view.updateCoin()
    }

}