package com.codertainment.wallow.activity

import android.animation.Animator
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.Pair
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.codertainment.wallow.BLUR_RADIUS
import com.codertainment.wallow.R
import com.codertainment.wallow.model.Wallpaper
import com.codertainment.wallow.util.GlideApp
import com.codertainment.wallow.util.UIUtils
import com.codertainment.wallow.util.WallpaperUtil
import com.mcxiaoke.koi.ext.delayed
import com.mcxiaoke.koi.ext.toast
import com.mcxiaoke.koi.log.logd
import eightbitlab.com.blurview.RenderScriptBlur
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fullscreen_wallpaper.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import java.util.concurrent.CancellationException

class FullscreenWallpaperActivity : BaseActivity() {

  companion object {

    val CURRENT_POSITION = "current_position"
    val ALL_WALLPAPERS = "all_wallpapers"

    fun openWall(ctx: Activity, image: ImageView, title: TextView, currentPosition: Int, allWallpapers: ArrayList<Wallpaper>, animate: Boolean = true) {
      val i = Intent(ctx, FullscreenWallpaperActivity::class.java)
      i.putExtra(CURRENT_POSITION, currentPosition)
      i.putParcelableArrayListExtra(ALL_WALLPAPERS, allWallpapers)
      if (animate) {
        val b = ActivityOptions.makeSceneTransitionAnimation(ctx, Pair<View, String>(title, "title"), Pair<View, String>(image, "wallpaper")).toBundle()
        ctx.startActivity(i, b)
      } else {
        ctx.startActivity(i)
      }
    }
  }

  var currentPosition = 0
  var walls: List<Wallpaper>? = null
  var transitionCompleted = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    UIUtils.setBarTranslucent(this, true, true)

    setContentView(R.layout.activity_fullscreen_wallpaper)

    supportPostponeEnterTransition()

    setSupportActionBar(full_toolbar)
    full_toolbar.title = ""
    full_toolbar.subtitle = ""
    supportActionBar!!.setDisplayShowTitleEnabled(false)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    supportActionBar!!.setDisplayShowHomeEnabled(true)
    full_toolbar.setNavigationOnClickListener { onBackPressed() }

    walls = intent.getParcelableArrayListExtra(ALL_WALLPAPERS)
    currentPosition = intent.getIntExtra(CURRENT_POSITION, 0)

    if (walls == null) {
      toast("Invalid call")
      finish()
    }

    full_detail_title.text = walls!![currentPosition].nameWithoutExtension
    full_detail_category.text = walls!![currentPosition].categoryName
    full_detail_down_size.text = walls!![currentPosition].readableFileSize()

    wallpaper_viewpager.adapter = WallpaperPagerAdapter(walls!!)
    wallpaper_viewpager.currentItem = currentPosition
    wallpaper_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(p0: Int) {

      }

