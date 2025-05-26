package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de domínio para representar um chat direto entre dois usuários
 */
data class Chat(
    val id: String,
    val participant1Id: String,
    val participant2Id: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val lastMessageAt: Timestamp,
    val lastMessagePreview: String,
    val messageCount: Int,
    // Campos derivados para facilitar o uso na UI
    val otherParticipantId: String = "", // ID do outro participante (calculado baseado no utilizador atual)
    val otherParticipantName: String = "",
    val otherParticipantImageUrl: String = "",
    val otherParticipantOnline: Boolean = false,
    val unreadCount: Int = 0
)

