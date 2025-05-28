package ipvc.tp.devhive.presentation.viewmodel.material

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.usecase.material.CreateMaterialUseCase
import ipvc.tp.devhive.domain.usecase.material.GetMaterialsUseCase
import ipvc.tp.devhive.domain.usecase.material.ToggleBookmarkUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch

class MaterialViewModel(
    private val getMaterialsUseCase: GetMaterialsUseCase,
    private val createMaterialUseCase: CreateMaterialUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    // Lista de todos os materiais
    val materials: LiveData<List<Material>> = getMaterialsUseCase()

    // Lista de materiais favoritos
    val bookmarkedMaterials: LiveData<List<Material>> = getMaterialsUseCase.bookmarked()

    private val _materialEvent = MutableLiveData<Event<MaterialEvent>>()
    val materialEvent: LiveData<Event<MaterialEvent>> = _materialEvent

    fun getMaterialsByUser(userId: String): LiveData<List<Material>> {
        return getMaterialsUseCase.byUser(userId)
    }

    fun getMaterialsBySubject(subject: String): LiveData<List<Material>> {
        return getMaterialsUseCase.bySubject(subject)
    }

    fun createMaterial(
        title: String,
        description: String,
        type: String,
        contentUrl: String,
        thumbnailUrl: String,
        fileSize: Long,
        ownerUid: String,
        categories: List<String>,
        isPublic: Boolean,
        subject: String
    ) {
        viewModelScope.launch {
            val result = createMaterialUseCase(
                title = title,
                description = description,
                type = type,
                contentUrl = contentUrl,
                thumbnailUrl = thumbnailUrl,
                fileSize = fileSize,
                ownerUid = ownerUid,
                categories = categories,
                isPublic = isPublic,
                subject = subject
            )

            result.fold(
                onSuccess = {
                    _materialEvent.value = Event(MaterialEvent.CreateSuccess(it))
                },
                onFailure = {
                    _materialEvent.value = Event(MaterialEvent.CreateFailure(it.message ?: "Erro ao criar material"))
                }
            )
        }
    }

    fun toggleBookmark(materialId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            val result = toggleBookmarkUseCase(materialId, bookmarked)

            result.fold(
                onSuccess = {
                    _materialEvent.value = Event(MaterialEvent.BookmarkToggled(materialId, bookmarked))
                },
                onFailure = {
                    _materialEvent.value = Event(MaterialEvent.BookmarkFailure(it.message ?: "Erro ao marcar/desmarcar favorito"))
                }
            )
        }
    }
}

sealed class MaterialEvent {
    data class CreateSuccess(val material: Material) : MaterialEvent()
    data class CreateFailure(val message: String) : MaterialEvent()
    data class BookmarkToggled(val materialId: String, val bookmarked: Boolean) : MaterialEvent()
    data class BookmarkFailure(val message: String) : MaterialEvent()
}
