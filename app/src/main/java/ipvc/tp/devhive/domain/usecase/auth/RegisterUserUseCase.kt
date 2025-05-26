package ipvc.tp.devhive.domain.usecase.auth

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import java.util.UUID

/**
 * Caso de uso para registrar um novo usuário
 */
class RegisterUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(
        name: String,
        username: String,
        email: String,
        password: String,
        institution: String,
        course: String
    ): Result<User> {
        // Validação de dados
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos os campos são obrigatórios"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("A senha deve ter pelo menos 6 caracteres"))
        }

        if (!email.contains("@")) {
            return Result.failure(IllegalArgumentException("Email inválido"))
        }

        // Criação do utilizador
        val userId = UUID.randomUUID().toString()
        val now = Timestamp(Date())

        val newUser = User(
            id = userId,
            name = name,
            username = username,
            email = email,
            profileImageUrl = "",
            bio = "",
            institution = institution,
            course = course,
            createdAt = now,
            lastLogin = now,
            isOnline = true,
            contributionStats = ipvc.tp.devhive.domain.model.ContributionStats(
                materials = 0,
                comments = 0,
                likes = 0,
                sessions = 0
            )
        )

        return userRepository.createUser(newUser)
    }
}
