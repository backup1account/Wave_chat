package com.example.project_app.auth.data_classes

import android.os.Parcel
import android.os.Parcelable


data class User(
    val email: String? = null,
    val name: String? = null,
    val profPictureUrl: String? = null,
    val randomIndex: Int? = null,
    val city: String? = null,
    var documentId: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(profPictureUrl)
        parcel.writeValue(randomIndex)
        parcel.writeString(city)
        parcel.writeString(documentId)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}