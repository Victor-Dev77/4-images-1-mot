package fr.esgi.app_4_images_1_word.models

class Level (private val id: String, private val levelNumber: Int, private val image: String, private val word: String, private val difficulty: String) {

    fun getID() : String {
        return id
    }

    fun getLevelNumber() : Int {
        return levelNumber
    }

    fun getImage() : String {
        return image
    }

    fun getWord() : String {
        return word
    }

    fun getDifficulty() : String {
        return difficulty
    }

}