      override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

      }

      override fun onPageSelected(p0: Int) {
        currentPosition = p0
        full_detail_apply.setImageResource(R.drawable.ic_apply_wallpaper_white)
        full_detail_apply.resetProgress()
        full_detail_download.setImageResource(R.drawable.ic_download_white)
        full_detail_download.resetProgress()
        full_detail_share.setImageResource(R.drawable.ic_share_white)
        full_detail_share.resetProgress()
        full_detail_title.text = walls!![p0].nameWithoutExtension
        full_detail_category.text = walls!![p0].categoryName
        full_detail_down_size.text = walls!![p0].readableFileSize()
      }
    })
    wallpaper_viewpager.setPageTransformer(false) { page, position ->
      val normalizedPosition = Math.abs(Math.abs(position) - 1)
      page.scaleX = normalizedPosition / 2 + 0.5f
      page.scaleY = normalizedPosition / 2 + 0.5f
    }

    full_detail_apply.setOnClickListener {
      full_detail_apply.startAnimation()
      WallpaperUtil.apply(this@FullscreenWallpaperActivity, walls!![currentPosition],
                          {
                            full_detail_apply.revertAnimation {
                              full_detail_apply.setImageResource(R.drawable.ic_done_white)
                            }
                          },
                          {
                            full_detail_apply.revertAnimation()
                            if (!(it is CancellationException)) {
                              this@FullscreenWallpaperActivity.toast("Failed to apply wallpaper")
                            }
                          })
    }

    full_detail_download.setOnClickListener {
      full_detail_download.startAnimation()
      WallpaperUtil.download(this, walls!![currentPosition])
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            full_detail_download.setProgress(it)
          },
          {
            full_detail_download.revertAnimation()
            this@FullscreenWallpaperActivity.toast("Failed to download wallpaper")
          },
          {
            full_detail_download.revertAnimation {
              full_detail_download.setImageResource(R.drawable.ic_done_white)
            }
          }
        )
    }

    WallpaperUtil.setupShareButton(this, walls!![currentPosition], full_detail_share)

    if (!hasNavBar()) {
      guideline.setGuidelineEnd(0)
    }

    full_detail_blur.setupWith(contentView as ViewGroup)
      .setBlurAlgorithm(RenderScriptBlur(this))
      .setBlurRadius(BLUR_RADIUS)
      .setHasFixedTransformationMatrix(false)

    full_detail_toolbar_blur.setupWith(contentView as ViewGroup)
      .setBlurAlgorithm(RenderScriptBlur(this))
      .setBlurRadius(BLUR_RADIUS)
      .setHasFixedTransformationMatrix(false)

    Handler().delayed(1500) {
      hidePanels()
    }
  }


  fun hasNavBar(): Boolean {
    val real = Point()
    windowManager.defaultDisplay.getRealSize(real)
    val size = Point()
    windowManager.defaultDisplay.getSize(size)
    logd("real", real.toString())
    logd("size", size.toString())
    val h = when (windowManager.defaultDisplay.rotation) {
      Surface.ROTATION_0, Surface.ROTATION_180 -> {
        real.y != size.y
      }

      Surface.ROTATION_90, Surface.ROTATION_270 -> {
        real.x != size.x
      }

      else -> false
    }
    return h
  }

  fun hidePanels() {
    full_detail_toolbar_blur
      .animate()
      .translationY(-full_toolbar.height.toFloat())
      .setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {
          full_detail_toolbar_blur.visibility = View.GONE
        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}
      })
    full_detail_blur.hide()
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
        // Set the content to appear under the system bars so that the
        // content doesn't resize when the system bars hide and show.
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        // Hide the nav bar and status bar
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN)
  }

  fun showPanels() {
    full_detail_toolbar_blur.visibility = View.VISIBLE
    full_detail_toolbar_blur.animate()
      .translationY(0f)
      .setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {
          full_detail_toolbar_blur.visibility = View.VISIBLE
        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}
      })
    full_detail_blur.show()
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
  }

  fun View.hide() {
    animate()
      .translationY(height.toFloat())
      .setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {
          visibility = View.GONE
        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}
      })
  }

  fun View.show() {
    visibility = View.VISIBLE
    animate()
      .translationY(0f)
      .setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {
          visibility = View.VISIBLE
        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}
      })
  }

  inner class WallpaperPagerAdapter(val walls: List<Wallpaper>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
      val layout = layoutInflater.inflate(R.layout.wallpaper, container, false)
      val img = layout.find<ImageView>(R.id.wallpaper)
      val progress = layout.find<ProgressBar>(R.id.wallpaper_progress)

      val ro = RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)

      GlideApp.with(this@FullscreenWallpaperActivity)
        .asBitmap()
        .listener(object : RequestListener<Bitmap> {
          override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
            img.setImageResource(R.drawable.ic_error_dark)
            if (!transitionCompleted) {
              supportStartPostponedEnterTransition()
              transitionCompleted = true
            }
            return false
          }

          override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            progress.visibility = View.GONE
            img.setImageBitmap(resource)
            if (!transitionCompleted) {
              startPostponedEnterTransition()
              transitionCompleted = true
            }
            return false
          }
        })
        .load(walls[position].link)
        .apply(ro)
        .dontAnimate()
        .into(img)

      img.setOnClickListener {
        if (full_detail_blur.visibility == View.GONE) {
          showPanels()
        } else {
          hidePanels()
        }
      }
      container.addView(layout)

      return layout
    }

    override fun isViewFromObject(p0: View, p1: Any) = p0 == p1

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(`object` as View)

    override fun getCount() = walls.size
  }
}
