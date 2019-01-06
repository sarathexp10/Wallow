package com.codertainment.wallow.fragment


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.codertainment.wallow.R
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.multimoon.colorful.Colorful
import org.jetbrains.anko.padding
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.ctx
import us.feras.mdv.MarkdownView

class AboutFragment : MaterialAboutFragment() {
  override fun getMaterialAboutList(p0: Context?): MaterialAboutList {

    requireContext().runOnUiThread {
      requireActivity().title = "About"
    }

    val mal = MaterialAboutList.Builder()

    val card1 = getCard()

    val title = MaterialAboutTitleItem.Builder()
      .text(R.string.app_name)
      .desc(R.string.app_description)
      .icon(R.mipmap.ic_launcher)
      .build()

    card1.addItem(title)

    val version = ConvenienceBuilder.createVersionActionItem(
      requireContext(),
      getIcon(GoogleMaterial.Icon.gmd_info_outline),
      "Version",
      true
    )
    card1.addItem(version)

    val markdownTheme = if (Colorful().getDarkTheme()) "file:///android_asset/dark.css" else "file:///android_asset/light.css"
    val changelog = MaterialAboutActionItem.Builder()
      .icon(getIcon(GoogleMaterial.Icon.gmd_history))
      .text("Changelog")
      .setOnClickAction {
        ctx.runOnUiThread {
          AlertDialog.Builder(ctx)
            .setPositiveButton("Cool") { d, _ ->
              d.dismiss()
            }
            .setView(MarkdownView(ctx).apply {
              loadMarkdownFile("https://raw.githubusercontent.com/shripal17/Wallow/master/CHANGELOG.md", markdownTheme)
              padding = 16
            })
            .create()
            .show()
        }
      }
    card1.addItem(changelog.build())

    val t = if (Colorful().getDarkTheme()) Libs.ActivityStyle.DARK else Libs.ActivityStyle.LIGHT_DARK_TOOLBAR
    val licenses = MaterialAboutActionItem.Builder()
      .text("Open Source Licenses")
      .icon(getIcon(GoogleMaterial.Icon.gmd_book))
      .setOnClickAction {
        LibsBuilder()
          .withAboutIconShown(false)
          .withAboutVersionShown(false)
          .withAboutDescription("")
          .withActivityTitle("Open Source Licenses")
          .withActivityStyle(t)
          .start(requireContext())
      }
      .build()
    card1.addItem(licenses)

    mal.addCard(card1.build())

    val people = getCard().apply {
      title("People")
      addItem(getPerson("Savio Perera", "Lead Wallpaper Designer", "Wizper99"))
      addItem(getPerson("Sarath EXP", "Wallpaper Designer"))
      addItem(getPerson("Biplov Biswas", "Wallpaper Designer"))
      addItem(getPerson("Rahul Ahuja", "Wallpaper Designer"))
      addItem(getPerson("Shripal jain", "App Developer", "shripal17"))
    }

    mal.addCard(people.build())

    val card2 = getCard()

    val rate = ConvenienceBuilder.createRateActionItem(requireContext(), getIcon(MaterialDesignIconic.Icon.gmi_star), "Rate the app", null)
    card2.addItem(rate)


    val wallSource = MaterialAboutActionItem.Builder()
      .text("View wallpapers source or contribute")
      .icon(getIcon(MaterialDesignIconic.Icon.gmi_github))
      .setOnClickAction {
        openGithub("https://github.com/Wizper99/Wallpapers")
      }
      .build()
    card2.addItem(wallSource)

    val source = MaterialAboutActionItem.Builder()
      .text("View app source or contribute")
      .icon(getIcon(MaterialDesignIconic.Icon.gmi_github))
      .setOnClickAction {
        openGithub("https://github.com/shripal17/Wallow")
      }
      .build()
    card2.addItem(source)

    mal.addCard(card2.build())

    return mal.build()
  }

  private fun getPerson(name: String, role: String, githubUsername: String? = null) = MaterialAboutActionItem.Builder().apply {
    text(name)
    subText(role)
    icon(getIcon(MaterialDesignIconic.Icon.gmi_account))
    if (githubUsername != null && githubUsername.isNotEmpty()) {
      setOnClickAction {
        openGithub("https://github.com/$githubUsername")
      }
    }
  }.build()

  private fun getDeveloperCard(name: String, role: String, githubUsername: String): MaterialAboutCard {
    val c = getCard()
    val dev = MaterialAboutActionItem.Builder()
      .text(name)
      .subText(role)
      .icon(getIcon(MaterialDesignIconic.Icon.gmi_account))
      .build()
    c.addItem(dev)
    val devGithub = MaterialAboutActionItem.Builder()
      .text("Fork on GitHub")
      .icon(getIcon(MaterialDesignIconic.Icon.gmi_github))
      .setOnClickAction {
        openGithub("https://github.com/$githubUsername")
      }
      .build()
    c.addItem(devGithub)

    return c.build()
  }

  private fun openGithub(link: String) = requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

  private fun getIcon(iicon: IIcon) = IconicsDrawable(requireContext()).icon(iicon).color(getIconColor())

  private fun getIconColor() = Colorful().getAccentColor().getColorPack().normal().asInt()

  private fun getCard() = MaterialAboutCard.Builder()

  override fun getTheme() =
    if (Colorful().getDarkTheme()) {
      R.style.Theme_Mal_Dark_LightActionBar
    } else {
      R.style.Theme_Mal_Light_DarkActionBar
    }
}
