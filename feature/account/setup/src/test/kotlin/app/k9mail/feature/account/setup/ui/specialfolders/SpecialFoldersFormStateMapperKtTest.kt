package app.k9mail.feature.account.setup.ui.specialfolders

import app.k9mail.feature.account.common.domain.entity.Folders
import app.k9mail.feature.account.common.domain.entity.SpecialFolderOption
import app.k9mail.feature.account.common.domain.entity.SpecialSpecialFolderOption
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fsck.k9.mail.FolderType
import com.fsck.k9.mail.folders.FolderServerId
import com.fsck.k9.mail.folders.RemoteFolder
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SpecialFoldersFormStateMapperKtTest {

    @Test
    fun `should map folders to form state and assign selected folders`() = runTest {
        val folders = Folders(
            archiveSpecialFolderOptions = createFolderList(
                SpecialSpecialFolderOption.Archive(
                    createRemoteFolder("archive1"),
                    isAutomatic = true,
                ),
            ),
            draftsSpecialFolderOptions = createFolderList(
                SpecialSpecialFolderOption.Drafts(
                    createRemoteFolder("drafts1"),
                    isAutomatic = true,
                ),
            ),
            sentSpecialFolderOptions = createFolderList(
                SpecialSpecialFolderOption.Sent(
                    createRemoteFolder("sent1"),
                    isAutomatic = true,
                ),
            ),
            spamSpecialFolderOptions = createFolderList(
                SpecialSpecialFolderOption.Spam(
                    createRemoteFolder("spam1"),
                    isAutomatic = true,
                ),
            ),
            trashSpecialFolderOptions = createFolderList(
                SpecialSpecialFolderOption.Trash(
                    createRemoteFolder("trash1"),
                    isAutomatic = true,
                ),
            ),
        )

        val result = folders.toFormState()

        assertThat(result).isEqualTo(
            SpecialFoldersContract.FormState(
                archiveSpecialFolderOptions = folders.archiveSpecialFolderOptions,
                draftsSpecialFolderOptions = folders.draftsSpecialFolderOptions,
                sentSpecialFolderOptions = folders.sentSpecialFolderOptions,
                spamSpecialFolderOptions = folders.spamSpecialFolderOptions,
                trashSpecialFolderOptions = folders.trashSpecialFolderOptions,

                selectedArchiveSpecialFolderOption = folders.archiveSpecialFolderOptions.first(),
                selectedDraftsSpecialFolderOption = folders.draftsSpecialFolderOptions.first(),
                selectedSentSpecialFolderOption = folders.sentSpecialFolderOptions.first(),
                selectedSpamSpecialFolderOption = folders.spamSpecialFolderOptions.first(),
                selectedTrashSpecialFolderOption = folders.trashSpecialFolderOptions.first(),
            ),
        )
    }

    @Test
    fun `should map folders to form state and not assign selected folders when there is none automatic`() {
        val folders = Folders(
            archiveSpecialFolderOptions = createFolderList(SpecialFolderOption.None(isAutomatic = true)),
            draftsSpecialFolderOptions = createFolderList(SpecialFolderOption.None(isAutomatic = true)),
            sentSpecialFolderOptions = createFolderList(SpecialFolderOption.None(isAutomatic = true)),
            spamSpecialFolderOptions = createFolderList(SpecialFolderOption.None(isAutomatic = true)),
            trashSpecialFolderOptions = createFolderList(SpecialFolderOption.None(isAutomatic = true)),
        )

        val result = folders.toFormState()

        assertThat(result).isEqualTo(
            SpecialFoldersContract.FormState(
                archiveSpecialFolderOptions = folders.archiveSpecialFolderOptions,
                draftsSpecialFolderOptions = folders.draftsSpecialFolderOptions,
                sentSpecialFolderOptions = folders.sentSpecialFolderOptions,
                spamSpecialFolderOptions = folders.spamSpecialFolderOptions,
                trashSpecialFolderOptions = folders.trashSpecialFolderOptions,

                selectedArchiveSpecialFolderOption = null,
                selectedDraftsSpecialFolderOption = null,
                selectedSentSpecialFolderOption = null,
                selectedSpamSpecialFolderOption = null,
                selectedTrashSpecialFolderOption = null,
            ),
        )
    }

    private companion object {
        fun createRemoteFolder(name: String): RemoteFolder {
            return RemoteFolder(
                serverId = FolderServerId(name),
                displayName = name,
                type = FolderType.REGULAR,
            )
        }

        fun createFolderList(automaticSpecialFolderOption: SpecialFolderOption): List<SpecialFolderOption> {
            return listOf(
                automaticSpecialFolderOption,
                SpecialFolderOption.None(),
                SpecialFolderOption.Regular(createRemoteFolder("regular1")),
            )
        }
    }
}
