package com.example.bloodbank

object RequestRepository {

    private val requests = mutableListOf<EmergencyRequest>()

    fun add(request: EmergencyRequest) {
        requests.add(request)
    }

    fun getAll(): List<EmergencyRequest> = requests
}
