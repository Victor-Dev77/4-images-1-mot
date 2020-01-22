package fr.esgi.app_4_images_1_word.controllers

import fr.esgi.app_4_images_1_word.models.User

class UserController {

    private val user = User("id", "pseudo", 0, 1)

    fun setUser(user: User) {
        this.user.id = user.id
        this.user.pseudo = user.pseudo
        this.user.nbCoin = user.nbCoin
        this.user.actualLevel = user.actualLevel
    }

    fun getCoin() : Int {
        return user.nbCoin
    }

    fun increaseCoin(coins: Int) {
        user.nbCoin = (user.nbCoin + coins)
    }

    fun decreaseCoin(coins: Int) {
        user.nbCoin = (user.nbCoin - coins)
    }

    fun setActualLevel(level: Int) {
        user.actualLevel = level
    }

    fun getActualLevel() : Int {
        return user.actualLevel
    }

    fun getUser() : User {
        return user.getUser()
    }
}