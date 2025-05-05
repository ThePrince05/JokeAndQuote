package com.project.jokeandquote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
// MutableLiveData for internal updates
val name = MutableLiveData<String>()
val officeAddress = MutableLiveData<String>()
val phoneNumber = MutableLiveData<String>()
val emailAddress = MutableLiveData<String>()
val bankName = MutableLiveData<String>()
val accountNumber = MutableLiveData<String>()
val nameOnAccount = MutableLiveData<String>()
val accountType = MutableLiveData<String>()

}