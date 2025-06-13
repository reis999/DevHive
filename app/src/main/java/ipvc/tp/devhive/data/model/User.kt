package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val institution: String = "",
    val course: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp = Timestamp.now(),
    val online: Boolean = false,
    val contributionStats: ContributionStats = ContributionStats()
)

data class ContributionStats(
    val materials: Int = 0,
    val comments: Int = 0,
    val likes: Int = 0,
    val sessions: Int = 0
)