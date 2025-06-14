package ipvc.tp.devhive.presentation.viewmodel.material

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.domain.usecase.material.CreateMaterialUseCase
import ipvc.tp.devhive.domain.usecase.material.DeleteMaterialUseCase
import ipvc.tp.devhive.domain.usecase.material.DownloadMaterialUseCase
import ipvc.tp.devhive.domain.usecase.material.GetMaterialsUseCase
import ipvc.tp.devhive.domain.usecase.material.ToggleBookmarkUseCase
import ipvc.tp.devhive.domain.usecase.material.ToggleMaterialLikeUseCase
import ipvc.tp.devhive.domain.usecase.user.GetCurrentUserUseCase
import ipvc.tp.devhive.presentation.util.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialViewModel @Inject constructor(
    private val getMaterialsUseCase: GetMaterialsUseCase,
    private val createMaterialUseCase: CreateMaterialUseCase,
    private val deleteMaterialUseCase: DeleteMaterialUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val downloadMaterialUseCase: DownloadMaterialUseCase,
    private val toggleMaterialLikeUseCase: ToggleMaterialLikeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _materials = MediatorLiveData<List<Material>>()
    val materials: LiveData<List<Material>> = _materials

    private val _material = MutableLiveData<Material?>()
    val material: LiveData<Material?> = _material

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _createMaterialResultEvent = MutableLiveData<Event<MaterialGeneralResult>>()
    val createMaterialResultEvent: LiveData<Event<MaterialGeneralResult>> = _createMaterialResultEvent

    private val _isLoadingMaterial = MutableLiveData<Boolean>()
    val isLoadingMaterial: LiveData<Boolean> = _isLoadingMaterial

    private val _isDeletingMaterial = MutableLiveData<Boolean>()
    val isDeletingMaterial: LiveData<Boolean> = _isDeletingMaterial

    private val _materialEvent = MutableLiveData<Event<MaterialEvent>>()
    val materialEvent: LiveData<Event<MaterialEvent>> = _materialEvent

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects

    private val _currentSearchQuery = MutableLiveData<String>("")
    private val _currentSubjectFilter = MutableLiveData<String?>(null)

    private var currentMaterialSource: LiveData<List<Material>>? = null

    init {
        loadCurrentUser()
        loadAllMaterials()
        loadSubjects()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase()
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
            }
        }
    }

    private fun loadAllMaterials() {
        switchMaterialSource(getMaterialsUseCase())
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                val subjects = getMaterialsUseCase.getDistinctSubjects()
                _subjects.value = subjects
            } catch (e: Exception) {
                _subjects.value = emptyList()
            }
        }
    }

    private fun switchMaterialSource(newSource: LiveData<List<Material>>) {
        currentMaterialSource?.let { _materials.removeSource(it) }

        currentMaterialSource = newSource
        _materials.addSource(newSource) { materials ->
            _materials.value = materials
        }
    }

    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }

    fun refreshCurrentUser() {
        loadCurrentUser()
    }

    fun getMaterialsByUser(userId: String): LiveData<List<Material>> {
        return getMaterialsUseCase.byUser(userId)
    }

    fun getMaterialsBySubject(subject: String): LiveData<List<Material>> {
        return getMaterialsUseCase.bySubject(subject)
    }

    fun getMaterialById(materialId: String) {
        viewModelScope.launch {
            _isLoadingMaterial.value = true
            try {
                val material = getMaterialsUseCase.byId(materialId)
                _material.value = material
            } catch (e: Exception) {
                _materialEvent.value = Event(MaterialEvent.ShowMessage("Erro ao carregar material: ${e.message}"))
            } finally {
                _isLoadingMaterial.value = false
            }
        }
    }

    fun searchMaterials(query: String) {
        _currentSearchQuery.value = query
        applyFilters()
    }

    fun filterBySubject(subject: String?) {
        _currentSubjectFilter.value = subject
        applyFilters()
    }

    fun clearFilters() {
        _currentSearchQuery.value = ""
        _currentSubjectFilter.value = null
        loadAllMaterials()
    }

    private fun applyFilters() {
        val query = _currentSearchQuery.value ?: ""
        val subject = _currentSubjectFilter.value

        val newSource = when {
            query.isNotBlank() && subject != null -> {
                getMaterialsUseCase.searchWithSubject(query, subject)
            }
            query.isNotBlank() -> {
                getMaterialsUseCase.search(query)
            }
            subject != null -> {
                getMaterialsUseCase.searchBySubject(subject)
            }
            else -> {
                getMaterialsUseCase()
            }
        }

        switchMaterialSource(newSource)
    }

    fun getCurrentSearchQuery(): String? = _currentSearchQuery.value
    fun getCurrentSubjectFilter(): String? = _currentSubjectFilter.value

    fun createMaterial(
        title: String,
        description: String,
        type: String,
        subject: String,
        categories: List<String>,
        isPublic: Boolean,
        fileUri: Uri,
        thumbnailUri: Uri? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = createMaterialUseCase(
                    title = title,
                    description = description,
                    type = type,
                    subject = subject,
                    categories = categories,
                    isPublic = isPublic,
                    fileUri = fileUri,
                    thumbnailUri = thumbnailUri
                )

                if (result.isSuccess) {
                    val newMaterial = result.getOrNull()
                    if (newMaterial != null) {
                        _createMaterialResultEvent.value = Event(MaterialGeneralResult.Success(newMaterial.id))
                        loadSubjects()
                    } else {
                        _createMaterialResultEvent.value = Event(MaterialGeneralResult.Failure("Material criado, mas dados não retornados."))
                    }
                } else {
                    _createMaterialResultEvent.value = Event(MaterialGeneralResult.Failure(
                        result.exceptionOrNull()?.message ?: "Falha ao criar material."
                    ))
                }
            } catch (e: Exception) {
                _createMaterialResultEvent.value = Event(MaterialGeneralResult.Failure(
                    e.message ?: "Erro inesperado ao criar material."
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMaterial(materialId: String) {
        _isDeletingMaterial.value = true
        viewModelScope.launch {
            try {
                val result = deleteMaterialUseCase(materialId)

                result.fold(
                    onSuccess = {
                        _materialEvent.value = Event(MaterialEvent.DeleteSuccess(materialId))
                        loadSubjects()
                    },
                    onFailure = { exception ->
                        _materialEvent.value = Event(MaterialEvent.DeleteFailure(
                            exception.message ?: "Erro ao eliminar material"
                        ))
                    }
                )
            } catch (e: Exception) {
                _materialEvent.value = Event(MaterialEvent.DeleteFailure(
                    e.message ?: "Erro inesperado ao eliminar material"
                ))
            } finally {
                _isDeletingMaterial.value = false
            }
        }
    }

    fun downloadMaterial(materialId: String) {
        viewModelScope.launch {
            val result = downloadMaterialUseCase(materialId)

            result.fold(
                onSuccess = { contentUrl ->
                    _materialEvent.value = Event(MaterialEvent.DownloadSuccess(contentUrl))
                },
                onFailure = { exception ->
                    _materialEvent.value = Event(MaterialEvent.DownloadFailure(exception.message ?: "Erro ao fazer download"))
                }
            )
        }
    }

    fun getBookmarkedMaterials(userId: String): LiveData<List<Material>> {
        return getMaterialsUseCase.bookmarked(userId)
    }

    fun toggleBookmark(materialId: String, userId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            val result = toggleBookmarkUseCase(materialId, userId, isBookmarked)

            result.fold(
                onSuccess = {
                    _materialEvent.value = Event(MaterialEvent.BookmarkToggled(materialId, isBookmarked))
                },
                onFailure = {
                    _materialEvent.value = Event(MaterialEvent.BookmarkFailure(it.message ?: "Erro ao marcar/desmarcar favorito"))
                }
            )
        }
    }

    fun toggleMaterialLike(materialId: String, userId: String, isLiked: Boolean) {
        viewModelScope.launch {
            val result = toggleMaterialLikeUseCase(materialId, userId, isLiked)

            result.fold(
                onSuccess = {
                    _materialEvent.value = Event(MaterialEvent.LikeToggled(materialId, isLiked))
                },
                onFailure = {
                    _materialEvent.value = Event(MaterialEvent.LikeFailure(it.message ?: "Erro ao curtir material"))
                }
            )
        }
    }

    fun clearMaterial() {
        _material.value = null
    }
}

sealed class MaterialGeneralResult {
    data class Success(val materialId: String) : MaterialGeneralResult()
    data class Failure(val message: String) : MaterialGeneralResult()
}

sealed class MaterialEvent {
    data class CreateSuccess(val material: Material) : MaterialEvent()
    data class CreateFailure(val message: String) : MaterialEvent()
    data class DeleteSuccess(val materialId: String) : MaterialEvent()
    data class DeleteFailure(val message: String) : MaterialEvent()
    data class BookmarkToggled(val materialId: String, val bookmarked: Boolean) : MaterialEvent()
    data class BookmarkFailure(val message: String) : MaterialEvent()
    data class DownloadSuccess(val contentUrl: String) : MaterialEvent()
    data class DownloadFailure(val message: String) : MaterialEvent()
    data class LikeToggled(val materialId: String, val isLiked: Boolean) : MaterialEvent()
    data class LikeFailure(val message: String) : MaterialEvent()
    data class ShowMessage(val message: String) : MaterialEvent()
}