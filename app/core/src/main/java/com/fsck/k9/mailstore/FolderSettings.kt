package com.fsck.k9.mailstore

data class FolderSettings(
    val visibleLimit: Int,
    val integrate: Boolean,
    val isHidden: Boolean,
    val inTopGroup: Boolean,
    val isAutoSyncViaPollEnabled: Boolean,
    val isAutoSyncViaPushEnabled: Boolean,
    val isNotificationEnabled: Boolean,
)
