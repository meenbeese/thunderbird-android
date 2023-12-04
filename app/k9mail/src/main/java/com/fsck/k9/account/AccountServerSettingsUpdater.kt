package com.fsck.k9.account

import app.k9mail.core.common.mail.Protocols
import app.k9mail.feature.account.common.domain.entity.AuthorizationState
import app.k9mail.feature.account.edit.AccountEditExternalContract
import app.k9mail.feature.account.edit.AccountEditExternalContract.AccountUpdaterFailure
import app.k9mail.feature.account.edit.AccountEditExternalContract.AccountUpdaterResult
import com.fsck.k9.Account
import com.fsck.k9.LocalKeyStoreManager
import com.fsck.k9.logging.Timber
import com.fsck.k9.mail.MailServerDirection
import com.fsck.k9.mail.ServerSettings
import com.fsck.k9.mail.store.imap.ImapStoreSettings
import com.fsck.k9.mail.store.imap.ImapStoreSettings.autoDetectNamespace
import com.fsck.k9.mail.store.imap.ImapStoreSettings.isSendClientId
import com.fsck.k9.mail.store.imap.ImapStoreSettings.isUseCompression
import com.fsck.k9.mail.store.imap.ImapStoreSettings.pathPrefix
import com.fsck.k9.preferences.AccountManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountServerSettingsUpdater(
    private val accountManager: AccountManager,
    private val localKeyStoreManager: LocalKeyStoreManager,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AccountEditExternalContract.AccountServerSettingsUpdater {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun updateServerSettings(
        accountUuid: String,
        isIncoming: Boolean,
        serverSettings: ServerSettings,
        authorizationState: AuthorizationState?,
    ): AccountUpdaterResult {
        return try {
            withContext(coroutineDispatcher) {
                updateSettings(accountUuid, isIncoming, serverSettings, authorizationState)
            }
        } catch (error: Exception) {
            Timber.e(error, "Error while updating account server settings with UUID %s", accountUuid)

            AccountUpdaterResult.Failure(AccountUpdaterFailure.UnknownError(error))
        }
    }

    private fun updateSettings(
        accountUuid: String,
        isIncoming: Boolean,
        serverSettings: ServerSettings,
        authorizationState: AuthorizationState?,
    ): AccountUpdaterResult {
        val account = accountManager.getAccount(accountUuid = accountUuid) ?: return AccountUpdaterResult.Failure(
            AccountUpdaterFailure.AccountNotFound(accountUuid),
        )

        deleteCertificate(account, isIncoming, serverSettings)

        if (isIncoming) {
            if (serverSettings.type == Protocols.IMAP) {
                account.useCompression = serverSettings.isUseCompression
                account.isSendClientIdEnabled = serverSettings.isSendClientId
                account.incomingServerSettings = serverSettings.copy(
                    extra = ImapStoreSettings.createExtra(
                        autoDetectNamespace = serverSettings.autoDetectNamespace,
                        pathPrefix = serverSettings.pathPrefix,
                    ),
                )
            } else {
                account.incomingServerSettings = serverSettings
            }
        } else {
            account.outgoingServerSettings = serverSettings
        }

        account.oAuthState = authorizationState?.state

        accountManager.saveAccount(account)

        if (serverSettings.clientCertificateAlias != null) {
            saveCertificate(account, isIncoming)
        }

        return AccountUpdaterResult.Success(accountUuid)
    }

    private fun deleteCertificate(
        account: Account,
        isIncoming: Boolean,
        serverSettings: ServerSettings,
    ) {
        val oldServerSettings = if (isIncoming) {
            account.incomingServerSettings
        } else {
            account.outgoingServerSettings
        }

        if (oldServerSettings.host != serverSettings.host || oldServerSettings.port != serverSettings.port) {
            localKeyStoreManager.deleteCertificate(
                account,
                serverSettings.host!!,
                serverSettings.port,
                if (isIncoming) MailServerDirection.INCOMING else MailServerDirection.OUTGOING,
            )
        }
    }

    private fun saveCertificate(
        account: Account,
        isIncoming: Boolean,
    ) {
        localKeyStoreManager.addCertificate(
            account,
            if (isIncoming) MailServerDirection.INCOMING else MailServerDirection.OUTGOING,
        )
    }
}
