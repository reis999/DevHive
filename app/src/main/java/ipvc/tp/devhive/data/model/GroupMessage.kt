package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class GroupMessage(
    val id: String = "",
    val studyGroupId: String = "",
    val content: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderImageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val attachments: List<MessageAttachment> = emptyList(),
    val replyToMessageId: String? = null,
    val isEdited: Boolean = false,
    val editedAt: Timestamp? = null
)
