package ipvc.tp.devhive.domain.usecase.studygroup

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject

class GetStudyGroupsByUserUseCase @Inject constructor(
    private val studyGroupRepository: StudyGroupRepository,
) {
    operator fun invoke(userId: String): LiveData<List<StudyGroup>> {
        return studyGroupRepository.getStudyGroupsByUser(userId)
    }
}