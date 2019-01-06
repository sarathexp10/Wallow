package com.codertainment.wallow.model


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SuperCategory(
  var name: String = "",
  var categories: ArrayList<Category> = ArrayList()
) : Parcelable