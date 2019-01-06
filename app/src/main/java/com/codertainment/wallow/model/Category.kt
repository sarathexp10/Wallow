package com.codertainment.wallow.model


import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Category(
  @Id
  var id: Long = 0,
  var name: String = "",
  var icon: String? = null
) : Parcelable {
  fun getSuperTypeName() = name.split("-")[0]
  fun getCategoryName() = name.split("-")[1]
}