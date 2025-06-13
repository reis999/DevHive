package ipvc.tp.devhive.domain.usecase.studygroup

import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject

class RemoveMemberUseCase @Inject constructor(
    private val repository: StudyGroupRepository
) {
    suspend operator fun invoke(groupId: String, memberId: String): Result<Unit> {
        return repository.removeMember(groupId, memberId)
    }
}