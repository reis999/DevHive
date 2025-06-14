package ipvc.tp.devhive.domain.usecase.user

import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserStatsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, action: StatsAction): Result<Boolean> {
        return userRepository.updateUserStats(userId, action)
    }
}

enum class StatsAction {
    INCREMENT_COMMENTS,
    INCREMENT_MATERIALS,
    INCREMENT_LIKES,
    INCREMENT_SESSIONS,

    DECREMENT_MATERIALS,
    DECREMENT_LIKES,
    DECREMENT_COMMENTS
}
