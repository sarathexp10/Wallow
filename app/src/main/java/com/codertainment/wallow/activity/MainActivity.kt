package com.codertainment.wallow.activity

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import com.codertainment.wallow.BuildConfig
import com.codertainment.wallow.R
import com.codertainment.wallow.fragment.AboutFragment
import com.codertainment.wallow.fragment.WallpaperFragment
import com.codertainment.wallow.model.SuperCategory
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.mcxiaoke.koi.ext.delayed
import com.mcxiaoke.koi.ext.startActivity
import com.mcxiaoke.koi.ext.toast
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

  var backPressed = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID)

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    setSupportActionBar(main_toolbar)

    title = "Home"

    drawer {
      toolbar = main_toolbar

      primaryItem("Home") {
        iicon = GoogleMaterial.Icon.gmd_home
        onClick { _ ->
          loadCategory(0L)
          false
        }
      }

      divider { }

      val superCategories = ArrayList<SuperCategory>()
      val categories = categoryBox.all

      categories.forEach {
        val superCat = it.getSuperTypeName()

        val foundSuperCat = superCategories.find { it.name == superCat }

        if (foundSuperCat == null) {
          val s = SuperCategory(superCat)
          s.categories.add(it)
          superCategories.add(s)
        } else {
          foundSuperCat.categories.add(it)
        }
      }

      superCategories.forEach {
        //        sectionHeader(it.name)
        expandableItem(it.name) {

          selectable = false

          it.categories.forEach { cat ->
            primaryItem(cat.getCategoryName()) {
              iicon = GoogleMaterial.Icon.gmd_wallpaper
              onClick { _ ->
                loadCategory(cat.id, cat.getCategoryName())
                false
              }
            }
          }

        }
      }

      /*categoryBox.all.forEach {
        primaryItem(it.name) {
          iicon = GoogleMaterial.Icon.gmd_wallpaper
          onClick { _ ->
            loadCategory(it.id, it.name)
            false
          }
        }
      }*/

      divider {}
      primaryItem("Settings") {
        iicon = GoogleMaterial.Icon.gmd_settings
        onClick { _ ->
          startActivity<SettingsActivity>()
          false
        }
      }
      primaryItem("About") {
        iicon = GoogleMaterial.Icon.gmd_info
        onClick { _ ->
          loadFragment(AboutFragment())
          false
        }
      }

      selectedItemByPosition = 0
    }
    loadCategory(0)
  }

  private fun loadCategory(id: Long, title: String = "Home") {
    val wallFrag = WallpaperFragment()
    val bundle = Bundle()
    bundle.putLong(WallpaperFragment.CATEGORY_ID, id)
    wallFrag.arguments = bundle
    loadFragment(wallFrag)
    this@MainActivity.title = title
  }

  private fun loadFragment(frag: Fragment) = supportFragmentManager.beginTransaction().replace(R.id.main_frame, frag).commitNow()

  override fun onBackPressed() {
    if (!backPressed) {
      backPressed = true
      toast("Please press again to quit")
      Handler().delayed(2000) {
        backPressed = false
      }
    } else {
      super.onBackPressed()
    }
  }
}
