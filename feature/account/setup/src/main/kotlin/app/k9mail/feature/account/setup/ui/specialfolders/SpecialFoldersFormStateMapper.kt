package app.k9mail.feature.account.setup.ui.specialfolders

import app.k9mail.feature.account.common.domain.entity.SpecialFolderOption
import app.k9mail.feature.account.common.domain.entity.SpecialFolderOptions
import app.k9mail.feature.account.common.domain.entity.SpecialSpecialFolderOption
import app.k9mail.feature.account.setup.ui.specialfolders.SpecialFoldersContract.FormState

fun SpecialFolderOptions.toFormState(): FormState {
    return FormState(
        archiveSpecialFolderOptions = archiveSpecialFolderOptions,
        draftsSpecialFolderOptions = draftsSpecialFolderOptions,
        sentSpecialFolderOptions = sentSpecialFolderOptions,
        spamSpecialFolderOptions = spamSpecialFolderOptions,
        trashSpecialFolderOptions = trashSpecialFolderOptions,

        selectedArchiveSpecialFolderOption = archiveSpecialFolderOptions.mapToDefaultFolder(),
        selectedDraftsSpecialFolderOption = draftsSpecialFolderOptions.mapToDefaultFolder(),
        selectedSentSpecialFolderOption = sentSpecialFolderOptions.mapToDefaultFolder(),
        selectedSpamSpecialFolderOption = spamSpecialFolderOptions.mapToDefaultFolder(),
        selectedTrashSpecialFolderOption = trashSpecialFolderOptions.mapToDefaultFolder(),
    )
}

private fun List<SpecialFolderOption>.mapToDefaultFolder(): SpecialFolderOption? {
    return firstOrNull {
        (it is SpecialSpecialFolderOption && it.isAutomatic)
    }
}
