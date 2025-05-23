package ipvc.tp.devhive.domain.usecase.studygroup

import ipvc.tp.devhive.domain.repository.StudyGroupRepository
import ipvc.tp.devhive.domain.repository.UserRepository

class JoinStudyGroupUseCase(
    private val studyGroupRepository: StudyGroupRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(groupId: String): Result<Boolean> {
        // Verifica se o usuário está logado
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Usuário não está logado")
        )

        // Verifica se o grupo existe
        val group = studyGroupRepository.getStudyGroupById(groupId) ?: return Result.failure(
            IllegalArgumentException("Grupo não encontrado")
        )

        // Verifica se o grupo já está cheio
        if (group.members.size >= group.maxMembers) {
            return Result.failure(IllegalStateException("O grupo já atingiu o número máximo de membros"))
        }

        // Verifica se o usuário já é membro
        if (group.members.contains(currentUser.id)) {
            return Result.success(true) // Já é membro, retorna sucesso
        }

        // Adiciona o usuário ao grupo
        return studyGroupRepository.joinStudyGroup(groupId, currentUser.id)
    }

    suspend operator fun invoke(joinCode: String): Result<Boolean> {
        // Verifica se o usuário está logado
        val currentUser = userRepository.getCurrentUser() ?: return Result.failure(
            IllegalStateException("Usuário não está logado")
        )

        // Busca todos os grupos do usuário para encontrar o grupo com o código de acesso
        val groups = studyGroupRepository.getStudyGroupsByUser(currentUser.id).value ?: emptyList()

        // Encontra o grupo com o código de acesso
        val group = groups.find { it.joinCode == joinCode } ?: return Result.failure(
            IllegalArgumentException("Código de acesso inválido")
        )

        // Verifica se o grupo já está cheio
        if (group.members.size >= group.maxMembers) {
            return Result.failure(IllegalStateException("O grupo já atingiu o número máximo de membros"))
        }

        // Verifica se o usuário já é membro
        if (group.members.contains(currentUser.id)) {
            return Result.success(true) // Já é membro, retorna sucesso
        }

        // Adiciona o usuário ao grupo
        return studyGroupRepository.joinStudyGroup(group.id, currentUser.id)
    }
}
