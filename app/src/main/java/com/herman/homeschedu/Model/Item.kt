package com.herman.homeschedu.Model

import android.os.Parcel
import android.os.Parcelable

class Item : Parcelable {
    var itemId: String? = null
    var name: String? = null

    constructor(){}

    constructor(parcel: Parcel) : this() {
        itemId = parcel.readString()
        name = parcel.readString()
    }

    constructor(parcel: String)

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }

}