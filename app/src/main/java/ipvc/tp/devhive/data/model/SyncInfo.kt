package ipvc.tp.devhive.data.model

import com.google.firebase.Timestamp

data class SyncInfo(
    val deviceId: String = "",
    val userUid: String = "",
    val lastSyncTimestamp: Timestamp = Timestamp.now(),
    val pendingUploads: Int = 0,
    val syncStatus: String = "",
    val lastError: String = "",
    val lastSyncedCollections: Map<String, Timestamp> = emptyMap()
)