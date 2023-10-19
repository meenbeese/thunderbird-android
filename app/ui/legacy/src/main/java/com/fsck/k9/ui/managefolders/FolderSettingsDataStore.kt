package com.fsck.k9.ui.managefolders

import androidx.preference.PreferenceDataStore
import com.fsck.k9.Account
import com.fsck.k9.mailstore.FolderDetails
import com.fsck.k9.mailstore.FolderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FolderSettingsDataStore(
    private val folderRepository: FolderRepository,
    private val account: Account,
    private var folder: FolderDetails,
) : PreferenceDataStore() {
    private val saveScope = CoroutineScope(GlobalScope.coroutineContext + Dispatchers.IO)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return when (key) {
            "folder_settings_in_top_group" -> folder.isInTopGroup
            "folder_settings_include_in_integrated_inbox" -> folder.isIntegrate
            "folder_settings_folder_display_mode" -> folder.isHidden
            "folder_settings_folder_sync_mode" -> folder.isAutoSyncViaPollEnabled
            "folder_settings_folder_notify_mode" -> folder.isNotificationEnabled
            "folder_settings_folder_push_mode" -> folder.isAutoSyncViaPushEnabled
            else -> error("Unknown key: $key")
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        return when (key) {
            "folder_settings_in_top_group" -> updateFolder(folder.copy(isInTopGroup = value))
            "folder_settings_include_in_integrated_inbox" -> updateFolder(folder.copy(isIntegrate = value))
            "folder_settings_folder_display_mode" -> updateFolder(folder.copy(isHidden = value))
            "folder_settings_folder_sync_mode" -> updateFolder(folder.copy(isAutoSyncViaPollEnabled = value))
            "folder_settings_folder_notify_mode" -> updateFolder(folder.copy(isNotificationEnabled = value))
            "folder_settings_folder_push_mode" -> updateFolder(folder.copy(isAutoSyncViaPushEnabled = value))
            else -> error("Unknown key: $key")
        }
    }

    private fun updateFolder(newFolder: FolderDetails) {
        folder = newFolder
        saveScope.launch {
            folderRepository.updateFolderDetails(account, newFolder)
        }
    }
}
