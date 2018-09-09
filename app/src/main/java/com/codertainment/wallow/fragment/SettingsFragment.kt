package com.codertainment.wallow.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.DropDownPreference
import android.support.v7.preference.Preference
import com.codertainment.wallow.R
import com.takisoft.fix.support.v7.preference.ColorPickerPreference
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import io.multimoon.colorful.Colorful
import io.multimoon.colorful.ThemeColor

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

  lateinit var primaryColor: ColorPickerPreference
  lateinit var accentColor: ColorPickerPreference
  lateinit var darkTheme: SwitchPreference
  lateinit var displayMode: DropDownPreference
  lateinit var gridMode: DropDownPreference

  override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
    if (p0 != null && p1 != null) {
      if (p0.key == getString(R.string.key_primary_color) || p0.key == getString(R.string.key_accent_color) || p0.key == getString(R.string.key_dark_theme)) {
        if (p0 is ColorPickerPreference) {
          if (p0.key == getString(R.string.key_primary_color)) {
            primaryColorValue = p1 as Int
          } else if (p0.key == getString(R.string.key_accent_color)) {
            accentColorValue = p1 as Int
          }
        } else if (p0 is SwitchPreference) {
          if (p0.key == getString(R.string.key_dark_theme)) {
            isDarkTheme = p1 as Boolean
          }
        }
        Handler().postDelayed({ updateTheme() }, 200)
      }
    }
    return true
  }

  var accentColorValue: Int = ThemeColor.AMBER.primaryStyle()
  var primaryColorValue: Int = ThemeColor.GREY.primaryStyle()
  var isDarkTheme: Boolean = true
  lateinit var colors: IntArray
  val themes = arrayOf(ThemeColor.RED,
      ThemeColor.PINK,
      ThemeColor.PURPLE,
      ThemeColor.DEEP_PURPLE,
      ThemeColor.INDIGO,
      ThemeColor.BLUE,
      ThemeColor.LIGHT_BLUE,
      ThemeColor.CYAN,
      ThemeColor.TEAL,
      ThemeColor.GREEN,
      ThemeColor.LIGHT_GREEN,
      ThemeColor.LIME,
      ThemeColor.YELLOW,
      ThemeColor.AMBER,
      ThemeColor.ORANGE,
      ThemeColor.DEEP_ORANGE,
      ThemeColor.BROWN,
      ThemeColor.GREY,
      ThemeColor.BLUE_GREY,
      ThemeColor.WHITE,
      ThemeColor.BLACK)

  override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
    preferenceManager.sharedPreferencesName = "prefs"
    setPreferencesFromResource(R.xml.settings, rootKey)
    colors = resources.getIntArray(R.array.default_colors)
    bind()
    accentColorValue = Colorful().getAccentColor().getColorPack().normal().asInt()
    primaryColorValue = Colorful().getPrimaryColor().getColorPack().normal().asInt()
    isDarkTheme = Colorful().getDarkTheme()
  }

  private fun bind() {
    primaryColor = findPreference(getString(R.string.key_primary_color)) as ColorPickerPreference
    primaryColor.onPreferenceChangeListener = this

    accentColor = findPreference(getString(R.string.key_accent_color)) as ColorPickerPreference
    accentColor.onPreferenceChangeListener = this

    darkTheme = findPreference(getString(R.string.key_dark_theme)) as SwitchPreference
    darkTheme.onPreferenceChangeListener = this

    displayMode = findPreference(getString(R.string.key_view_config)) as DropDownPreference
    gridMode = findPreference(getString(R.string.key_grid_count)) as DropDownPreference

    displayMode.setOnPreferenceChangeListener { _, any ->
      gridMode.isEnabled = any as String == "2"
      true
    }

    gridMode.isEnabled = displayMode.value == "2"
  }

  private fun updateTheme() {
    Colorful()
        .edit()
        .setPrimaryColor(themes[colors.indexOf(primaryColorValue)])
        .setAccentColor(themes[colors.indexOf(accentColorValue)])
        .setDarkTheme(isDarkTheme)
        .apply(activity!!.applicationContext) {
          activity!!.recreate()
        }
  }
}
