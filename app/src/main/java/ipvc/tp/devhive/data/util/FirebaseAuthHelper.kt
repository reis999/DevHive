package ipvc.tp.devhive.data.util

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthHelper {
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}