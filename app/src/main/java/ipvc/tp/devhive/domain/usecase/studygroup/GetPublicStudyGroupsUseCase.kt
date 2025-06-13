package ipvc.tp.devhive.domain.usecase.studygroup

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject

class GetPublicStudyGroupsUseCase @Inject constructor(
    private val studyGroupRepository: StudyGroupRepository
) {
    operator fun invoke(): LiveData<List<StudyGroup>> {
        return studyGroupRepository.getPublicStudyGroups()
    }
}