package com.codertainment.wallow

import android.app.Application
import android.content.Context
import android.graphics.Rect
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
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
        primaryColor = ThemeColor.GREEN,
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
    this.setOnLongClickListener {
      val screenPos = IntArray(2)
      val displayFrame = Rect()
      getLocationOnScreen(screenPos)
      getWindowVisibleDisplayFrame(displayFrame)

      val context = context
      val width = width
      val height = height
      val midy = screenPos[1] + height / 2
      var referenceX = screenPos[0] + width / 2
      if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        referenceX = screenWidth - referenceX // mirror
      }
      //TODO Set proper gravity below the view
      val cheatSheet = Toast.makeText(context, text, Toast.LENGTH_SHORT)
      if (midy < displayFrame.height()) {
        // Show along the top; follow action buttons
        cheatSheet.setGravity(Gravity.TOP or GravityCompat.END, referenceX, height)
      } else {
        // Show along the bottom center
        cheatSheet.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, height)
      }
      cheatSheet.show()
      true
    }
  }
}