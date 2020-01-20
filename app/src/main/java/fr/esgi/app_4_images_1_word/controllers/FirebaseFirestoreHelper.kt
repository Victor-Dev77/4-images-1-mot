package fr.esgi.app_4_images_1_word.controllers

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import fr.esgi.app_4_images_1_word.models.Level
import fr.esgi.app_4_images_1_word.views.MainActivity

class FirebaseFirestoreHelper(private val view: MainActivity, private val levelController: LevelController, private val user: UserController) {

    private val db = FirebaseFirestore.getInstance()

    init {
        setup()
        setupCacheSize()
        getAllLevels()
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

    private fun getAllLevels() {
        db.collection("levels")
            .orderBy("levelNumber")
            .get()
            .addOnCanceledListener { Log.d("toto", "errerur loading data")}
            .addOnSuccessListener { result ->
                for (document in result) {
                    val map = document.data
                    val level = Level(document.id, (map["levelNumber"] as Long).toInt(), map["image"] as String, map["word"] as String, map["difficulty"] as String)
                    levelController.addLevel(level)
                    Log.d("toto", "${document.id} => ${document.data}")
                }
                if (levelController.getNBLevel() > 0) {
                    val level = levelController.getLevel(0)!!
                    view.loadImage(level.getImage())

                    //DownloadImageTask(imageLevel)
                    //    .execute(listLevels.first().image)
                    user.setActualLevel(levelController.getLevel(0)!!.getID())
                    levelController.setActualLevel(level)
                    levelController.setWordTemp(" ".repeat(level.getWord().length))
                    view.initUI()
                }

            }
            .addOnFailureListener { exception ->
                Log.d("toto", "Error getting documents: ", exception)
            }
    }
}