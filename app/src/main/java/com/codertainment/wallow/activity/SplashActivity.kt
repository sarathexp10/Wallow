package com.codertainment.wallow.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.codertainment.wallow.R
import com.codertainment.wallow.getBoxStore
import com.codertainment.wallow.getCategoryBox
import com.codertainment.wallow.getWallpaperBox
import com.codertainment.wallow.model.Category
import com.codertainment.wallow.model.Wallpaper
import com.codertainment.wallow.util.ApiService
import com.codertainment.wallow.util.UIUtils
import com.mcxiaoke.koi.ext.delayed
import com.mcxiaoke.koi.ext.isConnected
import com.mcxiaoke.koi.ext.startActivity
import com.mcxiaoke.koi.log.logd
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

  var categories = ArrayList<Category>()
  var wallpapers = ArrayList<Wallpaper>()
  var categoryCounter = 0
  var disposable: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    UIUtils.setBarTranslucent(this, true, true)
    setContentView(R.layout.activity_splash)

    if (isConnected()) {
      disposable = ApiService.getInstance().getDirectoryContents().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              {
                logd(it.toString())
                it.filter { it.type == "dir" }.forEach { resp ->
                  if (!resp.isWallpaper()) {
                    val category = Category(name = resp.name, icon = it.find { it.name == resp.name + ".png" }?.downloadUrl)
                    categories.add(category)
                  }
                }
                getBoxStore().runInTx {
                  getCategoryBox().removeAll()
                  getCategoryBox().put(categories)
                }
                loadNextCategory()
              },
              {
                it.printStackTrace()
              }
          )
    } else if (getCategoryBox().count() == 0L || getWallpaperBox().count() == 0L) {
      splash_loader.smoothToHide()
      AlertDialog.Builder(this)
          .setTitle("Offline")
          .setMessage("An active internet connection is required for the first-time setup")
          .setPositiveButton("Ok") { dialogInterface, _ ->
            dialogInterface.dismiss()
            finish()
          }
          .create().show()
    } else {
      openHome()
    }
  }

  private fun loadNextCategory() {
    if (categoryCounter < categories.size) {
      loadDir()
      categoryCounter++
    } else {
      getBoxStore().runInTx {
        getWallpaperBox().removeAll()
        getWallpaperBox().put(wallpapers)
      }
      openHome()
    }
  }

  private fun openHome() {
    splash_loader.smoothToHide()
    Handler().delayed(500) {
      startActivity<MainActivity>()
      finish()
    }
  }

  private fun loadDir() {
    val currCategory = categories[categoryCounter]
    disposable = ApiService.getInstance().getDirectoryContents(currCategory.name).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            {
              it.filter { it.type == "file" }.forEach { resp ->
                if (resp.isWallpaper()) {
                  val wallpaper = Wallpaper(name = resp.name, size = resp.size, categoryId = currCategory.id, categoryName = currCategory.name, link = resp.downloadUrl)
                  wallpapers.add(wallpaper)
                }
              }
              loadNextCategory()
            },
            {
              it.printStackTrace()
            }
        )
  }

  override fun onDestroy() {
    disposable?.dispose()
    super.onDestroy()
  }
}
