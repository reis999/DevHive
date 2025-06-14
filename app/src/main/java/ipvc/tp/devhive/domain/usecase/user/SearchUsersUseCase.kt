package ipvc.tp.devhive.domain.usecase.user

import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
)
{
    suspend operator fun invoke(query: String, excludeUserId: String?): Result<List<User>> {
        return userRepository.searchUsers(query, excludeUserId)
    }
}