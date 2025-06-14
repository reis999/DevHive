package ipvc.tp.devhive.domain.usecase.studygroup

import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository
import javax.inject.Inject


sealed class JoinMethod {
    data class ByGroupId(val groupId: String) : JoinMethod()
    data class ByJoinCode(val joinCode: String) : JoinMethod()
}

class JoinStudyGroupUseCase @Inject constructor(
    private val studyGroupRepository: StudyGroupRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(joinMethod: JoinMethod): Result<Boolean> {
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Utilizador não está logado")
        )

        val groupResult: Result<StudyGroup?> = when (joinMethod) {
            is JoinMethod.ByGroupId -> {
                // Busca grupo por ID
                val g = studyGroupRepository.getStudyGroupById(joinMethod.groupId)
                if (g != null) Result.success(g) else Result.failure(IllegalArgumentException("Grupo não encontrado"))
            }
            is JoinMethod.ByJoinCode -> {
                // Busca grupo pelo código de acesso diretamente
                studyGroupRepository.getStudyGroupByJoinCode(joinMethod.joinCode)
            }
        }

        if (groupResult.isFailure) {
            return Result.failure(groupResult.exceptionOrNull() ?: IllegalStateException("Erro ao buscar grupo"))
        }

        val group = groupResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("Grupo não encontrado ou código de acesso inválido"))

        if (joinMethod is JoinMethod.ByJoinCode && !group.isPrivate) {
            return Result.failure(IllegalArgumentException("Este código de acesso é para um grupo que não é privado ou o código é inválido."))
        }

        if (group.members.contains(currentUser.id)) {
            return Result.success(true) // Já é membro
        }

        if (group.members.size >= group.maxMembers) {
            return Result.failure(IllegalStateException("O grupo já atingiu o número máximo de membros"))
        }

        return studyGroupRepository.joinStudyGroup(group.id, currentUser.id)
    }

    suspend fun joinByGroupId(groupId: String): Result<Boolean> {
        return invoke(JoinMethod.ByGroupId(groupId))
    }

    suspend fun joinByJoinCode(joinCode: String): Result<Boolean> {
        return invoke(JoinMethod.ByJoinCode(joinCode))
    }
}