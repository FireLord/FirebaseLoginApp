package com.firelord.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.firelord.login.databinding.FragmentLoginBinding
import com.firelord.login.util.Constants.Companion.RC_SIGN_IN
import com.firelord.login.util.Constants.Companion.TAG
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {
    private lateinit var loginBinding: FragmentLoginBinding
    // Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    // FireBase Auth
    private lateinit var firebaseAuth : FirebaseAuth
    private var email = ""
    private var password = ""

    // Google Auth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        loginBinding = FragmentLoginBinding.inflate(inflater)
        return loginBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // config progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Logging in..")
        progressDialog.setCanceledOnTouchOutside(false)

        // call google
        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(),googleSignInOption)

        // init firebaseauth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // Google Sign in button
        loginBinding.btGoogle.setOnClickListener {
            Log.d(TAG,"onCreate: begin google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        // handle click, open sign up page
        loginBinding.tvNoAccount.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        // handle click, begin login
        loginBinding.btLogin.setOnClickListener{
            //before logging in, validate data
            validateData()
        }
        // handle otp login click
        loginBinding.btPhone.setOnClickListener {
            openBottomSheet()
        }
        loginBinding.tvForgotPass.setOnClickListener {
            openResetBottomSheet()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Request returned from launching the intent
        if (requestCode == RC_SIGN_IN){
            Log.d(TAG,"onActivityResult: Google sign in intent")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign in success
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }
            catch (e:Exception){
                // failed google signin
                Log.d(TAG,"onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG,"firebaseAuthWithGoogleAccount: begin firebase auth with google")
        val credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //login success
                Log.d(TAG,"firebaseAuthWithGoogleAccount: LoggedIn")

                // Get logged in user
                val firebaseUser = firebaseAuth.currentUser

                // get user id
                val uid = firebaseUser!!.uid
                val email = firebaseUser!!.email

                Log.d(TAG,"firebaseAuthWithGoogleAccount: uid: ${uid}")
                Log.d(TAG,"firebaseAuthWithGoogleAccount: email: ${email}")

                //check if user is new or existing
                if (authResult.additionalUserInfo!!.isNewUser){
                    //user is new - account created
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Account created ...\n$email")
                    Toast.makeText(requireContext(),"Account created $email",Toast.LENGTH_SHORT).show()
                } else {
                    // existing user - logged in
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Existing user \n$email")
                    Toast.makeText(requireContext(), "Logged in as $email",Toast.LENGTH_SHORT).show()
                }
                // open MainActivity
                loginBinding.root.findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
            }
            .addOnFailureListener { e ->
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Login failed due to ${e.message}")
                Toast.makeText(requireContext(), "Login failed due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateData() {
        email = loginBinding.etEmail.text.toString().trim()
        password = loginBinding.etPass.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email format
            loginBinding.etEmail.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(password)){
            // no password entered
            loginBinding.etPass.error = "please enter password"
        }
        else {
            // data is validated, begin login
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        // show progress
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                //login success
                progressDialog.dismiss()
                //get user info
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(requireContext(), "Logged in as $email",Toast.LENGTH_SHORT).show()
                // open mainActivity
                loginBinding.root.findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
            }
            .addOnFailureListener { e ->
                // login failed
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser(){
        val  firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
           loginBinding.root.findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        }
    }

    private fun openBottomSheet() {
        val bottomSheetFragment = OtpBottomSheetFragment(findNavController())
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }
    private fun openResetBottomSheet() {
        val bottomSheetFragment = ForgotPasswordSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }
}