package com.wallet.modules.contacts

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.providers.Translator
import com.wallet.modules.address.AddressHandlerFactory
import com.wallet.modules.contacts.model.Contact
import com.wallet.modules.contacts.model.ContactAddress
import com.wallet.modules.contacts.viewmodel.AddressViewModel
import com.wallet.modules.contacts.viewmodel.ContactViewModel
import com.wallet.modules.contacts.viewmodel.ContactsViewModel

object ContactsModule {

    class ContactsViewModelFactory(private val mode: Mode) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsViewModel(App.contactsRepository, mode) as T
        }
    }

    class ContactViewModelFactory(
        private val contact: Contact?,
        private val newAddress: ContactAddress?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactViewModel(App.contactsRepository, contact, newAddress) as T
        }
    }

    class AddressViewModelFactory(
        private val contactUid: String?,
        private val contactAddress: ContactAddress?,
        private val definedAddresses: List<ContactAddress>?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddressViewModel(
                contactUid,
                App.contactsRepository,
                AddressHandlerFactory(App.appConfigProvider.udnApiKey),
                App.marketKit,
                contactAddress,
                definedAddresses
            ) as T
        }
    }

    enum class ContactsAction(@StringRes val title: Int) {
        Restore(R.string.Contacts_Restore),
        Backup(R.string.Contacts_Backup)
    }

    enum class AddAddressAction(@StringRes val title: Int) {
        AddToNewContact(R.string.Contacts_AddAddress_NewContact),
        AddToExistingContact(R.string.Contacts_AddAddress_ExistingContact)
    }

    sealed class ContactValidationException(override val message: String?) : Throwable() {
        object DuplicateContactName : ContactValidationException(Translator.getString(R.string.Contacts_Error_DefinedName))
        class DuplicateAddress(val contact: Contact) :
            ContactValidationException(Translator.getString(R.string.Contacts_Error_DefinedAddress, contact.name))
    }


}
