package com.codertainment.wallow.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codertainment.wallow.R
import com.codertainment.wallow.adapter.WallpaperAdapter
import com.codertainment.wallow.getCategoryBox
import com.codertainment.wallow.getWallpaperBox
import com.codertainment.wallow.model.Wallpaper
import com.codertainment.wallow.model.Wallpaper_
import com.library.utils.PrefMan
import io.objectbox.kotlin.query
import kotterknife.bindView

class WallpaperFragment : Fragment() {

  companion object {
    val CATEGORY_ID = "category_id"
  }

  val wallRecycler by bindView<RecyclerView>(R.id.wallpaper_recycler)
  var categoryId = 0L

  val categoryBox by lazy {
    requireContext().getCategoryBox()
  }

  val wallpaperBox by lazy {
    requireContext().getWallpaperBox()
  }

  val VIEW_MODE_GRID = "2"
  var wallpapers = ArrayList<Wallpaper>()
  var gridCount = 3
  var gridMode = "1"
  var shouldShowCategory = true

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_wallpaper, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (arguments != null) {
      categoryId = arguments!!.getLong(CATEGORY_ID)
    }

    if (categoryId == 0L) {
      wallpapers.addAll(wallpaperBox.all)
    } else {
      shouldShowCategory = false
      wallpapers.addAll(wallpaperBox.query {
        this.equal(Wallpaper_.categoryId, categoryId)
      }.find())
    }

    setup()
  }

  override fun onResume() {
    super.onResume()
    setup()
  }

  private fun setup() {
    gridMode = PrefMan.getInstance(requireContext()).getString(getString(R.string.key_view_config), "1")
    if (gridMode == VIEW_MODE_GRID) {
      gridCount = PrefMan.getInstance(requireContext()).getString(getString(R.string.key_grid_count), "3").toInt()
    }

    if (gridCount > 1 && gridMode == VIEW_MODE_GRID) {
      wallRecycler.layoutManager = GridLayoutManager(requireContext(), gridCount)
      wallRecycler.adapter = WallpaperAdapter(requireActivity(), wallpapers, shouldShowCategory, true, true)
    } else {
      wallRecycler.layoutManager = LinearLayoutManager(requireContext())
      wallRecycler.adapter = WallpaperAdapter(requireActivity(), wallpapers, shouldShowCategory, false, false)
    }
  }
}
