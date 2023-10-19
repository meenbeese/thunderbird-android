package com.fsck.k9.storage.messages

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.fsck.k9.mail.FolderClass
import com.fsck.k9.mail.FolderType
import com.fsck.k9.mailstore.CreateFolderInfo
import com.fsck.k9.mailstore.FolderSettings
import com.fsck.k9.storage.RobolectricTest
import org.junit.Test

class CreateFolderOperationsTest : RobolectricTest() {
    private val sqliteDatabase = createDatabase()
    private val lockableDatabase = createLockableDatabaseMock(sqliteDatabase)
    private val createFolderOperations = CreateFolderOperations(lockableDatabase)

    @Test
    fun `create single folder`() {
        createFolderOperations.createFolders(
            listOf(
                CreateFolderInfo(
                    serverId = "archived_messages",
                    name = "Archive",
                    type = FolderType.ARCHIVE,
                    settings = FolderSettings(
                        visibleLimit = 10,
                        integrate = false,
                        isHidden = FolderClass.FIRST_CLASS,
                        inTopGroup = true,
                        isAutoSyncViaPollEnabled = FolderClass.SECOND_CLASS,
                        isAutoSyncViaPushEnabled = FolderClass.NO_CLASS,
                        isNotificationEnabled = FolderClass.NO_CLASS,
                    ),
                ),
            ),
        )

        val folders = sqliteDatabase.readFolders()
        assertThat(folders).hasSize(1)
        val folder = folders.first()
        assertThat(folder.serverId).isEqualTo("archived_messages")
        assertThat(folder.name).isEqualTo("Archive")
        assertThat(folder.type).isEqualTo("archive")
        assertThat(folder.visibleLimit).isEqualTo(10)
        assertThat(folder.displayClass).isEqualTo("FIRST_CLASS")
        assertThat(folder.syncClass).isEqualTo("SECOND_CLASS")
        assertThat(folder.notifyClass).isEqualTo("NO_CLASS")
        assertThat(folder.pushClass).isEqualTo("NO_CLASS")
        assertThat(folder.inTopGroup).isEqualTo(1)
        assertThat(folder.integrate).isEqualTo(0)
    }

    @Test
    fun `create multiple folders`() {
        createFolderOperations.createFolders(
            listOf(
                createCreateFolderInfo(serverId = "folder1", name = "Inbox"),
                createCreateFolderInfo(serverId = "folder2", name = "Sent"),
                createCreateFolderInfo(serverId = "folder3", name = "Trash"),
            ),
        )

        val folders = sqliteDatabase.readFolders()
        assertThat(folders).hasSize(3)
        assertThat(folders.map { it.serverId to it.name }.toSet()).isEqualTo(
            setOf(
                "folder1" to "Inbox",
                "folder2" to "Sent",
                "folder3" to "Trash",
            ),
        )
    }

    fun createCreateFolderInfo(serverId: String, name: String): CreateFolderInfo {
        return CreateFolderInfo(
            serverId = serverId,
            name = name,
            type = FolderType.REGULAR,
            settings = FolderSettings(
                visibleLimit = 25,
                integrate = false,
                isHidden = FolderClass.NO_CLASS,
                inTopGroup = false,
                isAutoSyncViaPollEnabled = FolderClass.INHERITED,
                isAutoSyncViaPushEnabled = FolderClass.NO_CLASS,
                isNotificationEnabled = FolderClass.INHERITED,
            ),
        )
    }
}
