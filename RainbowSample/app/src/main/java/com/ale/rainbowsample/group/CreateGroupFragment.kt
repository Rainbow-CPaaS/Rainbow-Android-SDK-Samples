package com.ale.rainbowsample.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ale.rainbowsample.R
import com.ale.rainbowsample.activities.SearchCallback
import com.ale.rainbowsample.contacts.ContactsAdapter
import com.ale.rainbowsample.contacts.SelectedContactsAdapter
import com.ale.rainbowsample.databinding.FragmentCreateGroupBinding
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.viewLifecycle
import com.google.android.material.snackbar.Snackbar

class CreateGroupFragment : Fragment() {

    private var binding: FragmentCreateGroupBinding by viewLifecycle {
        binding.recyclerView.adapter = null
    }

    private val viewModel: CreateGroupViewModel by viewModels()
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var selectedContactsAdapter: SelectedContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
        (requireActivity() as? SearchCallback)?.getSearchButton()?.isVisible = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = insets.bottom }
            WindowInsetsCompat.CONSUMED
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAdapters()

        collectLifecycleFlow(viewModel.isButtonEnabled) { isEnabled ->
            binding.buttonValidate.isEnabled = isEnabled
        }

        collectLifecycleFlow(viewModel.contacts) { contacts ->
            contactsAdapter.submitList(contacts)
        }

        collectLifecycleFlow(viewModel.selectedContacts) { selectedContacts ->
            binding.recyclerViewSelected.isVisible = selectedContacts.isNotEmpty()

            if (selectedContacts.isNotEmpty()) {
                val staggeredLayoutManager = binding.recyclerViewSelected.layoutManager as StaggeredGridLayoutManager
                when (selectedContacts.size) {
                    in 1..3 -> staggeredLayoutManager.spanCount = 1
                    else -> staggeredLayoutManager.spanCount = 2
                }
            }
            selectedContactsAdapter.submitList(selectedContacts)
        }

        collectLifecycleFlow(viewModel.apiResult) { result ->
            when {
                result.isSuccess -> findNavController().popBackStack()
                result.isFailure -> Snackbar.make(requireView(), "${result.exceptionOrNull()?.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.editTextName.addTextChangedListener {
            viewModel.updateName(it.toString())
        }

        binding.editTextDescription.addTextChangedListener {
            viewModel.updateDescription(it.toString())
        }

        binding.buttonValidate.setOnClickListener { viewModel.createGroup() }
    }

    private fun initializeAdapters() {
        contactsAdapter = ContactsAdapter(
            onItemClick = viewModel::selectContact,
            displayCallAction = false
        )

        selectedContactsAdapter = SelectedContactsAdapter(
            onItemClick = viewModel::unselectContact
        )

        binding.recyclerView.adapter = contactsAdapter
        binding.recyclerViewSelected.adapter = selectedContactsAdapter
    }
}