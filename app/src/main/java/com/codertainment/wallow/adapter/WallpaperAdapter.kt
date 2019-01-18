package com.codertainment.wallow.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import br.com.simplepass.loading_button_lib.customViews.CircularProgressImageButton
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.codertainment.wallow.BLUR_RADIUS
import com.codertainment.wallow.R
import com.codertainment.wallow.activity.FullscreenWallpaperActivity
import com.codertainment.wallow.enableToolTip
import com.codertainment.wallow.model.Wallpaper
import com.codertainment.wallow.util.GlideApp
import com.codertainment.wallow.util.WallpaperUtil
import com.mcxiaoke.koi.ext.toast
import com.mcxiaoke.koi.log.logd
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.util.concurrent.CancellationException
import kotlin.system.measureTimeMillis

class WallpaperAdapter(
  val ctx: FragmentActivity,
  val data: List<Wallpaper>,
  var showCategory: Boolean = true,
  var hideButtons: Boolean = false,
  val animate: Boolean = true
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    WallpaperViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_wallpaper, parent, false))

  override fun getItemCount() = data.size

  override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) = holder.bind(data[holder.adapterPosition])

  inner class WallpaperViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

    val name by bindView<TextView>(R.id.wall_name)
    val category by bindView<TextView>(R.id.wall_category)

    val download by bindView<CircularProgressImageButton>(R.id.wall_download)
    val apply by bindView<CircularProgressImageButton>(R.id.wall_apply)
    val share by bindView<CircularProgressImageButton>(R.id.wall_share)
    val image by bindView<ImageView>(R.id.wall_image)
    val blurView by bindView<BlurView>(R.id.blurView)
    val detailContainer by bindView<ConstraintLayout>(R.id.wall_detail_container)

    init {
      setIsRecyclable(true)
    }

    fun bind(item: Wallpaper) {
      name.text = item.nameWithoutExtension
      GlideApp.with(ctx)
        .load(item.link)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .error(R.drawable.ic_error_white)
        .into(image)

      category.text = if (showCategory) {
        item.categoryName
      } else {
        item.readableFileSize()
      }

      if (!hideButtons) {
        apply.visibility = View.VISIBLE
        share.visibility = View.VISIBLE

        apply.setOnClickListener {
          apply.startAnimation()
          WallpaperUtil.apply(
            ctx, item,
            {
              apply.revertAnimation {
                apply.setImageResource(R.drawable.ic_done_white)
              }
              Snackbar.make(ctx.findViewById(R.id.main), "Wallpaper Applied", Snackbar.LENGTH_SHORT).show()
            },
            {
              apply.revertAnimation()
              if (!(it is CancellationException)) {
                ctx.toast("Failed to apply wallpaper")
              }
            })
        }

        WallpaperUtil.setupShareButton(ctx, item, share)

        download.enableToolTip("Download")
        apply.enableToolTip("Apply Wallpaper")
        share.enableToolTip("Share")
      } else {
        apply.visibility = View.GONE
        share.visibility = View.GONE
      }


      download.setOnClickListener {
        download.startAnimation()
        WallpaperUtil.download(ctx, item)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            {
              download.setProgress(it)
            },
            {
              download.revertAnimation()
              ctx.toast("Failed to download wallpaper")
            },
            {
              download.revertAnimation {
                download.setImageResource(R.drawable.ic_done_white)
              }
              Snackbar.make(ctx.findViewById(R.id.main), "Wallpaper Downloaded to ${ctx.getString(R.string.app_name)}/Download", Snackbar.LENGTH_SHORT).show()
            }
          )
      }

      logd("blur time $adapterPosition", measureTimeMillis {
        blurView.setupWith(v as ViewGroup)
          .setBlurAlgorithm(RenderScriptBlur(ctx))
          .setBlurRadius(BLUR_RADIUS)
          .setHasFixedTransformationMatrix(true)
      }.toString())

      v.setOnClickListener {
        FullscreenWallpaperActivity.openWall(ctx, image, name, adapterPosition, ArrayList(data), animate)
      }
    }
  }
}