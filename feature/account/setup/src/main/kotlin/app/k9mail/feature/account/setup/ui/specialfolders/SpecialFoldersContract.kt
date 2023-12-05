package app.k9mail.feature.account.setup.ui.specialfolders

import app.k9mail.core.ui.compose.common.mvi.UnidirectionalViewModel
import app.k9mail.feature.account.common.domain.entity.SpecialFolderOption
import app.k9mail.feature.account.common.ui.loadingerror.LoadingErrorState

interface SpecialFoldersContract {

    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    interface FormUiModel {
        fun event(event: FormEvent, formState: FormState): FormState
        fun validate(formState: FormState): Boolean
    }

    data class State(
        val formState: FormState = FormState(),

        val isSuccess: Boolean = false,
        override val error: Failure? = null,
        override val isLoading: Boolean = true,
    ) : LoadingErrorState<Failure>

    data class FormState(
        val archiveSpecialFolderOptions: List<SpecialFolderOption> = emptyList(),
        val draftsSpecialFolderOptions: List<SpecialFolderOption> = emptyList(),
        val sentSpecialFolderOptions: List<SpecialFolderOption> = emptyList(),
        val spamSpecialFolderOptions: List<SpecialFolderOption> = emptyList(),
        val trashSpecialFolderOptions: List<SpecialFolderOption> = emptyList(),

        val selectedArchiveSpecialFolderOption: SpecialFolderOption? = null,
        val selectedDraftsSpecialFolderOption: SpecialFolderOption? = null,
        val selectedSentSpecialFolderOption: SpecialFolderOption? = null,
        val selectedSpamSpecialFolderOption: SpecialFolderOption? = null,
        val selectedTrashSpecialFolderOption: SpecialFolderOption? = null,
    )

    sealed interface Event {
        data object LoadSpecialFolders : Event
        data object OnEditClicked : Event
        data object OnRetryClicked : Event
        data object OnNextClicked : Event
        data object OnBackClicked : Event
    }

    sealed interface FormEvent : Event {
        data class ArchiveFolderChanged(val specialFolderOption: SpecialFolderOption) : FormEvent
        data class DraftsFolderChanged(val specialFolderOption: SpecialFolderOption) : FormEvent
        data class SentFolderChanged(val specialFolderOption: SpecialFolderOption) : FormEvent
        data class SpamFolderChanged(val specialFolderOption: SpecialFolderOption) : FormEvent
        data class TrashFolderChanged(val specialFolderOption: SpecialFolderOption) : FormEvent
    }

    sealed interface Effect {
        data object NavigateNext : Effect
        data object NavigateBack : Effect
    }

    sealed interface Failure {
        val message: String

        data class MissingIncomingServerSettings(override val message: String) : Failure
        data class LoadFoldersFailed(override val message: String) : Failure
        data class SaveFailed(override val message: String) : Failure
    }
}
