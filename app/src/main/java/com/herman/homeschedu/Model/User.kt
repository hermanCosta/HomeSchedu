package com.herman.homeschedu.Model

class User {
    var houseId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var password: String? = null
    var profileImageUrl: String? = null


    constructor(
        houseId: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        password: String?,
        profileImageUrl: String?
    ) {
        this.houseId = houseId
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.password = password
        this.profileImageUrl = profileImageUrl
    }


}




