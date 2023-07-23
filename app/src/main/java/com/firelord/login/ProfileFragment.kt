package com.firelord.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.firelord.login.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentProfileBinding

    // init firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // handle onClickButton
        profileBinding.btLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
    }
    private fun checkUser(){
        //check user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            val email = firebaseUser.email
            // Set to textView
            profileBinding.tvEmail.text = email
        } else {
            profileBinding.root.findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}