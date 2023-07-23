package com.firelord.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import com.firelord.login.databinding.OtpBottomSheetBinding
import com.firelord.login.viewmodel.LoginViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpBottomSheetFragment(private val navController: NavController): BottomSheetDialogFragment() {
    private lateinit var otpBottomSheetBinding: OtpBottomSheetBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel: LoginViewModel
    private lateinit var number : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        otpBottomSheetBinding = OtpBottomSheetBinding.inflate(inflater)
        return otpBottomSheetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        auth = FirebaseAuth.getInstance()
        otpBottomSheetBinding.phoneProgressBar.visibility = View.INVISIBLE

        otpBottomSheetBinding.sendOTPBtn.setOnClickListener {
            number = otpBottomSheetBinding.phoneEditTextNumber.text.trim().toString()
            if (number.isNotEmpty()){
                if (number.length == 10){
                    number = "+91$number"
                    otpBottomSheetBinding.phoneProgressBar.visibility = View.VISIBLE
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity())          // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }else{
                    Toast.makeText(requireContext() , "Please Enter correct Number" , Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext() , "Please Enter Number" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(requireContext() , "Authenticate Successfully" , Toast.LENGTH_SHORT).show()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    otpBottomSheetBinding.phoneProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Login failed due to ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                otpBottomSheetBinding.phoneProgressBar.visibility = View.INVISIBLE
            }
            .addOnFailureListener {e->
                otpBottomSheetBinding.phoneProgressBar.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
                dismiss()
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(requireContext(),e.message.toString(),Toast.LENGTH_SHORT).show()
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
            // Show a message and update the UI
            Toast.makeText(requireContext(),e.message.toString(),Toast.LENGTH_SHORT).show()
            otpBottomSheetBinding.phoneProgressBar.visibility = View.INVISIBLE
            dismiss()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            viewModel.OTP.value = verificationId
            viewModel.resendToken.value = token
            viewModel.number.value = number
            navController.navigate(R.id.action_loginFragment_to_otpFragment)
            otpBottomSheetBinding.phoneProgressBar.visibility = View.INVISIBLE
            dismiss()
        }
    }
}