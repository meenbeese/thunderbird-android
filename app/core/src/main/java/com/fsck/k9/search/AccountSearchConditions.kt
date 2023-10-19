package com.fsck.k9.search

import com.fsck.k9.Account
import com.fsck.k9.search.SearchSpecification.Attribute
import com.fsck.k9.search.SearchSpecification.SearchCondition
import com.fsck.k9.search.SearchSpecification.SearchField

/**
 * Modify the supplied [LocalSearch] instance to limit the search to displayable folders.
 */
fun LocalSearch.limitToDisplayableFolders() {
    and(SearchField.FOLDER_HIDDEN, false.toString(), Attribute.EQUALS)
}

/**
 * Modify the supplied [LocalSearch] instance to exclude special folders.
 *
 * Currently the following folders are excluded:
 *  - Trash
 *  - Drafts
 *  - Spam
 *  - Outbox
 *  - Sent
 *
 * The Inbox will always be included even if one of the special folders is configured to point to the Inbox.
 */
fun LocalSearch.excludeSpecialFolders(account: Account) {
    this.excludeSpecialFolder(account.trashFolderId)
    this.excludeSpecialFolder(account.draftsFolderId)
    this.excludeSpecialFolder(account.spamFolderId)
    this.excludeSpecialFolder(account.outboxFolderId)
    this.excludeSpecialFolder(account.sentFolderId)

    account.inboxFolderId?.let { inboxFolderId ->
        or(SearchCondition(SearchField.FOLDER, Attribute.EQUALS, inboxFolderId.toString()))
    }
}

private fun LocalSearch.excludeSpecialFolder(folderId: Long?) {
    if (folderId != null) {
        and(SearchField.FOLDER, folderId.toString(), Attribute.NOT_EQUALS)
    }
}
