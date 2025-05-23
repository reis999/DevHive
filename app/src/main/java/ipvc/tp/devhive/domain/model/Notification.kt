package ipvc.tp.devhive.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de domínio para representar uma notificação
 */
data class Notification(
    val id: String,
    val recipientUid: String,
    val type: String,
    val title: String,
    val message: String,
    val createdAt: Timestamp,
    val read: Boolean,
    val actionType: String,
    val actionData: String,
    val senderUid: String
)
