package com.fsck.k9.storage.messages

import android.content.ContentValues
import com.fsck.k9.mail.FolderType
import com.fsck.k9.mailstore.FolderDetails
import com.fsck.k9.mailstore.LockableDatabase
import com.fsck.k9.mailstore.MoreMessages
import com.fsck.k9.mailstore.toDatabaseFolderType

internal class UpdateFolderOperations(private val lockableDatabase: LockableDatabase) {
    fun changeFolder(folderServerId: String, name: String, type: FolderType) {
        lockableDatabase.execute(false) { db ->
            val values = ContentValues().apply {
                put("name", name)
                put("type", type.toDatabaseFolderType())
            }

            db.update("folders", values, "server_id = ?", arrayOf(folderServerId))
        }
    }

    fun updateFolderSettings(folderDetails: FolderDetails) {
        lockableDatabase.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put("integrate", folderDetails.isIntegrate)
                put("hidden", folderDetails.isHidden)
                put("top_group", folderDetails.isInTopGroup)
                put("auto_sync_poll", folderDetails.isAutoSyncViaPollEnabled)
                put("auto_sync_push", folderDetails.isAutoSyncViaPushEnabled)
                put("notify", folderDetails.isNotificationEnabled)
            }

            db.update("folders", contentValues, "id = ?", arrayOf(folderDetails.folder.id.toString()))
        }
    }

    fun setIncludeInUnifiedInbox(folderId: Long, includeInUnifiedInbox: Boolean) {
        setBoolean(folderId = folderId, columnName = "integrate", value = includeInUnifiedInbox)
    }

    fun setHidden(folderId: Long, hidden: Boolean) {
        setBoolean(folderId = folderId, columnName = "hidden", value = hidden)
    }

    fun setAutoSyncViaPollEnabled(folderId: Long, enable: Boolean) {
        setBoolean(folderId = folderId, columnName = "auto_sync_poll", value = enable)
    }

    fun setAutoSyncViaPushEnabled(folderId: Long, enable: Boolean) {
        setBoolean(folderId = folderId, columnName = "auto_sync_push", value = enable)
    }

    fun setNotificationEnabled(folderId: Long, enable: Boolean) {
        setBoolean(folderId = folderId, columnName = "notify", value = enable)
    }

    fun setMoreMessages(folderId: Long, moreMessages: MoreMessages) {
        setString(folderId = folderId, columnName = "more_messages", value = moreMessages.databaseName)
    }

    fun setLastChecked(folderId: Long, timestamp: Long) {
        lockableDatabase.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put("last_updated", timestamp)
            }

            db.update("folders", contentValues, "id = ?", arrayOf(folderId.toString()))
        }
    }

    fun setStatus(folderId: Long, status: String?) {
        setString(folderId = folderId, columnName = "status", value = status)
    }

    fun setVisibleLimit(folderId: Long, visibleLimit: Int) {
        lockableDatabase.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put("visible_limit", visibleLimit)
            }

            db.update("folders", contentValues, "id = ?", arrayOf(folderId.toString()))
        }
    }

    private fun setString(folderId: Long, columnName: String, value: String?) {
        lockableDatabase.execute(false) { db ->
            val contentValues = ContentValues().apply {
                if (value == null) {
                    putNull(columnName)
                } else {
                    put(columnName, value)
                }
            }

            db.update("folders", contentValues, "id = ?", arrayOf(folderId.toString()))
        }
    }

    private fun setBoolean(folderId: Long, columnName: String, value: Boolean) {
        lockableDatabase.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put(columnName, value)
            }

            db.update("folders", contentValues, "id = ?", arrayOf(folderId.toString()))
        }
    }
}
