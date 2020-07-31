package com.herman.homeschedu.Model

class ScheduleInformation {
    var calendarUri: String = ""
    var resource: String? = null
    var userId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var profileImageUrl: String? = null
    var time: String? = null
    var itemId: String? = null
    var itemName: String? = null
    var placeId: String? = null
    var placeName: String? = null
    var placeAddress: String? = null
    var placeInformation: String? = null
    var timestamp: com.google.firebase.Timestamp? = null
    var slot: Long = 0

    constructor()


    constructor(
        calendarUri: String,
        resource: String?,
        customerId: String?,
        customerFirstName: String?,
        customerLastName: String?,
        customerEmail: String?,
        customerProfileImageUrl: String?,
        time: String?,
        itemId: String?,
        itemName: String?,
        placeId: String?,
        placeName: String?,
        placeAddress: String?,
        placeInformation: String?,
        slot: Long


    ) {
        this.calendarUri = calendarUri
        this.resource = resource
        this.userId = customerId
        this.firstName = customerFirstName
        this.lastName = customerLastName
        this.email = customerEmail
        this.profileImageUrl = customerProfileImageUrl
        this.time = time
        this.itemId = itemId
        this.itemName = itemName
        this.placeId = placeId
        this.placeName = placeName
        this.placeAddress = placeAddress
        this.placeInformation = placeInformation
        this.slot = slot

    }


}