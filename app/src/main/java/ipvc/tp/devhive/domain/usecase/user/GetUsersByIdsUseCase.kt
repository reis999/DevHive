package ipvc.tp.devhive.domain.usecase.user

import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersByIdsUseCase @Inject constructor(
    private val repository: UserRepository
)
{
    suspend operator fun invoke(userIds: List<String>): Result<List<User>> {
        return repository.getUsersByIds(userIds)
    }
}