package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Chapters 2 / 4 / 5 lesson words wired to raster PNGs in [drawable-nodpi].
 */
class LessonWordCatalogChapters245PngWiringTest {
    @Test
    fun chapters245_words_useRasterPngTiles() {
        val expected =
            mapOf(
                // Chapter 2
                "w_ג_1" to R.drawable.lesson_pic_gamal,
                "w_ג_2" to R.drawable.lesson_pic_glida,
                "w_ג_3" to R.drawable.lesson_pic_gader,
                "w_ג_4" to R.drawable.lesson_pic_girafa,
                "w_ה_1" to R.drawable.lesson_pic_har,
                "w_ה_2" to R.drawable.lesson_pic_haftaa,
                "w_ה_3" to R.drawable.lesson_pic_hipopotam,
                "w_ו_1" to R.drawable.lesson_pic_vered,
                "w_ו_2" to R.drawable.lesson_pic_wafel,
                "w_ו_3" to R.drawable.lesson_pic_vilon,
                "w_ר_1" to R.drawable.lesson_pic_rosh,
                "w_ר_2" to R.drawable.lesson_pic_car,
                "w_ר_3" to R.drawable.lesson_pic_regel,
                "w_ר_4" to R.drawable.lesson_pic_rakevet,
                "w_ר_5" to R.drawable.lesson_pic_ramzor,
                "w_ש_1" to R.drawable.lesson_pic_shemesh,
                "w_ש_2" to R.drawable.lesson_pic_shulchan,
                "w_ש_3" to R.drawable.lesson_pic_shinayim,
                "w_ש_4" to R.drawable.lesson_pic_shaon,
                // Chapter 4
                "w_ח_1" to R.drawable.lesson_pic_chatul,
                "w_ח_2" to R.drawable.lesson_pic_chalav,
                "w_ח_3" to R.drawable.lesson_pic_chalon,
                "w_ח_4" to R.drawable.lesson_pic_chultza,
                "w_ט_1" to R.drawable.lesson_pic_tost,
                "w_ט_2" to R.drawable.lesson_pic_telefon,
                "w_ט_3" to R.drawable.lesson_pic_tigris,
                "w_כ_1" to R.drawable.lesson_pic_kise,
                "w_כ_2" to R.drawable.lesson_pic_kelev,
                "w_כ_3" to R.drawable.lesson_pic_kadur,
                "w_פ_1" to R.drawable.lesson_pic_perach,
                "w_פ_2" to R.drawable.lesson_pic_pil,
                "w_פ_3" to R.drawable.lesson_pic_pilpel,
                "w_פ_4" to R.drawable.lesson_pic_parpar,
                "w_ת_1" to R.drawable.lesson_pic_tapuach,
                "w_ת_2" to R.drawable.lesson_pic_tik,
                "w_ת_3" to R.drawable.lesson_pic_tinok,
                "w_ת_4" to R.drawable.lesson_pic_tof,
                // Chapter 5
                "w_נ_1" to R.drawable.lesson_pic_nemala,
                "w_נ_2" to R.drawable.lesson_pic_ner,
                "w_נ_3" to R.drawable.lesson_pic_naalayim,
                "w_נ_4" to R.drawable.lesson_pic_nachash,
                "w_צ_1" to R.drawable.lesson_pic_tzipor,
                "w_צ_2" to R.drawable.lesson_pic_tzav,
                "w_צ_3" to R.drawable.lesson_pic_tzfardea,
                "w_צ_4" to R.drawable.lesson_pic_tzalachat,
                "w_ק_1" to R.drawable.lesson_pic_kof,
                "w_ק_2" to R.drawable.lesson_pic_kubia,
                "w_ק_3" to R.drawable.lesson_pic_katar,
            )
        for ((id, drawableRes) in expected) {
            val entry = LessonWordCatalog.entries.single { it.id == id }
            assertEquals("tileRes for $id", drawableRes, entry.tileRes)
            assertNotEquals(R.drawable.lesson_pic_placeholder, entry.tileRes)
            assertTrue(
                "raster registry: $id",
                RasterLessonPicDrawables.isPngTile(entry.tileRes),
            )
            assertTrue("drawable res id for $id", drawableRes != 0)
        }
    }

    @Test
    fun pilpel_catalogSpelling_unchanged() {
        val entry = LessonWordCatalog.entries.single { it.id == "w_פ_3" }
        assertEquals("פילפל", entry.word)
        assertEquals(R.drawable.lesson_pic_pilpel, entry.tileRes)
    }
}
