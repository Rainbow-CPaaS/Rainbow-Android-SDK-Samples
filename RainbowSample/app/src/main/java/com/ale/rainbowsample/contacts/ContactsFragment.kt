package com.ale.rainbowsample.contacts

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.ale.rainbowsample.R
import com.ale.rainbowsample.activities.HomeActivity
import com.ale.rainbowsample.activities.SearchCallback
import com.ale.rainbowsample.components.SearchToolbar
import com.ale.rainbowsample.databinding.FragmentContactsBinding
import com.ale.rainbowsample.search.SearchViewModel
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.getThemeColor
import com.ale.rainbowsample.utils.showSnackBar
import com.ale.rainbowsample.utils.viewLifecycle
import com.ale.rainbowsdk.RainbowSdk
import com.ale.rainbowx.conferencerecyclerview.removeAllItemDecorations
import com.google.android.material.divider.MaterialDividerItemDecoration

class ContactsFragment : Fragment() {

    private var binding: FragmentContactsBinding by viewLifecycle {
        binding.contactsList.adapter = null // To call onViewDetachedFromWindow
    }
    private val contactsViewModel: ContactsViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var contactsAdapter : ContactsAdapter
    private lateinit var searchContactsAdapter : SearchContactsAdapter

    private var currentAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        (activity as? HomeActivity)?.setAppBarScrollId(binding.contactsList)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorPrimary)
        val secondColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorPrimaryContainer)

        binding.animationView.addValueCallback(KeyPath("Rectangle_1", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(firstColor, PorterDuff.Mode.SRC_ATOP) }
        binding.animationView.addValueCallback(KeyPath("Rectangle_2", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(secondColor, PorterDuff.Mode.SRC_ATOP) }
        binding.animationView.addValueCallback(KeyPath("Rectangle_3", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(firstColor, PorterDuff.Mode.SRC_ATOP) }
        binding.animationView.addValueCallback(KeyPath("Rectangle_4", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(secondColor, PorterDuff.Mode.SRC_ATOP) }

        initializeContactsAdapter()
        initializeSearchContactsAdapter()
        initializeSearch()

        currentAdapter = contactsAdapter

        collectLifecycleFlow(contactsViewModel.uiState) { uiState ->
            contactsAdapter.submitList(uiState.contacts)
        }

        collectLifecycleFlow(searchViewModel.searchLoading) {
            showOrHideNoResults()
        }

        collectLifecycleFlow(searchViewModel.searchResult) {
            searchContactsAdapter.submitList(it)
        }

        collectLifecycleFlow(contactsViewModel.eventSharedFlow) { event ->
            when (event) {
                ContactsViewModel.Event.INVITATION_SEND_SUCCESS -> showSnackBar(getString(R.string.send_invitation_success))
                ContactsViewModel.Event.INVITATION_SEND_FAILURE -> showSnackBar(getString(R.string.send_invitation_failure))
                ContactsViewModel.Event.REMOVE_SUCCESS -> showSnackBar(getString(R.string.remove_contact_success))
                ContactsViewModel.Event.REMOVE_FAILURE -> showSnackBar(getString(R.string.remove_contact_failure))
                ContactsViewModel.Event.PENDING_INVITATION -> showSnackBar(getString(R.string.pending_invitation))
            }
        }


        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.contacts_list_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.new_group_item) {
                    findNavController().navigate(R.id.action_navigation_contacts_to_navigation_create_group)
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        requireActivity().onBackPressedDispatcher.addCallback(getViewLifecycleOwner(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!closeSearchIfNeeded()) {
                    NavHostFragment.findNavController(this@ContactsFragment).popBackStack()
                }
            }
        })
    }

    private fun initializeSearchContactsAdapter() {
        searchContactsAdapter = SearchContactsAdapter(
            onInviteUser = { contact -> contactsViewModel.inviteContactToJoinRoster(contact) }
        )
    }

    private fun initializeContactsAdapter() {
        contactsAdapter = ContactsAdapter(
            onRemoveContact = { contact -> contactsViewModel.removeContactFromRoster(contact) }
        )
        setAdapter(contactsAdapter)
    }

    private fun initializeSearch() {
        val searchCallback =  (requireActivity() as? SearchCallback) ?: return
        searchCallback.getSearchButton().isVisible = true
        searchCallback.getSearchButton().setOnClickListener {
            searchCallback.onSearchOpened()
            searchCallback.getSearchToolbar().listener = object : SearchToolbar.Listener {
                override fun onSearchTextChange(text: String) {
                    val search = text.trim()

                    searchViewModel.search(search)

                    if (search.isNotEmpty()) {
                        if (currentAdapter != searchContactsAdapter) {
                            setAdapter(searchContactsAdapter)
                        }
                    } else {
                        if (currentAdapter != contactsAdapter) {
                            setAdapter(contactsAdapter)
                            showOrHideNoResults()
                        }
                    }
                }

                override fun onSearchClosed() {
                    setAdapter(contactsAdapter)
                    searchCallback.onSearchClosed()
                }
            }
        }
    }

    private fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        if (currentAdapter === adapter) return

        currentAdapter = adapter
        binding.contactsList.adapter = adapter

        if (currentAdapter === contactsAdapter) {
            binding.contactsList.addItemDecoration(MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
                dividerThickness = 1
                isLastItemDecorated = false
            })
        } else {
            binding.contactsList.removeAllItemDecorations()
        }
    }

    private fun showOrHideNoResults() {
        if (currentAdapter === contactsAdapter) {
            binding.noResultLayout.isVisible = false
            binding.animationView.pauseAnimation()
            return
        }

        if (searchViewModel.searchResult.value.isNotEmpty() || searchViewModel.searchLoading.value) {
            binding.noResultLayout.isVisible = false
            binding.animationView.pauseAnimation()
            return
        }

        binding.noResultLayout.isVisible = true
        binding.animationView.playAnimation()
    }

    private fun closeSearchIfNeeded() : Boolean {
        if (isSearchOpen()) {
            (requireActivity() as SearchCallback).getSearchToolbar().collapse()
            (requireActivity() as SearchCallback).onSearchClosed()
            return true
        }

        return false
    }

    private fun isSearchOpen() : Boolean {
        return (requireActivity() as SearchCallback).getSearchToolbar().isVisible || currentAdapter == searchContactsAdapter
    }
}