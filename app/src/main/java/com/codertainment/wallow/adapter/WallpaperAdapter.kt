package com.codertainment.wallow.adapter

import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import br.com.simplepass.loading_button_lib.customViews.CircularProgressImageButton
import com.codertainment.wallow.R
import com.codertainment.wallow.activity.FullscreenWallpaperActivity
import com.codertainment.wallow.enableToolTip
import com.codertainment.wallow.model.Wallpaper
import com.codertainment.wallow.util.GlideApp
import com.codertainment.wallow.util.WallpaperUtil
import com.mcxiaoke.koi.ext.toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.util.concurrent.CancellationException

class WallpaperAdapter(val ctx: FragmentActivity, val data: List<Wallpaper>, var showCategory: Boolean = true, var hideButtons: Boolean = false, val animate: Boolean = true) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WallpaperViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_wallpaper, parent, false))

  override fun getItemCount() = data.size

  override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) = holder.bind(data[holder.adapterPosition])

  inner class WallpaperViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

    val name by bindView<TextView>(R.id.wall_name)
    val category by bindView<TextView>(R.id.wall_category)

    val download by bindView<CircularProgressImageButton>(R.id.wall_download)
    val apply by bindView<CircularProgressImageButton>(R.id.wall_apply)
    val image by bindView<ImageView>(R.id.wall_image)

    fun bind(item: Wallpaper) {
      name.setText(item.name)
      GlideApp.with(ctx).load(item.link).into(image)
      if (showCategory) {
        category.setText(item.categoryName)
      } else {
        category.setText(item.readableFileSize())
      }
      if (!hideButtons) {
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
        apply.setOnClickListener {
          apply.startAnimation()
          WallpaperUtil.apply(ctx, item,
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

        download.enableToolTip("Download")
        apply.enableToolTip("Apply Wallpaper")
      } else {
        download.visibility = View.GONE
        apply.visibility = View.GONE
      }

      v.setOnClickListener {
        FullscreenWallpaperActivity.openWall(ctx, image, name, adapterPosition, ArrayList(data), animate)
      }
    }
  }
}