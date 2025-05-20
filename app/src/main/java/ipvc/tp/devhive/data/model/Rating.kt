package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class Rating(
    val id: String = "",
    val materialId: String = "",
    val userUid: String = "",
    val rating: Float = 0f,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastSync: Timestamp = Timestamp.now()
)