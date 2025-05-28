package ipvc.tp.devhive.domain.usecase.studygroup

import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository

sealed class JoinMethod {
    data class ByGroupId(val groupId: String) : JoinMethod()
    data class ByJoinCode(val joinCode: String) : JoinMethod()
}

class JoinStudyGroupUseCase(
    private val studyGroupRepository: StudyGroupRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(joinMethod: JoinMethod): Result<Boolean> {
        // Verifica se o utilizador está logado
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Utilizador não está logado")
        )

        val group = when (joinMethod) {
            is JoinMethod.ByGroupId -> {
                // Busca grupo por ID
                studyGroupRepository.getStudyGroupById(joinMethod.groupId) ?: return Result.failure(
                    IllegalArgumentException("Grupo não encontrado")
                )
            }
            is JoinMethod.ByJoinCode -> {
                // Busca grupo pelo código de acesso
                val groups = studyGroupRepository.getStudyGroupsByUser(currentUser.id).value ?: emptyList()
                groups.find { it.joinCode == joinMethod.joinCode } ?: return Result.failure(
                    IllegalArgumentException("Código de acesso inválido")
                )
            }
        }

        // Verifica se o grupo já está cheio
        if (group.members.size >= group.maxMembers) {
            return Result.failure(IllegalStateException("O grupo já atingiu o número máximo de membros"))
        }

        // Verifica se o utilizador já é membro
        if (group.members.contains(currentUser.id)) {
            return Result.success(true) // Já é membro, retorna sucesso
        }

        // Adiciona o utilizador ao grupo
        return studyGroupRepository.joinStudyGroup(group.id, currentUser.id)
    }

    // Métodos de conveniência para facilitar o uso
    suspend fun joinByGroupId(groupId: String): Result<Boolean> {
        return invoke(JoinMethod.ByGroupId(groupId))
    }

    suspend fun joinByJoinCode(joinCode: String): Result<Boolean> {
        return invoke(JoinMethod.ByJoinCode(joinCode))
    }
}