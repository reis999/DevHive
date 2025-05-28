package ipvc.tp.devhive.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import ipvc.tp.devhive.data.repository.ChatRepository
import ipvc.tp.devhive.data.repository.CommentRepository
import ipvc.tp.devhive.data.repository.MaterialRepository
import ipvc.tp.devhive.data.repository.StudyGroupRepository
import ipvc.tp.devhive.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SyncManager(
    context: Context,
    private val userRepository: UserRepository,
    private val materialRepository: MaterialRepository,
    private val commentRepository: CommentRepository,
    private val chatRepository: ChatRepository,
    private val studyGroupRepository: StudyGroupRepository,
    private val appScope: CoroutineScope
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val isSyncing = AtomicBoolean(false)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            syncPendingData()
        }
    }

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun syncPendingData() {
        if (isSyncing.getAndSet(true)) {
            return
        }

        appScope.launch(Dispatchers.IO) {
            try {
                userRepository.syncPendingUsers()
                materialRepository.syncPendingMaterials()
                commentRepository.syncPendingComments()
                chatRepository.syncPendingChats()
                chatRepository.syncPendingMessages()
                studyGroupRepository.syncPendingStudyGroups()
                studyGroupRepository.syncPendingGroupMessages()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncing.set(false)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun syncDataManually() {
        if (isNetworkAvailable()) {
            syncPendingData()
        }
    }
}
