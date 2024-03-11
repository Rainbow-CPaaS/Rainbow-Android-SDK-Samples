package com.ale.rainbowsample.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ale.infra.contact.IRainbowContact
import com.ale.rainbow.RBLog
import com.ale.rainbowsample.databinding.FragmentProfileBinding
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsdk.RainbowSdk
import com.ale.util.FileUtil
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val user: IRainbowContact
        get() = RainbowSdk().user().getConnectedUser()

    private val profileViewModel: ProfileViewModel by viewModels()

    private val choosePhotoActivityResult = registerForActivityResult<PickVisualMediaRequest, Uri>(ActivityResultContracts.PickVisualMedia()) { result: Uri? ->
        if (result != null) {
            // Copy content uri to file
            try {
                val destination = File.createTempFile("avatar", ".jpg")

                requireContext().contentResolver.openInputStream(result).use { inputStream ->
                    FileOutputStream(destination).use { outputStream ->
                        val buffer = ByteArray(1024)
                        if (inputStream != null) {
                            var length: Int
                            while (inputStream.read(buffer).also { length = it } > 0) {
                                outputStream.write(buffer, 0, length)
                            }
                        }
                    }
                }

                profileViewModel.uploadAvatar(destination)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userAvatar.displayContact(user)
        binding.userAvatar.displayPresence(user)

        binding.editAvatar.setOnClickListener {
            choosePhotoActivityResult.launch(PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly).build())
        }

        binding.deleteAvatar.setOnClickListener {
            profileViewModel.deleteAvatar()
        }

        binding.changePassword.setOnClickListener {
            val fragment = ChangePasswordFragment()
            fragment.show(childFragmentManager, "change_password")
        }

        collectLifecycleFlow(profileViewModel.uiState) { uiState ->
            uiState.userPresence?.let {
                binding.userAvatar.displayPresence(it)
            }

            binding.userAvatar.displayContact(user)

            if (uiState.isLoading) {
                binding.animationView.playAnimation()
            } else {
                binding.animationView.pauseAnimation()
            }

            binding.editAvatar.isEnabled = !uiState.isLoading
            binding.deleteAvatar.isEnabled = !uiState.isLoading
            binding.animationView.isVisible = uiState.isLoading

            uiState.firstName?.let { binding.firstname.text = it }
            uiState.lastName?.let { binding.lastname.text = it }
            uiState.nickName?.let { binding.nickname.text = it }
            uiState.company?.let { binding.company.text = it }
        }
    }
}