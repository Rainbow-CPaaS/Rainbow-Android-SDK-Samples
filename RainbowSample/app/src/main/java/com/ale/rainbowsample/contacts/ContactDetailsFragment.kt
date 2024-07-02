package com.ale.rainbowsample.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.rainbowsdk.RainbowSdk

class ContactDetailsFragment : Fragment() {

    private var contact: IRainbowContact? = null
    private val contactListener = object : IRainbowContact.IContactListener {
        override fun contactUpdated(updatedContact: IRainbowContact) {
            // Handle update of the contact
        }

        override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
            // Handle presence change of the contact
        }

        override fun onCompanyChanged(companyId: String?) {
            // Optional callback to be notified when the company of the contact change
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        contact = RainbowSdk().contacts().getContactFromId("contactID")

        if (contact == null) {

        }
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        contact?.registerChangeListener(contactListener)
    }

    override fun onPause() {
        super.onPause()
        contact?.unregisterChangeListener(contactListener)
    }

    private fun fetchContactInformation() {

    }


}