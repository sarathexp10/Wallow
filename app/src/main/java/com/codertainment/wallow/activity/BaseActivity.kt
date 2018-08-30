package com.codertainment.wallow.activity

import android.os.Bundle
import com.codertainment.wallow.R
import com.codertainment.wallow.getCategoryBox
import com.codertainment.wallow.getWallpaperBox
import com.codertainment.wallow.model.Category
import com.codertainment.wallow.model.Wallpaper
import io.multimoon.colorful.CAppCompatActivity
import io.multimoon.colorful.Colorful
import io.objectbox.Box
import io.reactivex.disposables.Disposable

open class BaseActivity : CAppCompatActivity() {

  var disposable: Disposable? = null
  val categoryBox: Box<Category> by lazy {
    getCategoryBox()
  }
  val wallpaperBox: Box<Wallpaper> by lazy {
    getWallpaperBox()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Colorful().getDarkTheme()) {
      Colorful().edit()
          .setPrimaryColor(Colorful().getPrimaryColor())
          .setAccentColor(Colorful().getAccentColor())
          .setDarkTheme(true)
          .setCustomThemeOverride(R.style.MaterialDrawerDark)
          .apply(this)
    } else {
      Colorful().edit()
          .setPrimaryColor(Colorful().getPrimaryColor())
          .setAccentColor(Colorful().getAccentColor())
          .setDarkTheme(false)
          .setCustomThemeOverride(R.style.MaterialDrawerLight)
          .apply(this)
    }
  }
}
