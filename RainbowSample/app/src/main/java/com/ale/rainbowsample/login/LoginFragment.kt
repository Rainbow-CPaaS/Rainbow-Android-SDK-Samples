package com.ale.rainbowsample.login

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.ale.rainbowsample.R
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.databinding.FragmentLoginBinding
import com.ale.rainbowsample.utils.getThemeColor
import com.ale.rainbowsample.utils.hideKeyboard
import com.ale.rainbowsample.utils.showSnackBar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorPrimary)
        val secondColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorPrimaryContainer)

        binding.animationView.addValueCallback(KeyPath("Rectangle_1", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(firstColor, PorterDuff.Mode.SRC_ATOP) }
        binding.animationView.addValueCallback(KeyPath("Rectangle_2", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(secondColor, PorterDuff.Mode.SRC_ATOP) }
        binding.animationView.addValueCallback(KeyPath("Rectangle_3", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(firstColor, PorterDuff.Mode.SRC_ATOP) }
        binding.animationView.addValueCallback(KeyPath("Rectangle_4", "Rectangle 1", "Fill 1"), LottieProperty.COLOR_FILTER) { PorterDuffColorFilter(secondColor, PorterDuff.Mode.SRC_ATOP) }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(EMAIL_KEY)?.observe(viewLifecycleOwner) { result ->
            binding.username.setText(result)
            binding.password.setText("")
        }

        collectLifecycleFlow(loginViewModel.uiState) { uiState ->
            if (uiState.isUserLoggedIn) {
                navigateToHomeActivity()
                return@collectLifecycleFlow
            }

            uiState.userMessage?.let { message ->
                showSnackBar(message.asString(requireContext())) {
                    loginViewModel.userMessageShown()
                }
            }

            if (uiState.isLoading) {
                binding.animationView.playAnimation()
            } else {
                binding.animationView.pauseAnimation()
            }

            binding.animationView.isVisible = uiState.isLoading
            binding.btnSignIn.isEnabled = !uiState.isLoading
            binding.username.isEnabled = !uiState.isLoading
            binding.password.isEnabled = !uiState.isLoading

            uiState.login?.let { login ->
                binding.username.setText(login)
            }

            uiState.password?.let { password ->
                binding.password.setText(password)
            }
        }

        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.btnSignIn.setOnClickListener {
            hideKeyboard()
            loginViewModel.signIn(binding.username.text.toString(), binding.password.text.toString())
        }
    }

    private fun navigateToHomeActivity() {
        findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
        requireActivity().finish()
    }

    companion object {
        const val EMAIL_KEY = "EMAIL_KEY"
    }
}