package com.ale.rainbowsample.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.ale.infra.rest.listeners.RainbowError
import com.ale.infra.rest.listeners.RainbowListener
import com.ale.infra.rest.user.UserRepository
import com.ale.rainbowsample.activities.LoginActivity
import com.ale.rainbowsample.databinding.FragmentChangePasswordBinding
import com.ale.rainbowsample.utils.LoginFieldsValidator
import com.ale.rainbowsample.utils.showSnackBar
import com.ale.rainbowsdk.RainbowSdk
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePasswordFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val loginValidator = LoginFieldsValidator()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        binding.root.clipToOutline = true
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : BottomSheetDialog(requireContext(), theme) { }

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        dialog.setOnShowListener {
            binding.oldPassword.requestFocusFromTouch()
            val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(binding.oldPassword, InputMethodManager.SHOW_IMPLICIT)
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.oldPassword.doOnTextChanged { text, _, _, _ ->
            binding.changePasswordButton.isEnabled = !text.isNullOrEmpty() && !binding.newPassword.text.isNullOrEmpty()
        }

        binding.newPassword.doOnTextChanged { text, _, _, _ ->
            binding.changePasswordButton.isEnabled = !text.isNullOrEmpty() && !binding.oldPassword.text.isNullOrEmpty()
        }

        binding.changePasswordButton.setOnClickListener {
            val oldPassword = binding.oldPassword.text?.toString() ?: return@setOnClickListener
            val password = binding.newPassword.text?.toString() ?: return@setOnClickListener

            loginValidator.validatePassword(password)?.let { uiText ->
                showSnackBar(uiText.asString(requireContext()))
                return@setOnClickListener
            }

            binding.changePasswordButton.isEnabled = false

            RainbowSdk().user().changeUserPassword(oldPassword, password, object : RainbowListener<Unit, UserRepository.ChangePasswordError> {
                override fun onError(error: RainbowError<UserRepository.ChangePasswordError>) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        val message = when(error.errorType) {
                            UserRepository.ChangePasswordError.PASSWORD_POLICY -> "Your password does not comply with the password policy."
                            UserRepository.ChangePasswordError.SAME_PASSWORD -> "Your new password must be different from your old password."
                            UserRepository.ChangePasswordError.WRONG_ACTUAL_PASSWORD -> "Your current password is not correct"
                            else -> "An error occurred"
                        }

                        showSnackBar(message)
                    }
                }

                override fun onSuccess(data: Unit) {
                    // Restart application from beginning as the token is no longer valid
                    RainbowSdk().connection().signOut(object : RainbowListener<Unit, Unit> {
                        override fun onSuccess(data: Unit) {
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                            this@ChangePasswordFragment.activity?.finish()
                        }
                    })
                }
            })
        }
    }
}