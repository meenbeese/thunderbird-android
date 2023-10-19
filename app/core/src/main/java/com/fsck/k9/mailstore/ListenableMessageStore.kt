package com.fsck.k9.mailstore

import java.util.concurrent.CopyOnWriteArraySet

class ListenableMessageStore(private val messageStore: MessageStore) : MessageStore by messageStore {
    private val folderSettingsListener = CopyOnWriteArraySet<FolderSettingsChangedListener>()

    override fun createFolders(folders: List<CreateFolderInfo>) {
        messageStore.createFolders(folders)
        notifyFolderSettingsChanged()
    }

    override fun deleteFolders(folderServerIds: List<String>) {
        messageStore.deleteFolders(folderServerIds)
        notifyFolderSettingsChanged()
    }

    override fun updateFolderSettings(folderDetails: FolderDetails) {
        messageStore.updateFolderSettings(folderDetails)
        notifyFolderSettingsChanged()
    }

    override fun setIncludeInUnifiedInbox(folderId: Long, includeInUnifiedInbox: Boolean) {
        messageStore.setIncludeInUnifiedInbox(folderId, includeInUnifiedInbox)
        notifyFolderSettingsChanged()
    }

    override fun setHidden(folderId: Long, hidden: Boolean) {
        messageStore.setHidden(folderId, hidden)
        notifyFolderSettingsChanged()
    }

    override fun setAutoSyncViaPollEnabled(folderId: Long, enable: Boolean) {
        messageStore.setAutoSyncViaPollEnabled(folderId, enable)
        notifyFolderSettingsChanged()
    }

    override fun setAutoSyncViaPushEnabled(folderId: Long, enable: Boolean) {
        messageStore.setAutoSyncViaPushEnabled(folderId, enable)
        notifyFolderSettingsChanged()
    }

    override fun setNotificationEnabled(folderId: Long, enable: Boolean) {
        messageStore.setNotificationEnabled(folderId, enable)
        notifyFolderSettingsChanged()
    }

    fun addFolderSettingsChangedListener(listener: FolderSettingsChangedListener) {
        folderSettingsListener.add(listener)
    }

    fun removeFolderSettingsChangedListener(listener: FolderSettingsChangedListener) {
        folderSettingsListener.remove(listener)
    }

    private fun notifyFolderSettingsChanged() {
        for (listener in folderSettingsListener) {
            listener.onFolderSettingsChanged()
        }
    }
}

fun interface FolderSettingsChangedListener {
    fun onFolderSettingsChanged()
}
