package ipvc.tp.devhive.domain.usecase.material

import ipvc.tp.devhive.domain.repository.MaterialRepository
import javax.inject.Inject

/**
 * Caso de uso para fazer download de um material
 */
class DownloadMaterialUseCase @Inject constructor(
    private val materialRepository: MaterialRepository
) {

    suspend operator fun invoke(materialId: String): Result<String> {
        return try {
            // Primeiro obtém o material para verificar se existe
            val material = materialRepository.getMaterialById(materialId)

            if (material != null) {
                // Incrementa o contador de downloads
                materialRepository.incrementDownloads(materialId)

                // Retorna a URL do conteúdo para download
                Result.success(material.contentUrl)
            } else {
                Result.failure(Exception("Material não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}