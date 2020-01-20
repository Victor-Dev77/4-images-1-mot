package fr.esgi.app_4_images_1_word.models

class User (private var id: String, private var pseudo: String, private var nbCoin: Int, private var actualLevel: String) {

    fun getID(): String {
        return id
    }

    fun setID(id: String) {
        this.id = id
    }

    fun getPseudo() : String {
        return pseudo
    }

    fun setPseudo(pseudo: String) {
        this.pseudo = pseudo
    }

    fun getNbCoin(): Int {
        return nbCoin
    }

    fun setNbCoin(number: Int) {
        nbCoin = number
    }

    fun getActualLevel(): String {
        return actualLevel
    }

    fun setActualLevel(level: String) {
        actualLevel = level
    }


}

