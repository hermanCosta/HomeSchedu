package com.herman.homeschedu.Model

import android.os.Parcel
import android.os.Parcelable

class Place : Parcelable {

    var name: String? = null
    var local: String? = null
    var information: String? = null
    var placeId: String? = null

    constructor()

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        local = parcel.readString()
        information = parcel.readString()
        placeId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(local)
        parcel.writeString(information)
        parcel.writeString(placeId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Place> {
        override fun createFromParcel(parcel: Parcel): Place {
            return Place(parcel)
        }

        override fun newArray(size: Int): Array<Place?> {
            return arrayOfNulls(size)
        }
    }

}




