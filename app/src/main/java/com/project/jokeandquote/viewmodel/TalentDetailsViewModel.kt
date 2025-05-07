package com.project.jokeandquote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.jokeandquote.model.TalentDetailsRecord
import com.project.jokeandquote.service.TalentDetailsDao
import kotlinx.coroutines.launch

class TalentDetailsViewModel(application: Application) : AndroidViewModel(application)  {

    private val dao = TalentDetailsDao(application)
    val talentDetails = MutableLiveData<TalentDetailsRecord?>()

    fun loadTalentDetails() {
        viewModelScope.launch {
            val details = dao.getRecords().firstOrNull()
            talentDetails.postValue(details)
        }
    }
}