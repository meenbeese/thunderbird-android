package app.k9mail.feature.account.setup.ui.specialfolders

import app.k9mail.core.ui.compose.testing.MainDispatcherRule
import app.k9mail.core.ui.compose.testing.mvi.assertThatAndEffectTurbineConsumed
import app.k9mail.core.ui.compose.testing.mvi.assertThatAndStateTurbineConsumed
import app.k9mail.core.ui.compose.testing.mvi.turbinesWithInitialStateCheck
import app.k9mail.feature.account.common.data.InMemoryAccountStateRepository
import app.k9mail.feature.account.common.domain.AccountDomainContract
import app.k9mail.feature.account.common.domain.entity.AccountState
import app.k9mail.feature.account.common.domain.entity.SpecialFolderOption
import app.k9mail.feature.account.common.domain.entity.SpecialFolderOptions
import app.k9mail.feature.account.common.domain.entity.SpecialFolderSettings
import app.k9mail.feature.account.common.domain.entity.SpecialSpecialFolderOption
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.Effect
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.Event
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.FormEvent
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.FormState
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.State
import app.k9mail.feature.account.setup.ui.specialfolders.fake.FakeSpecialFoldersFormUiModel
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.fsck.k9.mail.FolderType
import com.fsck.k9.mail.folders.FolderFetcherException
import com.fsck.k9.mail.folders.FolderServerId
import com.fsck.k9.mail.folders.RemoteFolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SpecialFoldersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `should load folders, validate and save successfully when LoadSpecialFolders event received and setup valid`() =
        runTest {
            val accountStateRepository = InMemoryAccountStateRepository()
            val initialState = State(
                isLoading = true,
            )
            val testSubject = createTestSubject(
                formUiModel = FakeSpecialFoldersFormUiModel(
                    isValid = true,
                ),
                accountStateRepository = accountStateRepository,
                initialState = initialState,
            )
            val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

            testSubject.event(Event.LoadSpecialFolderOptions)

            val populatedState = initialState.copy(
                isLoading = true,
                isSuccess = false,
                formState = FormState(
                    archiveSpecialFolderOptions = SpecialFolderOptions.archiveSpecialFolderOptions,
                    draftsSpecialFolderOptions = SpecialFolderOptions.draftsSpecialFolderOptions,
                    sentSpecialFolderOptions = SpecialFolderOptions.sentSpecialFolderOptions,
                    spamSpecialFolderOptions = SpecialFolderOptions.spamSpecialFolderOptions,
                    trashSpecialFolderOptions = SpecialFolderOptions.trashSpecialFolderOptions,

                    selectedArchiveSpecialFolderOption = SPECIAL_FOLDER_ARCHIVE.copy(isAutomatic = true),
                    selectedDraftsSpecialFolderOption = SPECIAL_FOLDER_DRAFTS.copy(isAutomatic = true),
                    selectedSentSpecialFolderOption = SPECIAL_FOLDER_SENT.copy(isAutomatic = true),
                    selectedSpamSpecialFolderOption = SPECIAL_FOLDER_SPAM.copy(isAutomatic = true),
                    selectedTrashSpecialFolderOption = SPECIAL_FOLDER_TRASH.copy(isAutomatic = true),
                ),
            )

            assertThat(turbines.awaitStateItem()).isEqualTo(populatedState)

            val validatingState = populatedState.copy(
                isLoading = true,
                isSuccess = true,
            )
            assertThat(turbines.awaitStateItem()).isEqualTo(validatingState)

            val successState = populatedState.copy(
                isLoading = false,
                isSuccess = true,
            )
            turbines.assertThatAndStateTurbineConsumed {
                isEqualTo(successState)
            }

            turbines.assertThatAndEffectTurbineConsumed {
                isEqualTo(Effect.NavigateNext)
            }

            assertThat(accountStateRepository.getState()).isEqualTo(
                AccountState(
                    specialFolderSettings = SpecialFolderSettings(
                        archiveSpecialFolderOption = SPECIAL_FOLDER_ARCHIVE.copy(isAutomatic = true),
                        draftsSpecialFolderOption = SPECIAL_FOLDER_DRAFTS.copy(isAutomatic = true),
                        sentSpecialFolderOption = SPECIAL_FOLDER_SENT.copy(isAutomatic = true),
                        spamSpecialFolderOption = SPECIAL_FOLDER_SPAM.copy(isAutomatic = true),
                        trashSpecialFolderOption = SPECIAL_FOLDER_TRASH.copy(isAutomatic = true),
                    ),
                ),
            )
        }

    @Test
    fun `should load folders and validate unsuccessful when LoadSpecialFolders event received`() = runTest {
        val accountStateRepository = InMemoryAccountStateRepository()
        val initialState = State(
            isLoading = true,
        )
        val testSubject = createTestSubject(
            formUiModel = FakeSpecialFoldersFormUiModel(
                isValid = false,
            ),
            accountStateRepository = accountStateRepository,
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

        testSubject.event(Event.LoadSpecialFolderOptions)

        val populatedState = initialState.copy(
            isLoading = true,
            isSuccess = false,
            formState = FormState(
                archiveSpecialFolderOptions = SpecialFolderOptions.archiveSpecialFolderOptions,
                draftsSpecialFolderOptions = SpecialFolderOptions.draftsSpecialFolderOptions,
                sentSpecialFolderOptions = SpecialFolderOptions.sentSpecialFolderOptions,
                spamSpecialFolderOptions = SpecialFolderOptions.spamSpecialFolderOptions,
                trashSpecialFolderOptions = SpecialFolderOptions.trashSpecialFolderOptions,

                selectedArchiveSpecialFolderOption = SPECIAL_FOLDER_ARCHIVE.copy(isAutomatic = true),
                selectedDraftsSpecialFolderOption = SPECIAL_FOLDER_DRAFTS.copy(isAutomatic = true),
                selectedSentSpecialFolderOption = SPECIAL_FOLDER_SENT.copy(isAutomatic = true),
                selectedSpamSpecialFolderOption = SPECIAL_FOLDER_SPAM.copy(isAutomatic = true),
                selectedTrashSpecialFolderOption = SPECIAL_FOLDER_TRASH.copy(isAutomatic = true),
            ),
        )

        assertThat(turbines.awaitStateItem()).isEqualTo(populatedState)

        val validatingState = populatedState.copy(
            isLoading = false,
            isSuccess = false,
        )
        turbines.assertThatAndStateTurbineConsumed {
            isEqualTo(validatingState)
        }

        turbines.effectTurbine.ensureAllEventsConsumed()

        assertThat(accountStateRepository.getState()).isEqualTo(AccountState())
    }

    @Test
    fun `should change to error state when LoadSpecialFolders fails with missing incoming server settings`() = runTest {
        val initialState = State(
            isLoading = true,
        )
        val testSubject = createTestSubject(
            formUiModel = FakeSpecialFoldersFormUiModel(
                isValid = false,
            ),
            getSpecialFolderOptions = {
                throw IllegalStateException("No incoming server settings available")
            },
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

        testSubject.event(Event.LoadSpecialFolderOptions)

        turbines.assertThatAndStateTurbineConsumed {
            isEqualTo(
                State(
                    isLoading = false,
                    isSuccess = false,
                    error = SpecialFoldersContract.Failure.MissingIncomingServerSettings(
                        "No incoming server settings available",
                    ),
                ),
            )
        }
    }

    @Test
    fun `should change to error state when LoadSpecialFolders fails with loading folder failure`() = runTest {
        val initialState = State(
            isLoading = true,
        )
        val testSubject = createTestSubject(
            formUiModel = FakeSpecialFoldersFormUiModel(
                isValid = false,
            ),
            getSpecialFolderOptions = {
                throw FolderFetcherException(IllegalStateException("Failed to load folders"))
            },
            initialState = initialState,
        )
        val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

        testSubject.event(Event.LoadSpecialFolderOptions)

        turbines.assertThatAndStateTurbineConsumed {
            isEqualTo(
                State(
                    isLoading = false,
                    isSuccess = false,
                    error = SpecialFoldersContract.Failure.LoadFoldersFailed(
                        "Failed to load folders",
                    ),
                ),
            )
        }
    }

    @Test
    fun `should delegate form events to form view model`() = runTest {
        val formUiModel = FakeSpecialFoldersFormUiModel()
        val testSubject = createTestSubject(
            formUiModel = formUiModel,
        )

        testSubject.event(FormEvent.ArchiveFolderChanged(SPECIAL_FOLDER_ARCHIVE))
        testSubject.event(FormEvent.DraftsFolderChanged(SPECIAL_FOLDER_DRAFTS))
        testSubject.event(FormEvent.SentFolderChanged(SPECIAL_FOLDER_SENT))
        testSubject.event(FormEvent.SpamFolderChanged(SPECIAL_FOLDER_SPAM))
        testSubject.event(FormEvent.TrashFolderChanged(SPECIAL_FOLDER_TRASH))

        assertThat(formUiModel.events).containsExactly(
            FormEvent.ArchiveFolderChanged(SPECIAL_FOLDER_ARCHIVE),
            FormEvent.DraftsFolderChanged(SPECIAL_FOLDER_DRAFTS),
            FormEvent.SentFolderChanged(SPECIAL_FOLDER_SENT),
            FormEvent.SpamFolderChanged(SPECIAL_FOLDER_SPAM),
            FormEvent.TrashFolderChanged(SPECIAL_FOLDER_TRASH),
        )
    }

    @Test
    fun `should emit NavigateNext effect when OnNextClicked event received`() = runTest {
        val initialState = State(isSuccess = true)
        val testSubject = createTestSubject(initialState = initialState)
        val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

        testSubject.event(Event.OnNextClicked)

        turbines.assertThatAndEffectTurbineConsumed {
            isEqualTo(Effect.NavigateNext)
        }
    }

    @Test
    fun `should emit NavigateBack effect when OnBackClicked event received`() = runTest {
        val testSubject = createTestSubject()
        val turbines = turbinesWithInitialStateCheck(testSubject, State())

        testSubject.event(Event.OnBackClicked)

        turbines.assertThatAndEffectTurbineConsumed {
            isEqualTo(Effect.NavigateBack)
        }
    }

    @Test
    fun `should show form when OnEditClicked event received`() = runTest {
        val initialState = State(isSuccess = true)
        val testSubject = createTestSubject(initialState = initialState)
        val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

        testSubject.event(Event.OnEditClicked)

        turbines.assertThatAndStateTurbineConsumed {
            isEqualTo(initialState.copy(isSuccess = false))
        }
    }

    @Test
    fun `should show form when OnRetryClicked event received`() = runTest {
        val initialState = State(error = SpecialFoldersContract.Failure.SaveFailed("error"))
        val testSubject = createTestSubject(initialState = initialState)
        val turbines = turbinesWithInitialStateCheck(testSubject, initialState)

        testSubject.event(Event.OnRetryClicked)

        turbines.assertThatAndStateTurbineConsumed {
            isEqualTo(initialState.copy(error = null))
        }
    }

    private companion object {
        fun createTestSubject(
            formUiModel: SpecialFoldersContract.FormUiModel = FakeSpecialFoldersFormUiModel(),
            getSpecialFolderOptions: () -> SpecialFolderOptions = { SpecialFolderOptions },
            accountStateRepository: AccountDomainContract.AccountStateRepository = InMemoryAccountStateRepository(),
            initialState: State = State(),
        ) = SpecialFoldersViewModel(
            formUiModel = formUiModel,
            getSpecialFolderOptions = {
                delay(50)
                getSpecialFolderOptions()
            },
            accountStateRepository = accountStateRepository,
            initialState = initialState,
        )

        val REMOTE_FOLDER = RemoteFolder(FolderServerId("archive"), "archive", FolderType.ARCHIVE)

        val SPECIAL_FOLDER_ARCHIVE = SpecialSpecialFolderOption.Archive(REMOTE_FOLDER)
        val SPECIAL_FOLDER_DRAFTS = SpecialSpecialFolderOption.Drafts(REMOTE_FOLDER)
        val SPECIAL_FOLDER_SENT = SpecialSpecialFolderOption.Sent(REMOTE_FOLDER)
        val SPECIAL_FOLDER_SPAM = SpecialSpecialFolderOption.Spam(REMOTE_FOLDER)
        val SPECIAL_FOLDER_TRASH = SpecialSpecialFolderOption.Trash(REMOTE_FOLDER)

        val SpecialFolderOptions = SpecialFolderOptions(
            archiveSpecialFolderOptions = listOf(
                SPECIAL_FOLDER_ARCHIVE.copy(isAutomatic = true),
                SpecialFolderOption.None(),
                SPECIAL_FOLDER_ARCHIVE,
                SpecialFolderOption.Regular(REMOTE_FOLDER),
            ),
            draftsSpecialFolderOptions = listOf(
                SPECIAL_FOLDER_DRAFTS.copy(isAutomatic = true),
                SpecialFolderOption.None(),
                SPECIAL_FOLDER_DRAFTS,
                SpecialFolderOption.Regular(REMOTE_FOLDER),
            ),
            sentSpecialFolderOptions = listOf(
                SPECIAL_FOLDER_SENT.copy(isAutomatic = true),
                SpecialFolderOption.None(),
                SPECIAL_FOLDER_SENT,
                SpecialFolderOption.Regular(REMOTE_FOLDER),
            ),
            spamSpecialFolderOptions = listOf(
                SPECIAL_FOLDER_SPAM.copy(isAutomatic = true),
                SpecialFolderOption.None(),
                SPECIAL_FOLDER_SPAM,
                SpecialFolderOption.Regular(REMOTE_FOLDER),
            ),
            trashSpecialFolderOptions = listOf(
                SPECIAL_FOLDER_TRASH.copy(isAutomatic = true),
                SpecialFolderOption.None(),
                SPECIAL_FOLDER_TRASH,
                SpecialFolderOption.Regular(REMOTE_FOLDER),
            ),
        )
    }
}
