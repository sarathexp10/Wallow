package com.codertainment.wallow

import android.app.Application
import android.content.Context
import android.graphics.Rect
import androidx.appcompat.widget.TooltipCompat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import co.zsmb.materialdrawerkt.imageloader.drawerImageLoader
import com.androidnetworking.AndroidNetworking
import com.bumptech.glide.Glide
import com.codertainment.wallow.model.Category
import com.codertainment.wallow.model.MyObjectBox
import com.codertainment.wallow.model.Wallpaper
import com.mcxiaoke.koi.KoiConfig
import com.mcxiaoke.koi.log.logd
import com.mcxiaoke.koi.utils.oreoOrNewer
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import io.multimoon.colorful.Defaults
import io.multimoon.colorful.ThemeColor
import io.multimoon.colorful.initColorful
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.objectbox.kotlin.boxFor

class BaseApp : Application() {
  lateinit var boxStore: BoxStore

  override fun onCreate() {
    super.onCreate()
    val defaults = Defaults(
        primaryColor = ThemeColor.GREY,
        accentColor = ThemeColor.AMBER,
        useDarkTheme = true,
        translucent = false)
    initColorful(this, defaults)

    boxStore = MyObjectBox.builder().androidContext(this).build()
    if (BuildConfig.DEBUG) {
      KoiConfig.logEnabled = true
      KoiConfig.logLevel = Log.DEBUG
      val started = AndroidObjectBrowser(boxStore).start(this)
      logd("ObjectBox Browser", "$started")
    }

    drawerImageLoader {
      placeholder { ctx, tag ->
        DrawerUIUtils.getPlaceHolder(ctx)
      }
      set { imageView, uri, placeholder, tag ->
        Glide.with(imageView.context)
            .load(uri)
            .into(imageView)
      }
      cancel { imageView ->
        Glide.with(imageView.context)
            .clear(imageView)
      }
    }

    AndroidNetworking.initialize(this)
  }
}

fun Context.getBoxStore(): BoxStore = (this.applicationContext as BaseApp).boxStore

fun Context.getCategoryBox(): Box<Category> = getBoxStore().boxFor()

fun Context.getWallpaperBox(): Box<Wallpaper> = getBoxStore().boxFor()

fun View.enableToolTip(text: String) {
  if (oreoOrNewer()) {
    tooltipText = text
  } else {
    TooltipCompat.setTooltipText(this, text)
  }
}

val BLUR_RADIUS = 10f