package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val recipientUid: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val read: Boolean = false,
    val actionType: String = "",
    val actionData: String = "",
    val senderUid: String = "",
    val lastSync: Timestamp = Timestamp.now()
)