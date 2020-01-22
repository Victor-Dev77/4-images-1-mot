package fr.esgi.app_4_images_1_word.controllers

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.models.User
import fr.esgi.app_4_images_1_word.views.MainActivity

class FirebaseFirestoreHelper(private val view: MainActivity,
                              private val levelController: LevelController,
                              private var user: UserController) {

    private val db = FirebaseFirestore.getInstance()

    fun initFirestore() {
        setup()
        setupCacheSize()
    }

    private fun setup() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    private fun setupCacheSize() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
    }

    fun getAllLevels() {
        Log.d("toto", "getactuallevel: ${user.getActualLevel()}")
        db.collection("levels")
            .orderBy("levelNumber")
            .whereGreaterThanOrEqualTo("levelNumber", user.getActualLevel())
            .get()
            .addOnCanceledListener { view.alert("erreur loading data") }
            .addOnSuccessListener { result ->
                result.forEach {document ->
                    val map = document.data
                    val level = Level(
                                    document.id,
                                    (map["levelNumber"] as Long).toInt(),
                                    map["image"] as String,
                                    map["word"] as String,
                                    map["difficulty"] as String)

                    levelController.addLevel(level)
                    Log.d("toto", "${document.id} => ${document.data}")
                }
                if (levelController.getNBLevel() > 0) {
                    val level = levelController.getLevel(0)!!
                    view.loadImage(level.image)
                    user.setActualLevel(levelController.getLevel(0)!!.levelNumber)
                    levelController.setActualLevel(level)
                    levelController.setWordTemp(EMPTY_STRING.repeat(level.word.length))
                    view.initUI()
                }

            }
            .addOnFailureListener { exception ->
                Log.d("toto", "Error getting documents: ", exception)
            }
    }

    fun saveLevel(user: User) {
        db.collection("users")
            .document(user.id)
            .update(mapOf(
                "nbCoin" to user.nbCoin,
                "actualLevel" to user.actualLevel
            ))
            .addOnSuccessListener {
                Log.d("toto", "UPDATE ${user.nbCoin}")
            }
    }

    fun getUserInfo(currentUser: User) {
        Log.d("toto", "current user ${currentUser.id}")
        db.collection("users")
            .document(currentUser.id)
            .get()
            .addOnSuccessListener {result ->

                val map = (result.data)?: emptyMap()
                if (map.isEmpty()) {
                    this.user.setUser(currentUser)
                    setUser(this.user.getUser())
                } else {
                    val userBDD =
                        User(
                            map["id"] as String,
                            map["pseudo"] as String,
                            map["nbCoin"].toString().toInt(),
                            map["actualLevel"].toString().toInt()
                        )
                    this.user.setUser(userBDD)
                    getAllLevels()
                    Log.d("toto", "LOGGING $user")
                }

            }
    }

    fun setUser(currentUser: User) {
        Log.d("toto", "user setuser: ${currentUser.id}")
        val userBDD = User(currentUser.id, currentUser.pseudo, 400, 1)
        db.collection("users")
            .document(userBDD.id)
            .set(mapOf(
                "id" to userBDD.id,
                "pseudo" to userBDD.pseudo,
                "nbCoin" to userBDD.nbCoin,
                "actualLevel" to userBDD.actualLevel
            ))
            .addOnSuccessListener {result ->
                Log.d("toto", "USER INSCRIT DANS BDD")
            }
    }
}