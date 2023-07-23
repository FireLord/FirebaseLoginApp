package com.firelord.login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider

class LoginViewModel:ViewModel() {
    val OTP: MutableLiveData<String> = MutableLiveData()
    val resendToken: MutableLiveData<PhoneAuthProvider.ForceResendingToken> = MutableLiveData()
    val number: MutableLiveData<String> = MutableLiveData()
}