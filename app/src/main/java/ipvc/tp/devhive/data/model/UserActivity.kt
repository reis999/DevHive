package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class UserActivity(
    val id: String = "",
    val userUid: String = "",
    val type: String = "",
    val targetId: String = "",
    val targetType: String = "",
    val action: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val metadata: Map<String, Any> = emptyMap(),
    val lastSync: Timestamp = Timestamp.now()
)
