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
        db.collection(COLLECTION_LEVEL)
            .orderBy(LEVEL_COL_LEVELNUMBER)
            .whereGreaterThanOrEqualTo(LEVEL_COL_LEVELNUMBER, user.getActualLevel())
            .get()
            .addOnCanceledListener { view.alert(ALERT_DATA_ERROR) }
            .addOnSuccessListener { result ->
                result.forEach {document ->
                    val map = document.data
                    val level = Level(
                                    document.id,
                                    (map[LEVEL_COL_LEVELNUMBER] as Long).toInt(),
                                    map[LEVEL_COL_IMAGE] as String,
                                    map[LEVEL_COL_WORD] as String,
                                    map[LEVEL_COL_DIFFICULTY] as String)

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
        db.collection(COLLECTION_USER)
            .document(user.id)
            .update(mapOf(
                USER_COL_NBCOIN to user.nbCoin,
                USER_COL_ACTUALLEVEL to user.actualLevel
            ))
            .addOnSuccessListener {
                Log.d("toto", "UPDATE ${user.nbCoin}")
            }
    }

    fun getUserInfo(currentUser: User) {
        Log.d("toto", "current user ${currentUser.id}")
        db.collection(COLLECTION_USER)
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
                            map[USER_COL_ID] as String,
                            map[USER_COL_PSEUDO] as String,
                            map[USER_COL_NBCOIN].toString().toInt(),
                            map[USER_COL_ACTUALLEVEL].toString().toInt()
                        )
                    this.user.setUser(userBDD)
                    getAllLevels()
                    Log.d("toto", "LOGGING $user")
                }

            }
    }

    fun setUser(currentUser: User) {
        Log.d("toto", "user setuser: ${currentUser.id}")
        val userBDD = User(currentUser.id, currentUser.pseudo, START_NB_COIN, START_LEVEL)
        db.collection(COLLECTION_USER)
            .document(userBDD.id)
            .set(mapOf(
                USER_COL_ID to userBDD.id,
                USER_COL_PSEUDO to userBDD.pseudo,
                USER_COL_NBCOIN to userBDD.nbCoin,
                USER_COL_ACTUALLEVEL to userBDD.actualLevel
            ))
            .addOnSuccessListener {
                Log.d("toto", "USER INSCRIT DANS BDD")
            }
    }
}