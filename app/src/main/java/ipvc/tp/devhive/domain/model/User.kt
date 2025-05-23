package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de domínio para representar um usuário na aplicação
 */
data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val profileImageUrl: String,
    val bio: String,
    val institution: String,
    val course: String,
    val createdAt: Timestamp,
    val lastLogin: Timestamp,
    val isOnline: Boolean,
    val contributionStats: ContributionStats
)

data class ContributionStats(
    val materials: Int,
    val comments: Int,
    val likes: Int,
    val sessions: Int
)
