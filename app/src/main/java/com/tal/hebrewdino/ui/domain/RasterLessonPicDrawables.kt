package com.tal.hebrewdino.ui.domain

import androidx.annotation.DrawableRes
import com.tal.hebrewdino.R

/** Raster lesson art in `res/drawable-nodpi/lesson_pic_*.png`. */
object RasterLessonPicDrawables {
    val tileDrawables: Set<Int> =
        setOf(
            // Season 1 Chapter 1 (א ב ד ל מ)
            R.drawable.lesson_pic_ish,
            R.drawable.lesson_pic_aryeh,
            R.drawable.lesson_pic_avatiach,
            R.drawable.lesson_pic_car,
            R.drawable.lesson_pic_arnav,
            R.drawable.lesson_pic_bait,
            R.drawable.lesson_pic_balloon,
            R.drawable.lesson_pic_barvaz,
            R.drawable.lesson_pic_dag,
            R.drawable.lesson_pic_delet,
            R.drawable.lesson_pic_dachlil,
            R.drawable.lesson_pic_dvash,
            R.drawable.lesson_pic_lechem,
            R.drawable.lesson_pic_lev,
            R.drawable.lesson_pic_limuda,
            R.drawable.lesson_pic_machbat,
            R.drawable.lesson_pic_medusa,
            R.drawable.lesson_pic_mitah,
            R.drawable.lesson_pic_motzetz,
            // Season 2 Chapter 1
            R.drawable.lesson_pic_zebra,
            R.drawable.lesson_pic_zikit,
            R.drawable.lesson_pic_zakhal,
            R.drawable.lesson_pic_zer,
            R.drawable.lesson_pic_yeled,
            R.drawable.lesson_pic_yad,
            R.drawable.lesson_pic_yona,
            R.drawable.lesson_pic_yareach,
            R.drawable.lesson_pic_yanshuf,
            R.drawable.lesson_pic_sus,
            R.drawable.lesson_pic_sira,
            R.drawable.lesson_pic_sukariya,
            R.drawable.lesson_pic_sefer,
            R.drawable.lesson_pic_sartan,
            R.drawable.lesson_pic_ayin,
            R.drawable.lesson_pic_agvania,
            R.drawable.lesson_pic_akbar,
            R.drawable.lesson_pic_uga,
            R.drawable.lesson_pic_etz,
            R.drawable.lesson_pic_aleh,
            R.drawable.lesson_pic_akavish,
        )

    fun isPngTile(@DrawableRes drawableRes: Int): Boolean = drawableRes in tileDrawables
}
