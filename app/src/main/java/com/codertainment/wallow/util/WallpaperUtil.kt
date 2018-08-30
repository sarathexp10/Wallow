package com.codertainment.wallow.util

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.codertainment.wallow.R
import com.codertainment.wallow.model.Wallpaper
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.library.utils.PrefMan
import com.mcxiaoke.koi.ext.addToMediaStore
import com.mcxiaoke.koi.utils.nougatOrNewer
import io.reactivex.Observable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.wallpaperManager
import java.io.File
import java.util.concurrent.CancellationException

object WallpaperUtil {
  fun Context.getBasePath() = Environment.getExternalStorageDirectory().absolutePath + File.separator + getString(R.string.app_name) + File.separator + "Download"

  fun download(ctx: FragmentActivity, wall: Wallpaper): Observable<Int> = Observable.create<Int> {
    ctx.askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) { permissionResult ->
      if (permissionResult.isAccepted) {
        val shouldCategorise = PrefMan.getInstance(ctx).getBool(ctx.getString(R.string.key_wallpaper_download_location_mode))
        val baseFolder = if (shouldCategorise) {
          File(ctx.getBasePath() + File.separator + wall.categoryName)
        } else {
          File(ctx.getBasePath())
        }
        if (!baseFolder.exists()) {
          baseFolder.mkdirs()
        }
        val toSave = if (shouldCategorise) {
          File(baseFolder.absolutePath + File.separator + wall.name)
        } else {
          File(baseFolder.absolutePath + File.separator + wall.categoryName + "_" + wall.name)
        }
        AndroidNetworking.download(wall.link, baseFolder.absolutePath, toSave.name)
            .setPriority(Priority.HIGH)
            .build()
            .setDownloadProgressListener { bytesDownloaded, totalBytes ->
              val progress = ((bytesDownloaded * 100) / totalBytes).toInt()
              it.onNext(progress)
            }
            .startDownload(object : DownloadListener {
              override fun onDownloadComplete() {
                try {
                  ctx.addToMediaStore(toSave)
                  it.onComplete()
                } catch (e: Exception) {
                  e.printStackTrace()
                }
              }

              override fun onError(anError: ANError?) {
                anError?.printStackTrace()
                try {
                  it.onError(anError!!)
                } catch (e: Exception) {
                  e.printStackTrace()
                }
              }
            })
      } else {
        it.onError(CancellationException())
      }
    }
  }


  fun apply(ctx: Context, wall: Wallpaper, onSuccess: () -> Unit, onError: (e: Exception) -> Unit) {
    if (nougatOrNewer()) {
      val initial = booleanArrayOf(true, true)
      AlertDialog.Builder(ctx)
          .setMultiChoiceItems(arrayOf("Home Screen", "Lock Screen"), initial) { _, i, b ->
            initial[i] = b
          }
          .setPositiveButton("Apply") { dialogInterface, _ ->
            applyWallpaper(ctx, wall, initial[0], initial[1], onSuccess, onError)
            dialogInterface.dismiss()
          }
          .setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
            onError(CancellationException())
          }
          .create()
          .show()
    } else {
      applyWallpaper(ctx, wall, true, false, onSuccess, onError)
    }
  }

  private fun applyWallpaper(ctx: Context, wall: Wallpaper, applyOnHome: Boolean = true, applyOnLock: Boolean = false, onSuccess: () -> Unit, onError: (e: Exception) -> Unit) {
    try {
      GlideApp.with(ctx)
          .apply {
            val ro = RequestOptions()
            ro.format(DecodeFormat.PREFER_ARGB_8888)
            this.setRequestOptions(ro)
          }
          .asBitmap()
          .load(wall.link)
          .into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
              if (nougatOrNewer()) {
                doAsync {
                  if (applyOnHome) {
                    ctx.wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                    ctx.runOnUiThread {
                      onSuccess()
                    }
                  }
                  if (applyOnLock) {
                    ctx.wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK)
                    ctx.runOnUiThread {
                      onSuccess()
                    }
                  }
                }
              } else {
                doAsync {
                  ctx.wallpaperManager.setBitmap(resource)
                  ctx.runOnUiThread {
                    onSuccess()
                  }
                }
              }
            }
          })
    } catch (e: Exception) {
      e.printStackTrace()
      onError(e)
    }
  }
}