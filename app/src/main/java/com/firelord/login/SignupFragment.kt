package com.firelord.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.firelord.login.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {
    private lateinit var signupBinding: FragmentSignupBinding
    // Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        signupBinding = FragmentSignupBinding.inflate(inflater)
        return signupBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // config progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("creating account in..")
        progressDialog.setCanceledOnTouchOutside(false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        // handle click, begin sign up
        signupBinding.btSignUp.setOnClickListener {
            //validate data
            validateData()
        }
    }
    private fun validateData() {
        // get data
        email = signupBinding.etEmailSignUp.text.toString().trim()
        password = signupBinding.etPassSignUp.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email format
            signupBinding.etEmailSignUp.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(password)){
            // no password entered
            signupBinding.etPassSignUp.error = "please enter password"
        }
        else if (password.length <6){
            // password length is less than 6
            signupBinding.etPassSignUp.error = "Password must be atleast 6 char long"
        }
        else {
            // data is validated, continue sign up
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        progressDialog.show()

        // create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                //signup success
                progressDialog.dismiss()
                // get current user
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(requireContext(),"Account created with email $email",Toast.LENGTH_SHORT).show()

                // open MainActivity
                signupBinding.root.findNavController().navigate(R.id.action_signupFragment_to_profileFragment)
            }
            .addOnFailureListener { e->
                //sign up failed
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Sign up Failed due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}