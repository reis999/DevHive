package ipvc.tp.devhive.domain.usecase.auth

import com.google.firebase.Timestamp
import ipvc.tp.devhive.domain.model.ContributionStats
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.repository.AuthRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import java.util.Date
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        username: String,
        email: String,
        password: String,
        institution: String,
        course: String
    ): Result<User> {
        // Validação
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos os campos são obrigatórios"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("A senha deve ter pelo menos 6 caracteres"))
        }

        if (!email.contains("@")) {
            return Result.failure(IllegalArgumentException("Email inválido"))
        }

        // Criar conta no Firebase Auth
        val authResult = authRepository.register(email, password)
        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull() ?: Exception("Erro ao criar utilizador"))
        }

        val userId = authResult.getOrNull()!!
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
            contributionStats = ContributionStats(0, 0, 0, 0)
        )

        // Guardar no Firestore
        return userRepository.createUser(newUser)
    }
}
