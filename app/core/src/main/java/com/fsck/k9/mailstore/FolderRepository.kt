package com.fsck.k9.mailstore

import com.fsck.k9.Account
import com.fsck.k9.DI
import com.fsck.k9.controller.MessagingController
import com.fsck.k9.controller.SimpleMessagingListener
import com.fsck.k9.preferences.AccountManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.fsck.k9.mail.FolderType as RemoteFolderType

@OptIn(ExperimentalCoroutinesApi::class)
class FolderRepository(
    private val messageStoreManager: MessageStoreManager,
    private val accountManager: AccountManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val sortForDisplay =
        compareByDescending<DisplayFolder> { it.folder.type == FolderType.INBOX }
            .thenByDescending { it.folder.type == FolderType.OUTBOX }
            .thenByDescending { it.folder.type != FolderType.REGULAR }
            .thenByDescending { it.isInTopGroup }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.folder.name }

    fun getDisplayFolders(account: Account, showHiddenFolders: Boolean): List<DisplayFolder> {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getDisplayFolders(showHiddenFolders, outboxFolderId = account.outboxFolderId) { folder ->
            DisplayFolder(
                folder = Folder(
                    id = folder.id,
                    name = folder.name,
                    type = folderTypeOf(account, folder.id),
                    isLocalOnly = folder.isLocalOnly,
                ),
                isInTopGroup = folder.isInTopGroup,
                unreadMessageCount = folder.unreadMessageCount,
                starredMessageCount = folder.starredMessageCount,
            )
        }.sortedWith(sortForDisplay)
    }

    fun getDisplayFoldersFlow(account: Account, showHiddenFolders: Boolean = false): Flow<List<DisplayFolder>> {
        val messagingController = DI.get<MessagingController>()
        val messageStore = messageStoreManager.getMessageStore(account)

        return callbackFlow {
            send(getDisplayFolders(account, showHiddenFolders))

            val folderStatusChangedListener = object : SimpleMessagingListener() {
                override fun folderStatusChanged(statusChangedAccount: Account, folderId: Long) {
                    if (statusChangedAccount.uuid == account.uuid) {
                        trySendBlocking(getDisplayFolders(account, showHiddenFolders))
                    }
                }
            }
            messagingController.addListener(folderStatusChangedListener)

            val folderSettingsChangedListener = FolderSettingsChangedListener {
                trySendBlocking(getDisplayFolders(account, showHiddenFolders))
            }
            messageStore.addFolderSettingsChangedListener(folderSettingsChangedListener)

            awaitClose {
                messagingController.removeListener(folderStatusChangedListener)
                messageStore.removeFolderSettingsChangedListener(folderSettingsChangedListener)
            }
        }.buffer(capacity = Channel.CONFLATED)
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    fun getFolder(account: Account, folderId: Long): Folder? {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolder(folderId) { folder ->
            Folder(
                id = folder.id,
                name = folder.name,
                type = folderTypeOf(account, folder.id),
                isLocalOnly = folder.isLocalOnly,
            )
        }
    }

    fun getFolderDetails(account: Account, folderId: Long): FolderDetails? {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolder(folderId) { folder ->
            FolderDetails(
                folder = Folder(
                    id = folder.id,
                    name = folder.name,
                    type = folderTypeOf(account, folder.id),
                    isLocalOnly = folder.isLocalOnly,
                ),
                isIntegrate = folder.isIntegrate,
                isHidden = folder.isHidden,
                isInTopGroup = folder.isInTopGroup,
                isAutoSyncViaPollEnabled = folder.isAutoSyncViaPollEnabled,
                isAutoSyncViaPushEnabled = folder.isAutoSyncViaPushEnabled,
                isNotificationEnabled = folder.isNotificationEnabled,
            )
        }
    }

    fun getRemoteFolders(account: Account): List<RemoteFolder> {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolders(excludeLocalOnly = true) { folder ->
            RemoteFolder(
                id = folder.id,
                serverId = folder.serverIdOrThrow(),
                name = folder.name,
                type = folder.type.toFolderType(),
            )
        }
    }

    fun getRemoteFolderDetails(account: Account): List<RemoteFolderDetails> {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolders(excludeLocalOnly = true) { folder ->
            RemoteFolderDetails(
                folder = RemoteFolder(
                    id = folder.id,
                    serverId = folder.serverIdOrThrow(),
                    name = folder.name,
                    type = folder.type.toFolderType(),
                ),
                isIntegrate = folder.isIntegrate,
                isHidden = folder.isHidden,
                isInTopGroup = folder.isInTopGroup,
                isAutoSyncViaPollEnabled = folder.isAutoSyncViaPollEnabled,
                isAutoSyncViaPushEnabled = folder.isAutoSyncViaPushEnabled,
                isNotificationEnabled = folder.isNotificationEnabled,
            )
        }
    }

    fun getPushFoldersFlow(account: Account): Flow<List<RemoteFolder>> {
        return account.getPushEnabledFlow()
            .flatMapLatest { isPushEnabled ->
                if (isPushEnabled) {
                    getPushFoldersFlowInternal(account)
                } else {
                    flowOf(emptyList())
                }
            }
    }

    private fun getPushFoldersFlowInternal(account: Account): Flow<List<RemoteFolder>> {
        val messageStore = messageStoreManager.getMessageStore(account)
        return callbackFlow {
            send(getPushFolders(account))

            val listener = FolderSettingsChangedListener {
                trySendBlocking(getPushFolders(account))
            }
            messageStore.addFolderSettingsChangedListener(listener)

            awaitClose {
                messageStore.removeFolderSettingsChangedListener(listener)
            }
        }.buffer(capacity = Channel.CONFLATED)
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    private fun getPushFolders(account: Account): List<RemoteFolder> {
        return getRemoteFolderDetails(account)
            .asSequence()
            .filter { it.isAutoSyncViaPushEnabled }
            .map { folderDetails -> folderDetails.folder }
            .toList()
    }

    fun getFolderServerId(account: Account, folderId: Long): String? {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolder(folderId) { folder ->
            folder.serverId
        }
    }

    fun getFolderId(account: Account, folderServerId: String): Long? {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolderId(folderServerId)
    }

    fun isFolderPresent(account: Account, folderId: Long): Boolean {
        val messageStore = messageStoreManager.getMessageStore(account)
        return messageStore.getFolder(folderId) { true } ?: false
    }

    fun updateFolderDetails(account: Account, folderDetails: FolderDetails) {
        val messageStore = messageStoreManager.getMessageStore(account)
        messageStore.updateFolderSettings(folderDetails)
    }

    fun setIncludeInUnifiedInbox(account: Account, folderId: Long, includeInUnifiedInbox: Boolean) {
        val messageStore = messageStoreManager.getMessageStore(account)
        messageStore.setIncludeInUnifiedInbox(folderId, includeInUnifiedInbox)
    }

    fun setHidden(account: Account, folderId: Long, hidden: Boolean) {
        val messageStore = messageStoreManager.getMessageStore(account)
        messageStore.setHidden(folderId, hidden)
    }

    fun setAutoSyncViaPollEnabled(account: Account, folderId: Long, enable: Boolean) {
        val messageStore = messageStoreManager.getMessageStore(account)
        messageStore.setAutoSyncViaPollEnabled(folderId, enable)
    }

    fun setAutoSyncViaPushEnabled(account: Account, folderId: Long, enable: Boolean) {
        val messageStore = messageStoreManager.getMessageStore(account)
        messageStore.setAutoSyncViaPushEnabled(folderId, enable)
    }

    fun setNotificationEnabled(account: Account, folderId: Long, enable: Boolean) {
        val messageStore = messageStoreManager.getMessageStore(account)
        messageStore.setNotificationEnabled(folderId, enable)
    }

    private fun folderTypeOf(account: Account, folderId: Long) = when (folderId) {
        account.inboxFolderId -> FolderType.INBOX
        account.outboxFolderId -> FolderType.OUTBOX
        account.sentFolderId -> FolderType.SENT
        account.trashFolderId -> FolderType.TRASH
        account.draftsFolderId -> FolderType.DRAFTS
        account.archiveFolderId -> FolderType.ARCHIVE
        account.spamFolderId -> FolderType.SPAM
        else -> FolderType.REGULAR
    }

    private fun RemoteFolderType.toFolderType(): FolderType = when (this) {
        RemoteFolderType.REGULAR -> FolderType.REGULAR
        RemoteFolderType.INBOX -> FolderType.INBOX
        RemoteFolderType.OUTBOX -> FolderType.REGULAR // We currently don't support remote Outbox folders
        RemoteFolderType.DRAFTS -> FolderType.DRAFTS
        RemoteFolderType.SENT -> FolderType.SENT
        RemoteFolderType.TRASH -> FolderType.TRASH
        RemoteFolderType.SPAM -> FolderType.SPAM
        RemoteFolderType.ARCHIVE -> FolderType.ARCHIVE
    }

    private fun Account.getPushEnabledFlow(): Flow<Boolean> {
        return accountManager.getAccountFlow(uuid).map { it.isPushEnabled }
    }
}

data class Folder(val id: Long, val name: String, val type: FolderType, val isLocalOnly: Boolean)

data class RemoteFolder(val id: Long, val serverId: String, val name: String, val type: FolderType)

data class FolderDetails(
    val folder: Folder,
    val isIntegrate: Boolean,
    val isHidden: Boolean,
    val isInTopGroup: Boolean,
    val isAutoSyncViaPollEnabled: Boolean,
    val isAutoSyncViaPushEnabled: Boolean,
    val isNotificationEnabled: Boolean,
)

data class RemoteFolderDetails(
    val folder: RemoteFolder,
    val isIntegrate: Boolean,
    val isHidden: Boolean,
    val isInTopGroup: Boolean,
    val isAutoSyncViaPollEnabled: Boolean,
    val isAutoSyncViaPushEnabled: Boolean,
    val isNotificationEnabled: Boolean,
)

data class DisplayFolder(
    val folder: Folder,
    val isInTopGroup: Boolean,
    val unreadMessageCount: Int,
    val starredMessageCount: Int,
)

enum class FolderType {
    REGULAR,
    INBOX,
    OUTBOX,
    SENT,
    TRASH,
    DRAFTS,
    ARCHIVE,
    SPAM,
}
