package com.wallet.modules.settings.main

import androidx.lifecycle.viewModelScope
import com.wallet.R
import com.wallet.core.IAccountManager
import com.wallet.core.IBackupManager
import com.wallet.core.ITermsManager
import com.wallet.core.ViewModelUiState
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.managers.LanguageManager
import com.wallet.core.providers.AppConfigProvider
import com.wallet.core.providers.Translator
import com.wallet.modules.settings.main.MainSettingsModule.CounterType
import com.wallet.modules.walletconnect.WCManager
import com.wallet.modules.walletconnect.WCSessionManager
import com.core.IPinComponent
import com.core.ISystemInfoManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx2.asFlow

class MainSettingsViewModel(
    private val backupManager: IBackupManager,
    private val systemInfoManager: ISystemInfoManager,
    private val termsManager: ITermsManager,
    private val pinComponent: IPinComponent,
    private val wcSessionManager: WCSessionManager,
    private val wcManager: WCManager,
    private val accountManager: IAccountManager,
    private val appConfigProvider: AppConfigProvider,
    private val languageManager: LanguageManager,
    private val currencyManager: CurrencyManager,
) : ViewModelUiState<MainSettingUiState>() {

    val appVersion: String
        get() {
            var appVersion = systemInfoManager.appVersion
            if (Translator.getString(R.string.is_release) == "false") {
                appVersion += " (${appConfigProvider.appBuild})"
            }

            return appVersion
        }

    val companyWebPage = appConfigProvider.companyWebPageLink

    val walletConnectSupportState: WCManager.SupportState
        get() = wcManager.getWalletConnectSupportState()

    private val currentLanguageDisplayName: String
        get() = languageManager.currentLanguageName

    private val baseCurrencyCode: String
        get() = currencyManager.baseCurrency.code

    private val appWebPageLink = appConfigProvider.appWebPageLink
    private val hasNonStandardAccount: Boolean
        get() = accountManager.hasNonStandardAccount

    private val allBackedUp: Boolean
        get() = backupManager.allBackedUp

    private val walletConnectSessionCount: Int
        get() = wcSessionManager.sessions.count()

    private val isPinSet: Boolean
        get() = pinComponent.isPinSet


    private var wcCounterType: CounterType? = null
    private var wcSessionsCount = walletConnectSessionCount
    private var wcPendingRequestCount = 0

    init {
        viewModelScope.launch {
            backupManager.allBackedUpFlowable.asFlow().collect {
                emitState()
            }
        }
        viewModelScope.launch {
            wcSessionManager.sessionsFlow.collect {
                wcSessionsCount = walletConnectSessionCount
                syncCounter()
            }
        }
        viewModelScope.launch {
            pinComponent.pinSetFlowable.asFlow().collect {
                emitState()
            }
        }

        viewModelScope.launch {
            termsManager.termsAcceptedSignalFlow.collect {
                emitState()
            }
        }

        viewModelScope.launch {
            wcSessionManager.pendingRequestCountFlow.collect {
                wcPendingRequestCount = it
                syncCounter()
            }
        }
        viewModelScope.launch {
            currencyManager.baseCurrencyUpdatedSignal.asFlow().collect {
                emitState()
            }
        }
        syncCounter()
    }

    override fun createState(): MainSettingUiState {
        return MainSettingUiState(
            currentLanguage = currentLanguageDisplayName,
            baseCurrencyCode = baseCurrencyCode,
            appWebPageLink = appWebPageLink,
            hasNonStandardAccount = hasNonStandardAccount,
            allBackedUp = allBackedUp,
            pendingRequestCount = wcPendingRequestCount,
            walletConnectSessionCount = wcSessionsCount,
            manageWalletShowAlert = !allBackedUp || hasNonStandardAccount,
            securityCenterShowAlert = !isPinSet,
            aboutAppShowAlert = !termsManager.allTermsAccepted,
            wcCounterType = wcCounterType
        )
    }

    private fun syncCounter() {
        if (wcPendingRequestCount > 0) {
            wcCounterType = CounterType.PendingRequestCounter(wcPendingRequestCount)
        } else if (wcSessionsCount > 0) {
            wcCounterType = CounterType.SessionCounter(wcSessionsCount)
        } else {
            wcCounterType = null
        }
        emitState()
    }
}

data class MainSettingUiState(
    val currentLanguage: String,
    val baseCurrencyCode: String,
    val appWebPageLink: String,
    val hasNonStandardAccount: Boolean,
    val allBackedUp: Boolean,
    val pendingRequestCount: Int,
    val walletConnectSessionCount: Int,
    val manageWalletShowAlert: Boolean,
    val securityCenterShowAlert: Boolean,
    val aboutAppShowAlert: Boolean,
    val wcCounterType: CounterType?
)