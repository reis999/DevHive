package ipvc.tp.devhive.domain.usecase.studygroup

import androidx.lifecycle.LiveData
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import javax.inject.Inject

class GetStudyGroupMessagesUseCase @Inject constructor(
    private val studyGroupRepository: StudyGroupRepository
)
{
    operator fun invoke(groupId: String): LiveData<List<GroupMessage>> {
        return studyGroupRepository.getGroupMessagesByStudyGroupId(groupId)
    }
}