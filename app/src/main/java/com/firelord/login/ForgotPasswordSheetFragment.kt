package com.firelord.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firelord.login.databinding.ForgotBottomSheetBinding
import com.firelord.login.util.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordSheetFragment: BottomSheetDialogFragment() {
    private lateinit var forgotBottomSheetBinding: ForgotBottomSheetBinding
    private lateinit var auth : FirebaseAuth
    private var email = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        forgotBottomSheetBinding = ForgotBottomSheetBinding.inflate(inflater)
        return forgotBottomSheetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        forgotBottomSheetBinding.button.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        email = forgotBottomSheetBinding.textInsideEdit.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email format
            forgotBottomSheetBinding.textInsideEdit.error = "Invalid email format"
        }
        else {
            // data is validated, begin login
            forgetPassword()
        }
    }
    private fun forgetPassword(){
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(requireContext(),"Check your Email $email",Toast.LENGTH_SHORT).show()
                    dismiss()
                }else{
                    Toast.makeText(requireContext(),"Error: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.d(Constants.TAG, "FirebaseReset: Reset failed due to ${e.message}")
                Toast.makeText(requireContext(), "Reset failed due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}