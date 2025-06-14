package ipvc.tp.devhive.data.remote.service

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class MaterialService(private val firestore: FirebaseFirestore) {
    private val materialsCollection = firestore.collection("materials")

    suspend fun getMaterialById(materialId: String): Material? {
        return try {
            val document = materialsCollection.document(materialId).get().await()
            if (document.exists()) {
                document.toObject(Material::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllMaterials(limit: Long = 50): Result<List<Material>> {
        return try {
            Log.d("MaterialService", "🔍 Iniciando busca de materiais no Firestore...")

            val snapshot = materialsCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            Log.d("MaterialService", "📦 Documentos encontrados: ${snapshot.size()}")

            val materials = snapshot.documents.mapNotNull { it.toObject(Material::class.java) }
            Result.success(materials)
        } catch (e: Exception) {
            Log.e("MaterialService", "💥 Erro ao buscar materiais", e)
            Result.failure(e)
        }
    }

    suspend fun getMaterialsByUser(userId: String): Result<List<Material>> {
        return try {
            val snapshot = materialsCollection
                .whereEqualTo("ownerUid", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val materials = snapshot.documents.mapNotNull { it.toObject(Material::class.java) }
            Result.success(materials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMaterialsBySubject(subject: String): Result<List<Material>> {
        return try {
            val snapshot = materialsCollection
                .whereEqualTo("subject", subject)
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val materials = snapshot.documents.mapNotNull { it.toObject(Material::class.java) }
            Result.success(materials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMaterial(material: Material): Result<Material> {
        return try {
            val materialId = material.id.ifEmpty { UUID.randomUUID().toString() }
            val newMaterial = material.copy(id = materialId)
            materialsCollection.document(materialId).set(newMaterial).await()
            Result.success(newMaterial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMaterial(material: Material): Result<Material> {
        return try {
            materialsCollection.document(material.id).set(material, SetOptions.merge()).await()
            Result.success(material)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMaterial(materialId: String): Result<Boolean> {
        return try {
            materialsCollection.document(materialId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleMaterialLike(materialId: String, userId: String, isLiked: Boolean): Result<Boolean> {
        return try {
            val materialRef = firestore.collection("materials").document(materialId)

            firestore.runTransaction { transaction ->
                val materialDoc = transaction.get(materialRef)

                if (materialDoc.exists()) {
                    val currentLikes = materialDoc.getLong("likes") ?: 0L

                    val likedBy = getLikedByList(materialDoc.get("likedBy"))

                    val newLikedBy = if (isLiked) {
                        if (userId !in likedBy) likedBy + userId else likedBy
                    } else {
                        likedBy - userId
                    }

                    val newLikesCount = newLikedBy.size.toLong()

                    transaction.update(materialRef, mapOf(
                        "likes" to newLikesCount,
                        "likedBy" to newLikedBy,
                        "updatedAt" to Timestamp.now()
                    ))
                } else {
                    throw Exception("Material não encontrado")
                }
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleMaterialBookmark(materialId: String, userId: String, isBookmarked: Boolean): Result<Boolean> {
        return try {
            val materialRef = firestore.collection("materials").document(materialId)

            firestore.runTransaction { transaction ->
                val materialDoc = transaction.get(materialRef)

                if (materialDoc.exists()) {
                    val bookmarkedBy = getBookmarkedByList(materialDoc.get("bookmarkedBy"))

                    val newBookmarkedBy = if (isBookmarked) {
                        if (userId !in bookmarkedBy) bookmarkedBy + userId else bookmarkedBy
                    } else {
                        bookmarkedBy - userId
                    }

                    val newBookmarksCount = newBookmarkedBy.size.toLong()

                    transaction.update(materialRef, mapOf(
                        "bookmarks" to newBookmarksCount,
                        "bookmarkedBy" to newBookmarkedBy,
                        "updatedAt" to Timestamp.now()
                    ))
                } else {
                    throw Exception("Material não encontrado")
                }
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getBookmarkedByList(data: Any?): List<String> {
        return when (data) {
            is List<*> -> {
                data.filterIsInstance<String>()
            }
            null -> emptyList()
            else -> emptyList()
        }
    }

    private fun getLikedByList(data: Any?): List<String> {
        return when (data) {
            is List<*> -> {
                data.filterIsInstance<String>()
            }
            null -> emptyList()
            else -> emptyList()
        }
    }

    suspend fun incrementMaterialViews(materialId: String): Result<Boolean> {
        return try {
            materialsCollection.document(materialId)
                .update("views", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementMaterialDownloads(materialId: String): Result<Boolean> {
        return try {
            materialsCollection.document(materialId)
                .update("downloads", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMaterials(query: String): Result<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                val queryLowerCase = query.lowercase()

                val snapshot = firestore.collection("materials")
                    .whereEqualTo("isPublic", true)
                    .get()
                    .await()

                val materials = snapshot.documents.mapNotNull { document ->
                    try {
                        val material = document.toObject(Material::class.java)?.copy(id = document.id)

                        // Filtro local para pesquisa em múltiplos campos
                        material?.let {
                            if (it.title.lowercase().contains(queryLowerCase) ||
                                it.description.lowercase().contains(queryLowerCase) ||
                                it.subject.lowercase().contains(queryLowerCase) ||
                                it.ownerName.lowercase().contains(queryLowerCase)) {
                                it
                            } else null
                        }
                    } catch (e: Exception) {
                        Log.e("MaterialService", "Error parsing material: ${e.message}")
                        null
                    }
                }

                Result.success(materials.sortedByDescending { it.createdAt })
            } catch (e: Exception) {
                Log.e("MaterialService", "Error searching materials", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getDistinctSubjects(): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("materials")
                    .whereEqualTo("isPublic", true)
                    .get()
                    .await()

                val subjects = snapshot.documents.mapNotNull { document ->
                    try {
                        document.getString("subject")?.takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        null
                    }
                }.distinct().sorted()

                Result.success(subjects)
            } catch (e: Exception) {
                Log.e("MaterialService", "Error getting distinct subjects", e)
                Result.failure(e)
            }
        }
    }

    suspend fun searchMaterialsWithSubject(query: String, subject: String?): Result<List<Material>> {
        return withContext(Dispatchers.IO) {
            try {
                val queryLowerCase = query.lowercase()

                var firestoreQuery = firestore.collection("materials")
                    .whereEqualTo("isPublic", true)

                if (subject != null) {
                    firestoreQuery = firestoreQuery.whereEqualTo("subject", subject)
                }

                val snapshot = firestoreQuery.get().await()

                val materials = snapshot.documents.mapNotNull { document ->
                    try {
                        val material = document.toObject(Material::class.java)?.copy(id = document.id)

                        material?.let {
                            if (query.isNotBlank()) {
                                if (it.title.lowercase().contains(queryLowerCase) ||
                                    it.description.lowercase().contains(queryLowerCase) ||
                                    it.subject.lowercase().contains(queryLowerCase) ||
                                    it.ownerName.lowercase().contains(queryLowerCase)) {
                                    it
                                } else null
                            } else {
                                it
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MaterialService", "Error parsing material: ${e.message}")
                        null
                    }
                }

                Result.success(materials.sortedByDescending { it.createdAt })
            } catch (e: Exception) {
                Log.e("MaterialService", "Error searching materials with subject", e)
                Result.failure(e)
            }
        }
    }
}
