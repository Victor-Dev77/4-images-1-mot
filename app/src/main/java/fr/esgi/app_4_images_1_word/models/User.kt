package fr.esgi.app_4_images_1_word.models

data class User (var id: String, var pseudo: String, var nbCoin: Int, var actualLevel: Int) {

   fun getUser() : User {
        return User(id, pseudo, nbCoin, actualLevel)
    }

}

