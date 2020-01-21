package fr.esgi.app_4_images_1_word.controllers

import fr.esgi.app_4_images_1_word.models.User

class UserController {

    private val user = User("id", "pseudo", 0, 1)

    fun setUser(user: User) {
        this.user.setID(user.getID())
        this.user.setPseudo(user.getPseudo())
        this.user.setNbCoin(user.getNbCoin())
        this.user.setActualLevel(user.getActualLevel())
    }

    fun getCoin() : Int {
        return user.getNbCoin()
    }

    fun increaseCoin(coins: Int) {
        user.setNbCoin(user.getNbCoin() + coins)
    }

    fun decreaseCoin(coins: Int) {
        user.setNbCoin(user.getNbCoin() - coins)
    }

    fun setActualLevel(level: Int) {
        user.setActualLevel(level)
    }

    fun getActualLevel() : Int {
        return user.getActualLevel()
    }

    fun getUser() : User {
        return user.getUser()
    }
}