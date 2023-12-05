package app.k9mail.feature.account.setup.domain.usecase

import app.k9mail.feature.account.common.domain.entity.Account
import app.k9mail.feature.account.common.domain.entity.AccountOptions
import app.k9mail.feature.account.common.domain.entity.MailConnectionSecurity
import app.k9mail.feature.account.common.domain.entity.SpecialFolderSettings
import app.k9mail.feature.account.common.domain.entity.SpecialSpecialFolderOption
import app.k9mail.feature.account.setup.AccountSetupExternalContract.AccountCreator.AccountCreatorResult
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fsck.k9.mail.AuthType
import com.fsck.k9.mail.FolderType
import com.fsck.k9.mail.ServerSettings
import com.fsck.k9.mail.folders.FolderServerId
import com.fsck.k9.mail.folders.RemoteFolder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreateAccountTest {

    @Test
    fun `should successfully create account`() = runTest {
        var recordedAccount: Account? = null
        val createAccount = CreateAccount(
            accountCreator = { account ->
                recordedAccount = account
                AccountCreatorResult.Success(accountUuid = "uuid")
            },
            uuidGenerator = { "uuid" },
        )

        val result = createAccount.execute(
            EMAIL_ADDRESS,
            INCOMING_SETTINGS,
            OUTGOING_SETTINGS,
            AUTHORIZATION_STATE,
            SPECIAL_FOLDER_SETTINGS,
            OPTIONS,
        )

        assertThat(result).isEqualTo(AccountCreatorResult.Success("uuid"))
        assertThat(recordedAccount).isEqualTo(
            Account(
                uuid = "uuid",
                emailAddress = EMAIL_ADDRESS,
                incomingServerSettings = INCOMING_SETTINGS,
                outgoingServerSettings = OUTGOING_SETTINGS,
                authorizationState = AUTHORIZATION_STATE,
                specialFolderSettings = SPECIAL_FOLDER_SETTINGS,
                options = OPTIONS,
            ),
        )
    }

    private companion object {
        const val EMAIL_ADDRESS = "user@example.com"

        val INCOMING_SETTINGS = ServerSettings(
            type = "imap",
            host = "imap.example.com",
            port = 993,
            connectionSecurity = MailConnectionSecurity.SSL_TLS_REQUIRED,
            authenticationType = AuthType.PLAIN,
            username = "user",
            password = "password",
            clientCertificateAlias = null,
        )

        val OUTGOING_SETTINGS = ServerSettings(
            type = "smtp",
            host = "smtp.example.com",
            port = 465,
            connectionSecurity = MailConnectionSecurity.SSL_TLS_REQUIRED,
            authenticationType = AuthType.PLAIN,
            username = "user",
            password = "password",
            clientCertificateAlias = null,
        )

        const val AUTHORIZATION_STATE = "authorization state"

        val SPECIAL_FOLDER_SETTINGS = SpecialFolderSettings(
            archiveSpecialFolderOption = SpecialSpecialFolderOption.Archive(
                RemoteFolder(FolderServerId("archive"), "archive", FolderType.ARCHIVE),
            ),
            draftsSpecialFolderOption = SpecialSpecialFolderOption.Drafts(
                RemoteFolder(FolderServerId("drafts"), "drafts", FolderType.DRAFTS),
            ),
            sentSpecialFolderOption = SpecialSpecialFolderOption.Sent(
                RemoteFolder(FolderServerId("sent"), "sent", FolderType.SENT),
            ),
            spamSpecialFolderOption = SpecialSpecialFolderOption.Spam(
                RemoteFolder(FolderServerId("spam"), "spam", FolderType.SPAM),
            ),
            trashSpecialFolderOption = SpecialSpecialFolderOption.Trash(
                RemoteFolder(FolderServerId("trash"), "trash", FolderType.TRASH),
            ),
        )

        val OPTIONS = AccountOptions(
            accountName = "accountName",
            displayName = "displayName",
            emailSignature = null,
            checkFrequencyInMinutes = 15,
            messageDisplayCount = 25,
            showNotification = true,
        )
    }
}
