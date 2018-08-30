package com.codertainment.wallow.activity

import android.os.Bundle
import com.codertainment.wallow.R
import com.codertainment.wallow.fragment.SettingsFragment
import io.multimoon.colorful.CAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class SettingsActivity : CAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setSupportActionBar(main_toolbar)

    title = "Settings"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction().replace(R.id.main_frame, SettingsFragment()).commitNow()
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
