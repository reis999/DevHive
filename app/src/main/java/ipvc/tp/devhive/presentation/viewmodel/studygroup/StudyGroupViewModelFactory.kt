package ipvc.tp.devhive.presentation.viewmodel.studygroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.domain.usecase.studygroup.CreateStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.JoinStudyGroupUseCase
import ipvc.tp.devhive.domain.usecase.studygroup.SendGroupMessageUseCase

class StudyGroupViewModelFactory(
    private val createStudyGroupUseCase: CreateStudyGroupUseCase,
    private val joinStudyGroupUseCase: JoinStudyGroupUseCase,
    private val sendGroupMessageUseCase: SendGroupMessageUseCase
) : ViewModelProvider.Factory {



    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyGroupViewModel::class.java)) {
            return StudyGroupViewModel(createStudyGroupUseCase, joinStudyGroupUseCase, sendGroupMessageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
