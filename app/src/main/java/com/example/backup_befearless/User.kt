package com.example.backup_befearless

data class User(
    var uid: String? = "",
    var name: String = "",
    var numbers: List<ContactModel> = listOf(),
    var emergencyMessage: String = "",
    var about: String = ""
) {
    companion object {
        val UID = "uid"
    }
}