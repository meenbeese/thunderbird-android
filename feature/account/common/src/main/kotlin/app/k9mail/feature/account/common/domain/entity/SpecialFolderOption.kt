package app.k9mail.feature.account.common.domain.entity

import com.fsck.k9.mail.folders.RemoteFolder

sealed interface SpecialFolderOption {
    data class None(
        val isAutomatic: Boolean = false,
    ) : SpecialFolderOption

    data class Regular(
        val remoteFolder: RemoteFolder,
    ) : SpecialFolderOption
}

sealed interface SpecialSpecialFolderOption : SpecialFolderOption {
    val remoteFolder: RemoteFolder
    val isAutomatic: Boolean get() = false

    data class Inbox(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption

    data class Outbox(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption

    data class Archive(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption

    data class Drafts(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption

    data class Sent(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption

    data class Spam(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption

    data class Trash(
        override val remoteFolder: RemoteFolder,
        override val isAutomatic: Boolean = false,
    ) : SpecialSpecialFolderOption
}
