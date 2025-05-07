package com.project.jokeandquote.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.jokeandquote.model.TalentDetailsRecord
import com.project.jokeandquote.service.TalentDetailsDao
import kotlinx.coroutines.launch


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
val logoUri = MutableLiveData<Uri?>()

    private val talentDetailsDao = TalentDetailsDao(application)

    suspend fun saveDetailsToDatabase(): Boolean {
        return try {
            val record = TalentDetailsRecord(
                name = name.value.orEmpty(),
                officeAddress = officeAddress.value.orEmpty(),
                phoneNumber = phoneNumber.value.orEmpty(),
                emailAddress = emailAddress.value.orEmpty(),
                bankName = bankName.value.orEmpty(),
                accountNumber = accountNumber.value.orEmpty(),
                nameOnAccount = nameOnAccount.value.orEmpty(),
                accountType = accountType.value.orEmpty(),
                logoUri = logoUri.value?.toString() ?: ""
            )

            val existingRecords = talentDetailsDao.getRecords()
            if (existingRecords.isNotEmpty()) {
                // Use the existing record's ID
                val existingRecord = existingRecords.first()
                val updatedRecord = record.copy(id = existingRecord.id)
                talentDetailsDao.updateRecord(updatedRecord)
            } else {
                talentDetailsDao.insertRecord(record)
            }
            true // Successfully saved
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Database save failed", e)
            false // Failed to save
        }
    }

    fun loadDetailsFromDatabase() {
        viewModelScope.launch {
            try {
                val existingRecords = talentDetailsDao.getRecords()
                if (existingRecords.isNotEmpty()) {
                    val record = existingRecords.first()
                    name.postValue(record.name)
                    officeAddress.postValue(record.officeAddress)
                    phoneNumber.postValue(record.phoneNumber)
                    emailAddress.postValue(record.emailAddress)
                    bankName.postValue(record.bankName)
                    accountNumber.postValue(record.accountNumber)
                    nameOnAccount.postValue(record.nameOnAccount)
                    accountType.postValue(record.accountType)
                    logoUri.postValue(Uri.parse(record.logoUri))
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to load details", e)
            }
        }
    }


}

