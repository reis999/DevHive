package ipvc.tp.devhive.domain.usecase.studygroup

import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject


class GetStudyGroupByIdUseCase @Inject constructor(
    private val studyGroupRepository: StudyGroupRepository
){
    suspend operator fun invoke(userId: String): StudyGroup? {
        return studyGroupRepository.getStudyGroupById(userId)
    }
}