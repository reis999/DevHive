package ipvc.tp.devhive.domain.usecase.user

import android.net.Uri
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(name: String, bio: String, institution: String, course: String, imageUri: Uri? = null): Result<User> {
        return repository.updateUser(name, bio, institution, course, imageUri)
    }
}