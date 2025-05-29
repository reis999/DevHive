package ipvc.tp.devhive.domain.repository

/**
 * Interface de repositório para operações relacionadas a autenticação
 */

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun register(email: String, password: String): Result<String>
    suspend fun logout(): Result<Boolean>
    fun getCurrentUserId(): String?
}
