package com.example.bloodbank

fun buildEmergencyMessage(request: EmergencyRequest): String {
    return """
        🚨 URGENT BLOOD REQUIRED 🚨
        
        Blood Group: ${request.bloodGroup}
        Location: ${request.location}
        Instructions: ${request.instructions}
        
        Status: ${request.status}
        
        Please help or share.
    """.trimIndent()
}
