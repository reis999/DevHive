package ipvc.tp.devhive.data.repository

import com.google.firebase.auth.FirebaseAuth
import ipvc.tp.devhive.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Erro ao obter ID"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Erro ao obter ID"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Boolean> {
        return try {
            FirebaseAuth.getInstance().signOut()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
