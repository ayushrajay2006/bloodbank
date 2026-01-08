package com.example.bloodbank.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.EmergencyRequest
import com.example.bloodbank.RequestRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RequestRepository(application)

    val requests: StateFlow<List<EmergencyRequest>> = repository.getAllRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun resolveRequest(id: Int) {
        viewModelScope.launch {
            repository.resolveRequest(id)
        }
    }
}
