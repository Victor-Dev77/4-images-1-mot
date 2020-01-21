package fr.esgi.app_4_images_1_word.controllers

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import fr.esgi.app_4_images_1_word.models.User
import fr.esgi.app_4_images_1_word.views.MainActivity


class FirebaseAuthHelper(private val view: MainActivity, private val firestore: FirebaseFirestoreHelper, private val userController: UserController) {

    private var auth = FirebaseAuth.getInstance()


    fun signIn() {
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Get USER INFO FIRESTORE
            val user = User(currentUser.uid, currentUser.displayName as String, 400, 1)
            firestore.getUserInfo(user)
            //userController.setUser(User(currentUser.uid, currentUser.displayName as String, 400, ""))
            return
        }
        //updateUI(currentUser)
        Log.d("toto", "user: ${currentUser}")
        view.updateCoin()
        auth.signInAnonymously()
            .addOnFailureListener { res -> Log.d("toto", "NOP NOP")}
            .addOnCompleteListener(view) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("toto", "signInAnonymously:success")
                    val user = User(auth.currentUser?.uid as String, auth.currentUser?.displayName as String, 400, 1)
                    firestore.setUser(user)
                    firestore.getAllLevels()
                    // userController.setUser()
                    Log.d("toto", "user: ${auth.currentUser!!.isAnonymous}")
                    view.updateCoin()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("toto", "signInAnonymously:failure", task.exception)
                    Toast.makeText(view, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                // ...
            }
    }



}