package com.fsck.k9.preferences

import com.fsck.k9.Account
import com.fsck.k9.mailstore.FolderRepository
import com.fsck.k9.mailstore.RemoteFolderDetails

class FolderSettingsProvider(private val folderRepository: FolderRepository) {
    fun getFolderSettings(account: Account): List<FolderSettings> {
        return folderRepository.getRemoteFolderDetails(account)
            .filterNot { it.containsOnlyDefaultValues() }
            .map { it.toFolderSettings() }
    }

    private fun RemoteFolderDetails.containsOnlyDefaultValues(): Boolean {
        return isInTopGroup == getDefaultValue("inTopGroup") &&
            isIntegrate == getDefaultValue("integrate") &&
            isAutoSyncViaPollEnabled == getDefaultValue("syncMode") &&
            isHidden == getDefaultValue("displayMode") &&
            isNotificationEnabled == getDefaultValue("notifyMode") &&
            isAutoSyncViaPushEnabled == getDefaultValue("pushMode")
    }

    private fun getDefaultValue(key: String): Any? {
        val versionedSetting = FolderSettingsDescriptions.SETTINGS[key] ?: error("Key not found: $key")
        val highestVersion = versionedSetting.lastKey()
        val setting = versionedSetting[highestVersion] ?: error("Setting description not found: $key")
        return setting.defaultValue
    }

    private fun RemoteFolderDetails.toFolderSettings(): FolderSettings {
        return FolderSettings(
            folder.serverId,
            isIntegrate,
            isHidden,
            isInTopGroup,
            isAutoSyncViaPollEnabled,
            isAutoSyncViaPushEnabled,
            isNotificationEnabled,
        )
    }
}

data class FolderSettings(
    val serverId: String,
    val isIntegrate: Boolean,
    val isHidden: Boolean,
    val isInTopGroup: Boolean,
    val isAutoSyncViaPollEnabled: Boolean,
    val isAutoSyncViaPushEnabled: Boolean,
    val isNotificationEnabled: Boolean,
)
