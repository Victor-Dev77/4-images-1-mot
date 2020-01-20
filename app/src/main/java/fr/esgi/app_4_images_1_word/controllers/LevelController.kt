package fr.esgi.app_4_images_1_word.controllers

import android.util.Log
import android.widget.Toast
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.views.MainActivity

class LevelController(private val user: UserController, private val view: MainActivity) {

    private lateinit var actualLevel: Level
    private val listLevels = ArrayList<Level>()
    private var wordTemp = ""


    fun addLevel(level: Level) {
        listLevels.add(level)
    }

    fun getNBLevel() : Int {
        return listLevels.size
    }

    fun getLevel(numLevel: Int) : Level? {
        if  (numLevel < 0 || numLevel >= listLevels.size)
            return null
        return listLevels[numLevel]
    }

    fun getActualLevel(): Level {
        return actualLevel
    }

    fun setActualLevel(level: Level) {
        actualLevel = level
    }

    fun getWordTemp() : String {
        return wordTemp
    }

    fun setWordTemp(word: String) {
        wordTemp = word
    }

    fun setWordTemp(position: Int, word: String) {
        wordTemp = StringBuilder(wordTemp).replace(position, position + 1, word).toString()
    }

    fun randomLetterList(): ArrayList<Char> {
        val actualWord = actualLevel.getWord()
        wordTemp = " ".repeat(actualWord.length)
        // STRING CHAR A METTRE EN CONSTANTE en faut class
        val STRING_CHARACTERS = ('a'..'z').toList().toTypedArray()
        val letterArray : ArrayList<Char> = actualWord.toList() as ArrayList<Char>
        Log.d("toto", "$letterArray")
        (0 until (12 - actualWord.length)).forEach {
            letterArray.add(STRING_CHARACTERS.random())
        }
        letterArray.shuffle()
        return letterArray
    }

    fun verifyEndLevel() {
        if (!(wordTemp.contains(' '))) {
            if (verifyValidWord()) {
                Log.d("toto", "MOT TROUVE !!!")
                if (nextLevel())
                    view.winLevel()
                else
                    view.finishGame()
            } else {
                Log.d("toto", "MOT ERRONNE")
                view.loseLevel()
            }
        }
    }

    private fun verifyValidWord() : Boolean {
        if (wordTemp.contains(' '))
            return false
        return wordTemp.trim().toLowerCase() == actualLevel.getWord().trim().toLowerCase()
    }

    private fun nextLevel() : Boolean {
        user.increaseCoin(100)
        if (listLevels.indexOf(actualLevel) + 1 < listLevels.size) {
            val level = listLevels[listLevels.indexOf(actualLevel) + 1]
            user.setActualLevel(level.getID())
            actualLevel = level
            wordTemp = " ".repeat(level.getWord().length)
            return true

        } else {
            Log.d("toto", "JEU FINI !!!")
            return false
        }
    }

    fun bonusAddLetter() {
        if (user.getCoin() - 80 < 0) {
            view.alert("Pas assez de pièces !")
            return
        }
        if (!(wordTemp.contains(' '))) {
            view.alert("Enlever une lettre pour utiliser le bonus")
            return
        }
        val arrayIndex = getMissingPositionWord(wordTemp)
        val newIndex = (arrayIndex.indices).random()
        val newChar = actualLevel.getWord()[arrayIndex[newIndex]]
        Log.d("toto", "new Char -> $newChar")
        arrayIndex.forEach {
            Log.d("toto", "index array: $it")
        }

        // update UI
        view.insertLetterWithBonus(newChar, arrayIndex[newIndex])

        verifyEndLevel()
    }

    private fun getMissingPositionWord(word: String) : Array<Int> {
        if (!(word.contains(' ')))
            return ArrayList<Int>().toTypedArray()
        val array = ArrayList<Int>()
        for (i in word.indices) {
            if (word[i] == ' ') { // CAS 2 || word[i] != actualLevel.word[i]) {
                array.add(i)
            }
        }
        return array.toTypedArray()
    }

    fun bonusDeleteLetter() {
        if (user.getCoin() - 60 < 0) {
            view.alert("Pas assez de pièces !")
            return
        }
        if (!(wordTemp.contains(' '))) {
            view.alert("Enlever une lettre pour utiliser le bonus")
            return
        }

        val arrayIndex = view.getMissingPositionLettersGrid()
        if (arrayIndex.isEmpty()) {
            view.alert("Toutes les lettres sont supprimées")
            return
        }
        val newIndex = (arrayIndex.keys).random()
        Log.d("toto", "array: $arrayIndex, index: $newIndex")
        view.bonusRemoveLetter(arrayIndex[newIndex]!!, newIndex)
        user.decreaseCoin(60)
        view.updateCoin()
    }

}