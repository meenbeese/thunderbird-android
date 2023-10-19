package com.fsck.k9.mailstore

import com.fsck.k9.Account
import com.fsck.k9.Preferences

/**
 * Provides imported folder settings if available, otherwise default values.
 */
class FolderSettingsProvider(val preferences: Preferences, val account: Account) {
    fun getFolderSettings(folderServerId: String): FolderSettings {
        val storage = preferences.storage
        val prefix = "${account.uuid}.$folderServerId"

        //FIXME
        return FolderSettings(
            visibleLimit = account.displayCount,
            integrate = false,
            isHidden = false,
            inTopGroup = false,
            isAutoSyncViaPollEnabled = false,
            isAutoSyncViaPushEnabled = false,
            isNotificationEnabled = false,
        ).also {
            removeImportedFolderSettings(prefix)
        }
    }

    private fun removeImportedFolderSettings(prefix: String) {
        val editor = preferences.createStorageEditor()

        editor.remove("$prefix.displayMode")
        editor.remove("$prefix.syncMode")
        editor.remove("$prefix.notifyMode")
        editor.remove("$prefix.pushMode")
        editor.remove("$prefix.inTopGroup")
        editor.remove("$prefix.integrate")

        editor.commit()
    }
}
