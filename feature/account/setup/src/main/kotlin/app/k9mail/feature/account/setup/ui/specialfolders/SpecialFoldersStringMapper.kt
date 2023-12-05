package app.k9mail.feature.account.setup.ui.specialfolders

import android.content.res.Resources
import app.k9mail.feature.account.common.domain.entity.SpecialFolderOption
import app.k9mail.feature.account.common.domain.entity.SpecialSpecialFolderOption
import app.k9mail.feature.account.setup.R

internal fun SpecialFolderOption?.toResourceString(resources: Resources) = when (this) {
    is SpecialFolderOption.None -> resources.getString(R.string.account_setup_special_folders_folder_none)
    is SpecialFolderOption.Regular -> remoteFolder.displayName
    is SpecialSpecialFolderOption -> {
        if (isAutomatic) {
            resources.getString(R.string.account_setup_special_folders_folder_automatic, remoteFolder.displayName)
        } else {
            remoteFolder.displayName
        }
    }

    null -> resources.getString(R.string.account_setup_special_folders_folder_empty_selection)
}
