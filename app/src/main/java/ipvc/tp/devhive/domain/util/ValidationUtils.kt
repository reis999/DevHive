package ipvc.tp.devhive.domain.util

/**
 * Utilitários para validação de dados
 */
object ValidationUtils {

    /**
     * Valida um endereço de email
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    /**
     * Valida uma senha
     * Deve ter pelo menos 6 caracteres, incluindo pelo menos uma letra e um número
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }

        return hasLetter && hasDigit
    }

    /**
     * Valida um nome de usuário
     * Deve ter entre 3 e 20 caracteres, apenas letras, números e underscores
     */
    fun isValidUsername(username: String): Boolean {
        val usernameRegex = "^[A-Za-z0-9_]{3,20}$"
        return username.matches(usernameRegex.toRegex())
    }
}
