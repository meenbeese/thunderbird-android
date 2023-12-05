package app.k9mail.feature.account.setup.ui.specialfolders

import app.k9mail.feature.account.common.domain.entity.SpecialFolderOption
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.FormEvent
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.FormState
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fsck.k9.mail.FolderType
import com.fsck.k9.mail.folders.FolderServerId
import com.fsck.k9.mail.folders.RemoteFolder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpecialFoldersFormUiModelTest {

    private val testSubject = SpecialFoldersFormUiModel()

    @Test
    fun `should change archive folder on ArchiveFolderChanged`() {
        val folder = createFolder("archiveFolder")

        val result = testSubject.event(FormEvent.ArchiveFolderChanged(folder), FORM_STATE)

        assertThat(result).isEqualTo(FORM_STATE.copy(selectedArchiveSpecialFolderOption = folder))
    }

    @Test
    fun `should change drafts folder on DraftsFolderChanged`() {
        val folder = createFolder("draftsFolder")

        val result = testSubject.event(FormEvent.DraftsFolderChanged(folder), FORM_STATE)

        assertThat(result).isEqualTo(FORM_STATE.copy(selectedDraftsSpecialFolderOption = folder))
    }

    @Test
    fun `should change sent folder on SentFolderChanged`() {
        val folder = createFolder("sentFolder")

        val result = testSubject.event(FormEvent.SentFolderChanged(folder), FORM_STATE)

        assertThat(result).isEqualTo(FORM_STATE.copy(selectedSentSpecialFolderOption = folder))
    }

    @Test
    fun `should change spam folder on SpamFolderChanged`() {
        val folder = createFolder("spamFolder")

        val result = testSubject.event(FormEvent.SpamFolderChanged(folder), FORM_STATE)

        assertThat(result).isEqualTo(FORM_STATE.copy(selectedSpamSpecialFolderOption = createFolder("spamFolder")))
    }

    @Test
    fun `should change trash folder on TrashFolderChanged`() {
        val folder = createFolder("trashFolder")

        val result = testSubject.event(FormEvent.TrashFolderChanged(folder), FORM_STATE)

        assertThat(result).isEqualTo(FORM_STATE.copy(selectedTrashSpecialFolderOption = folder))
    }

    @Test
    fun `validate should return true when all folders are selected`() = runTest {
        val result = testSubject.validate(FORM_STATE_WITH_SELECTION)

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `validate should return false when one of the folders is not selected`() = runTest {
        val archiveResult = testSubject.validate(
            FORM_STATE_WITH_SELECTION.copy(
                selectedArchiveSpecialFolderOption = null,
            ),
        )
        val draftsResult = testSubject.validate(
            FORM_STATE_WITH_SELECTION.copy(
                selectedDraftsSpecialFolderOption = null,
            ),
        )
        val sentResult = testSubject.validate(
            FORM_STATE_WITH_SELECTION.copy(
                selectedSentSpecialFolderOption = null,
            ),
        )
        val spamResult = testSubject.validate(
            FORM_STATE_WITH_SELECTION.copy(
                selectedSpamSpecialFolderOption = null,
            ),
        )
        val trashResult = testSubject.validate(
            FORM_STATE_WITH_SELECTION.copy(
                selectedTrashSpecialFolderOption = null,
            ),
        )

        assertThat(archiveResult).isEqualTo(false)
        assertThat(draftsResult).isEqualTo(false)
        assertThat(sentResult).isEqualTo(false)
        assertThat(spamResult).isEqualTo(false)
        assertThat(trashResult).isEqualTo(false)
    }

    private companion object {
        val FORM_STATE = FormState(
            archiveSpecialFolderOptions = listOf(createFolder("archiveFolder")),
            draftsSpecialFolderOptions = listOf(createFolder("draftsFolder")),
            sentSpecialFolderOptions = listOf(createFolder("sentFolder")),
            spamSpecialFolderOptions = listOf(createFolder("spamFolder")),
            trashSpecialFolderOptions = listOf(createFolder("trashFolder")),
        )

        val FORM_STATE_WITH_SELECTION = FORM_STATE.copy(
            selectedArchiveSpecialFolderOption = createFolder("archiveFolder"),
            selectedDraftsSpecialFolderOption = createFolder("draftsFolder"),
            selectedSentSpecialFolderOption = createFolder("sentFolder"),
            selectedSpamSpecialFolderOption = createFolder("spamFolder"),
            selectedTrashSpecialFolderOption = createFolder("trashFolder"),
        )

        fun createFolder(folderName: String): SpecialFolderOption {
            return SpecialFolderOption.Regular(
                RemoteFolder(
                    serverId = FolderServerId(folderName),
                    displayName = folderName,
                    type = FolderType.REGULAR,
                ),
            )
        }
    }
}
