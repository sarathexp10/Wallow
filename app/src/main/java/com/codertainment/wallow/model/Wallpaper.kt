package com.codertainment.wallow.model

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize
import java.text.DecimalFormat

@Parcelize
@Entity
data class Wallpaper(@Id
                     var id: Long = 0,
                     var name: String = "",
                     var size: Long = 0,
                     var categoryId: Long = 0,
                     var categoryName: String = "",
                     var link: String = "0") : Parcelable {

  fun readableFileSize() =
      if (size <= 0) {
        "0"
      } else {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + units[digitGroups]
      }
}