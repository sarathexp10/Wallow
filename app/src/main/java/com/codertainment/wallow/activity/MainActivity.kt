package com.codertainment.wallow.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.codertainment.wallow.BuildConfig
import com.codertainment.wallow.R
import com.codertainment.wallow.fragment.AboutFragment
import com.codertainment.wallow.fragment.WallpaperFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.mcxiaoke.koi.ext.startActivity
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

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
      divider {}
      categoryBox.all.forEach {
        primaryItem(it.name) {
          iicon = GoogleMaterial.Icon.gmd_wallpaper
          onClick { _ ->
            loadCategory(it.id, it.name)
            false
          }
        }
      }
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
}
