package com.ale.rainbowsample.signup

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.FragmentSignUpBinding
import com.ale.rainbowsample.login.LoginFragment
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.getThemeColor
import com.ale.rainbowsample.utils.hideKeyboard
import com.ale.rainbowsample.utils.showSnackBar

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLifecycleFlow(signUpViewModel.uiState) { uiState ->
            uiState.userMessage?.let { message ->
                showSnackBar(message.asString(requireContext())) {
                    signUpViewModel.userMessageShown()
                }
            }

            if (uiState.isLoading) {
                binding.animationView.playAnimation()
            } else {
                binding.animationView.pauseAnimation()
            }

            binding.animationView.isVisible = uiState.isLoading
            binding.btnEmail.isEnabled = !uiState.isLoading
            binding.username.isEnabled = !uiState.isLoading

            showCurrentRegistrationStep(uiState.registrationState)
        }

        binding.btnEmail.setOnClickListener {
            hideKeyboard()
            signUpViewModel.registerWithEmail(binding.username.text.toString(), binding.password.text.toString())
        }

        binding.btnCode.setOnClickListener {
            hideKeyboard()
            signUpViewModel.registerWithCode(binding.code.text.toString())
        }

        binding.btnSignUp.setOnClickListener {
            hideKeyboard()
            signUpViewModel.register(binding.firstName.text.toString(), binding.lastName.text.toString())
        }

        binding.btnSignIn.setOnClickListener {
           findNavController().popBackStack()
        }
    }

    private fun showCurrentRegistrationStep(registrationState: RegistrationState) {
        if (registrationState == RegistrationState.REGISTRATION_SUCCEED) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(LoginFragment.EMAIL_KEY, signUpViewModel.email)
            findNavController().popBackStack()
            return
        }

        binding.signUpEmail.isVisible = registrationState == RegistrationState.EMAIL
        binding.signUpCode.isVisible = registrationState == RegistrationState.CODE
        binding.signUpProfile.isVisible = registrationState == RegistrationState.PROFILE
    }

    companion object {
        @JvmStatic
        fun newInstance() = SignUpFragment()
    }
}