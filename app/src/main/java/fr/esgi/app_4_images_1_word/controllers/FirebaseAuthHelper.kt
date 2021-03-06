package fr.esgi.app_4_images_1_word.controllers

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import fr.esgi.app_4_images_1_word.models.User
import fr.esgi.app_4_images_1_word.views.MainActivity


class FirebaseAuthHelper(private val view: MainActivity,
                         private val firestore: FirebaseFirestoreHelper,
                         private val userController: UserController) {

    private var auth = FirebaseAuth.getInstance()


    fun signIn() {
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        currentUser?.let { thisCurrentUser ->
            val user = User(thisCurrentUser.uid, thisCurrentUser.displayName as String, START_NB_COIN, START_LEVEL)
            firestore.getUserInfo(user)
            return
        }

        Log.d("toto", "user debut : ${currentUser?.uid}")
        view.updateCoin()

        auth.signInAnonymously()
            .addOnCompleteListener(view) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("toto", "signInAnonymously:success")
                    val user = User(auth.currentUser?.uid ?: USER_COL_ID,
                                auth.currentUser?.displayName ?: USER_COL_PSEUDO,
                                        START_NB_COIN,
                                        START_LEVEL)
                    userController.setUser(user)
                    firestore.setUser(user)
                    firestore.getAllLevels()
                    view.updateCoin()

                } else
                    view.alert(ALERT_AUTH_FAILED)

            }
    }



}