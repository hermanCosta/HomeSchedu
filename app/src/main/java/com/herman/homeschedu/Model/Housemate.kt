package com.herman.homeschedu.Model

class Housemate {
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var profileImageUrl: String? = null


    constructor(){

    }

    constructor(firstName: String?, lastName: String?, email: String?, profileImageUrl: String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.profileImageUrl = profileImageUrl
    }


}



