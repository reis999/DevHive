package ipvc.tp.devhive.domain.usecase.studygroup

import android.net.Uri
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject

class UpdateStudyGroupUseCase @Inject constructor(
    private val repository: StudyGroupRepository
) {
    suspend operator fun invoke(groupId: String, name: String, description: String, categories: List<String>, imageUri: Uri? = null): Result<StudyGroup> {
        return repository.updateStudyGroup(groupId, name, description, categories, imageUri)
    }
}