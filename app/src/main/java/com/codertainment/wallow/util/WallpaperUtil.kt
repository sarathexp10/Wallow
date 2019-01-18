package com.codertainment.wallow.util

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import androidx.core.content.FileProvider
import androidx.appcompat.app.AlertDialog
import androidx.palette.graphics.Palette
import android.webkit.MimeTypeMap
import br.com.simplepass.loading_button_lib.customViews.CircularProgressImageButton
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.codertainment.wallow.BuildConfig
import com.codertainment.wallow.R
import com.codertainment.wallow.model.Wallpaper
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.mcxiaoke.koi.ext.addToMediaStore
import com.mcxiaoke.koi.ext.toast
import com.mcxiaoke.koi.utils.nougatOrNewer
import io.multimoon.colorful.Colorful
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.wallpaperManager
import java.io.File
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WallpaperUtil {
  fun Context.getBasePath() =
    Environment.getExternalStorageDirectory().absolutePath + File.separator + getString(R.string.app_name) + File.separator + "Download"

  fun download(ctx: FragmentActivity, wall: Wallpaper): Observable<Int> = Observable.create<Int> {
    ctx.askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) { permissionResult ->
      if (permissionResult.isAccepted) {
        val shouldCategorise = PrefMan.getInstance(ctx).getBool(ctx.getString(R.string.key_wallpaper_download_location_mode))
        val baseFolder = if (shouldCategorise) {
          File(ctx.getBasePath() + File.separator + wall.getSuperCategoryName() + File.separator + wall.getCurrentCategoryName())
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
        if (toSave.exists() && toSave.isFile) {
          it.onComplete()
          return@askPermission
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

  fun getWallpaperFile(ctx: Context, wall: Wallpaper): File {
    val shouldCategorise = PrefMan.getInstance(ctx).getBool(ctx.getString(R.string.key_wallpaper_download_location_mode))
    val baseFolder = if (shouldCategorise) {
      File(ctx.getBasePath() + File.separator + wall.getSuperCategoryName() + File.separator + wall.getCurrentCategoryName())
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
    return toSave
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

  private fun applyWallpaper(
    ctx: Context,
    wall: Wallpaper,
    applyOnHome: Boolean = true,
    applyOnLock: Boolean = false,
    onSuccess: () -> Unit,
    onError: (e: Exception) -> Unit
  ) {
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

  fun setupShareButton(ctx: FragmentActivity, wallpaper: Wallpaper, shareButton: CircularProgressImageButton) {
    shareButton.setOnClickListener {
      shareButton.startAnimation()
      WallpaperUtil.download(ctx, wallpaper)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            shareButton.setProgress(it)
          },
          {
            shareButton.revertAnimation()
            ctx.toast("Failed to download wallpaper")
          },
          {
            shareButton.revertAnimation {
              shareButton.setImageResource(R.drawable.ic_done_white)
            }
            share(ctx, wallpaper)
          }
        )
    }
  }

  private fun share(ctx: Context, wall: Wallpaper) {
    val toShare = getWallpaperFile(ctx, wall)
    val i = Intent(Intent.ACTION_SEND).apply {
      type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(toShare.extension)
      putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ctx, "${BuildConfig.APPLICATION_ID}.fileprovider", toShare))
      putExtra(
        Intent.EXTRA_TEXT,
        "${wall.nameWithoutExtension} (${wall.categoryName})\nShared from Wallow\n\n" +
            "Get it: https://play.google.com/apps/details?id=${BuildConfig.APPLICATION_ID}"
      )
    }
    ctx.startActivity(Intent.createChooser(i, "Share wallpaper via"))
  }

  suspend fun getColorFromBitmap(bitmap: Bitmap): Pair<Int?, Int?> = suspendCoroutine {
    Palette.from(bitmap).generate { p ->
      it.resume(
        if (Colorful().getDarkTheme()) {
          Pair(p?.vibrantSwatch?.rgb, p?.vibrantSwatch?.titleTextColor)
        } else {
          Pair(p?.vibrantSwatch?.rgb, p?.vibrantSwatch?.titleTextColor)
        }
      )
    }
  }
}