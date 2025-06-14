package ipvc.tp.devhive.domain.usecase.studygroup

import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject

class LeaveStudyGroupUseCase @Inject constructor(
    private val repository: StudyGroupRepository
){
    suspend operator fun invoke(groupId: String, userId: String): Result<Boolean> {
        return repository.leaveStudyGroup(groupId, userId)
    }
}
