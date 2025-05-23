package ipvc.tp.devhive.presentation.viewmodel.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ipvc.tp.devhive.domain.usecase.material.CreateMaterialUseCase
import ipvc.tp.devhive.domain.usecase.material.GetMaterialsUseCase
import ipvc.tp.devhive.domain.usecase.material.ToggleBookmarkUseCase

class MaterialViewModelFactory(
    private val getMaterialsUseCase: GetMaterialsUseCase,
    private val createMaterialUseCase: CreateMaterialUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialViewModel::class.java)) {
            return MaterialViewModel(
                getMaterialsUseCase,
                createMaterialUseCase,
                toggleBookmarkUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
