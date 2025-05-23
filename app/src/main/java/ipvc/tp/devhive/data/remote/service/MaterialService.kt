package ipvc.tp.devhive.data.remote.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import ipvc.tp.devhive.data.model.Material
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MaterialService(firestore: FirebaseFirestore) {
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
            val snapshot = materialsCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val materials = snapshot.documents.mapNotNull { it.toObject(Material::class.java) }
            Result.success(materials)
        } catch (e: Exception) {
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
}
