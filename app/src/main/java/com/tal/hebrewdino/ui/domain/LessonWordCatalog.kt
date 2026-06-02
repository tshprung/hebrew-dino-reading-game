package com.tal.hebrewdino.ui.domain

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.tal.hebrewdino.R
import kotlin.random.Random

/**
 * Authoritative picture↔word↔letter rows for picture-first stations.
 * [letter] is the pedagogical first visible print letter taught for [word] (stored explicitly).
 *
 * [tintArgb] + [tileRes] drive card art; default [R.drawable.lesson_pic_placeholder] is replaced in UI by a word illustration.
 */
data class LessonWordEntry(
    val id: String,
    val letter: String,
    val word: String,
    @param:ColorInt val tintArgb: Int,
    @param:DrawableRes val tileRes: Int = R.drawable.lesson_pic_placeholder,
)

/**
 * Frozen catalog: validated once at class load (fail fast in debug if corrupted).
 */
object LessonWordCatalog {
    /** אוטו / מכונית / רכב: same card tint + same placeholder art (see [validateCatalog] for allowed tint sharing). */
    private val carSynonymCardTint = 0xFFFFE0B2.toInt()

    val entries: List<LessonWordEntry> =
        listOf(
            LessonWordEntry("w_א_1", "א", "איש", 0xFFB3E5FC.toInt(), tileRes = R.drawable.lesson_pic_ish),
            LessonWordEntry("w_א_2", "א", "אריה", 0xFFFFF9C4.toInt(), tileRes = R.drawable.lesson_pic_aryeh),
            LessonWordEntry("w_א_3", "א", "אבטיח", 0xFFC8E6C9.toInt(), tileRes = R.drawable.lesson_pic_avatiach),
            LessonWordEntry("w_א_4", "א", "אוטו", carSynonymCardTint, tileRes = R.drawable.lesson_pic_car),
            LessonWordEntry("w_א_5", "א", "ארנב", 0xFF9CCC65.toInt(), tileRes = R.drawable.lesson_pic_arnav),
            LessonWordEntry("w_ב_1", "ב", "בית", 0xFFE1BEE7.toInt(), tileRes = R.drawable.lesson_pic_bait),
            LessonWordEntry("w_ב_2", "ב", "בלון", 0xFFFFCDD2.toInt(), tileRes = R.drawable.lesson_pic_balloon),
            LessonWordEntry("w_ב_3", "ב", "ברווז", 0xFFB2EBF2.toInt(), tileRes = R.drawable.lesson_pic_barvaz),
            LessonWordEntry("w_ג_1", "ג", "גמל", 0xFF4DB6AC.toInt(), tileRes = R.drawable.lesson_pic_gamal),
            LessonWordEntry("w_ג_2", "ג", "גלידה", 0xFFFFD740.toInt(), tileRes = R.drawable.lesson_pic_glida),
            LessonWordEntry("w_ג_3", "ג", "גדר", 0xFF8BC34A.toInt(), tileRes = R.drawable.lesson_pic_gader),
            LessonWordEntry("w_ג_4", "ג", "ג'ירפה", 0xFFFFA726.toInt(), tileRes = R.drawable.lesson_pic_girafa),
            LessonWordEntry("w_ד_1", "ד", "דג", 0xFF81D4FA.toInt(), tileRes = R.drawable.lesson_pic_dag),
            LessonWordEntry("w_ד_2", "ד", "דלת", 0xFFFFF59D.toInt(), tileRes = R.drawable.lesson_pic_delet),
            LessonWordEntry("w_ד_3", "ד", "דחליל", 0xFFD1C4E9.toInt(), tileRes = R.drawable.lesson_pic_dachlil),
            LessonWordEntry("w_ד_4", "ד", "דבש", 0xFFFFB300.toInt(), tileRes = R.drawable.lesson_pic_dvash),
            LessonWordEntry("w_ה_1", "ה", "הר", 0xFFA1887F.toInt(), tileRes = R.drawable.lesson_pic_har),
            LessonWordEntry("w_ה_2", "ה", "הפתעה", 0xFFFFF176.toInt(), tileRes = R.drawable.lesson_pic_haftaa),
            LessonWordEntry("w_ה_3", "ה", "היפופוטם", 0xFFBCAAA4.toInt(), tileRes = R.drawable.lesson_pic_hipopotam),
            LessonWordEntry("w_ו_1", "ו", "ורד", 0xFF80D8FF.toInt(), tileRes = R.drawable.lesson_pic_vered),
            // NOTE: Audio file naming is keyed by id (w_ו_2 -> word_w_vav_2.wav). Keep word↔audio aligned.
            LessonWordEntry("w_ו_2", "ו", "וופל", 0xFFFF8A65.toInt(), tileRes = R.drawable.lesson_pic_wafel),
            LessonWordEntry("w_ו_3", "ו", "וילון", 0xFF8E24AA.toInt(), tileRes = R.drawable.lesson_pic_vilon),
            LessonWordEntry("w_ח_1", "ח", "חתול", 0xFFE91E63.toInt(), tileRes = R.drawable.lesson_pic_chatul),
            LessonWordEntry("w_ח_2", "ח", "חלב", 0xFF5C6BC0.toInt(), tileRes = R.drawable.lesson_pic_chalav),
            LessonWordEntry("w_ח_3", "ח", "חלון", 0xFF42A5F5.toInt(), tileRes = R.drawable.lesson_pic_chalon),
            LessonWordEntry("w_ח_4", "ח", "חולצה", 0xFF039BE5.toInt(), tileRes = R.drawable.lesson_pic_chultza),
            LessonWordEntry("w_ט_1", "ט", "טוסט", 0xFF4DD0E1.toInt(), tileRes = R.drawable.lesson_pic_tost),
            LessonWordEntry("w_ט_2", "ט", "טלפון", 0xFFFF7043.toInt(), tileRes = R.drawable.lesson_pic_telefon),
            LessonWordEntry("w_ט_3", "ט", "טיגריס", 0xFF26C6DA.toInt(), tileRes = R.drawable.lesson_pic_tigris),
            LessonWordEntry("w_י_2", "י", "ילד", 0xFF64B5F6.toInt(), tileRes = R.drawable.lesson_pic_yeled),
            // Easier-to-draw yod words (Season 2 friendly).
            LessonWordEntry("w_י_3", "י", "יד", 0xFF6EC6D8.toInt(), tileRes = R.drawable.lesson_pic_yad),
            LessonWordEntry("w_י_4", "י", "יונה", 0xFF9FB7FF.toInt(), tileRes = R.drawable.lesson_pic_yona),
            LessonWordEntry("w_י_5", "י", "ירח", 0xFFF2B880.toInt(), tileRes = R.drawable.lesson_pic_yareach),
            LessonWordEntry("w_י_6", "י", "ינשוף", 0xFFB8A3D6.toInt(), tileRes = R.drawable.lesson_pic_yanshuf),
            // Season 2 Chapter 1 — raster art in res/drawable-nodpi/lesson_pic_*.png
            LessonWordEntry("w_ז_1", "ז", "זברה", 0xFF66B0FF.toInt(), tileRes = R.drawable.lesson_pic_zebra),
            LessonWordEntry("w_ז_2", "ז", "זיקית", 0xFF86D6A0.toInt(), tileRes = R.drawable.lesson_pic_zikit),
            LessonWordEntry("w_ז_3", "ז", "זחל", 0xFFD9C27E.toInt(), tileRes = R.drawable.lesson_pic_zakhal),
            LessonWordEntry("w_ז_4", "ז", "זר", 0xFFF2A3B6.toInt(), tileRes = R.drawable.lesson_pic_zer),
            LessonWordEntry("w_ס_1", "ס", "סוס", 0xFF7FD1C6.toInt(), tileRes = R.drawable.lesson_pic_sus),
            LessonWordEntry("w_ס_2", "ס", "סירה", 0xFF5AA7E6.toInt(), tileRes = R.drawable.lesson_pic_sira),
            LessonWordEntry("w_ס_3", "ס", "סוכריה", 0xFFFF9C7A.toInt(), tileRes = R.drawable.lesson_pic_sukariya),
            LessonWordEntry("w_ס_4", "ס", "ספר", 0xFFB6C0FF.toInt(), tileRes = R.drawable.lesson_pic_sefer),
            LessonWordEntry("w_ס_5", "ס", "סרטן", 0xFFFFCC6B.toInt(), tileRes = R.drawable.lesson_pic_sartan),
            LessonWordEntry("w_ע_1", "ע", "עין", 0xFFB5E07A.toInt(), tileRes = R.drawable.lesson_pic_ayin),
            LessonWordEntry("w_ע_2", "ע", "עגבניה", 0xFFEE7B67.toInt(), tileRes = R.drawable.lesson_pic_agvania),
            LessonWordEntry("w_ע_3", "ע", "עכבר", 0xFFC8B8A6.toInt(), tileRes = R.drawable.lesson_pic_akbar),
            LessonWordEntry("w_ע_4", "ע", "עוגה", 0xFFD8B3F0.toInt(), tileRes = R.drawable.lesson_pic_uga),
            LessonWordEntry("w_ע_5", "ע", "עץ", 0xFF6EBB6F.toInt(), tileRes = R.drawable.lesson_pic_etz),
            LessonWordEntry("w_ע_6", "ע", "עכביש", 0xFF9AA7B2.toInt(), tileRes = R.drawable.lesson_pic_akavish),
            LessonWordEntry("w_ע_7", "ע", "עלה", 0xFF8BD37E.toInt(), tileRes = R.drawable.lesson_pic_aleh),
            LessonWordEntry("w_כ_1", "כ", "כיסא", 0xFFFFD54F.toInt(), tileRes = R.drawable.lesson_pic_kise),
            LessonWordEntry("w_כ_2", "כ", "כלב", 0xFFC5E1A5.toInt(), tileRes = R.drawable.lesson_pic_kelev),
            LessonWordEntry("w_כ_3", "כ", "כדור", 0xFFAED581.toInt(), tileRes = R.drawable.lesson_pic_kadur),
            LessonWordEntry("w_ל_1", "ל", "לחם", 0xFFFFE082.toInt(), tileRes = R.drawable.lesson_pic_lechem),
            LessonWordEntry("w_ל_2", "ל", "לב", 0xFFF8BBD0.toInt(), tileRes = R.drawable.lesson_pic_lev),
            LessonWordEntry("w_ל_3", "ל", "לימון", 0xFFB2DFDB.toInt(), tileRes = R.drawable.lesson_pic_limon),
            // Car synonyms share the same picture + card background as [w_א_4] (אוטו).
            LessonWordEntry("w_מ_1", "מ", "מכונית", carSynonymCardTint, tileRes = R.drawable.lesson_pic_car),
            LessonWordEntry("w_מ_2", "מ", "מחבת", 0xFFDCEDC8.toInt(), tileRes = R.drawable.lesson_pic_machbat),
            LessonWordEntry("w_מ_3", "מ", "מדוזה", 0xFFC5CAE9.toInt(), tileRes = R.drawable.lesson_pic_medusa),
            LessonWordEntry("w_מ_4", "מ", "מיטה", 0xFFB4A196.toInt(), tileRes = R.drawable.lesson_pic_mitah),
            LessonWordEntry("w_מ_5", "מ", "מוצץ", 0xFFE53935.toInt(), tileRes = R.drawable.lesson_pic_motzetz),
            LessonWordEntry("w_נ_1", "נ", "נמלה", 0xFF9FA8DA.toInt(), tileRes = R.drawable.lesson_pic_nemala),
            LessonWordEntry("w_נ_2", "נ", "נר", 0xFFFFCC80.toInt(), tileRes = R.drawable.lesson_pic_ner),
            LessonWordEntry("w_נ_3", "נ", "נעליים", 0xFF80DEEA.toInt(), tileRes = R.drawable.lesson_pic_naalayim),
            LessonWordEntry("w_נ_4", "נ", "נחש", 0xFFE57373.toInt(), tileRes = R.drawable.lesson_pic_nachash),
            LessonWordEntry("w_פ_1", "פ", "פרח", 0xFFBA68C8.toInt(), tileRes = R.drawable.lesson_pic_perach),
            LessonWordEntry("w_פ_2", "פ", "פיל", 0xFF7986CB.toInt(), tileRes = R.drawable.lesson_pic_pil),
            LessonWordEntry("w_פ_3", "פ", "פילפל", 0xFFD32F2F.toInt(), tileRes = R.drawable.lesson_pic_pilpel),
            LessonWordEntry("w_פ_4", "פ", "פרפר", 0xFF9575CD.toInt(), tileRes = R.drawable.lesson_pic_parpar),
            LessonWordEntry("w_צ_1", "צ", "ציפור", 0xFFAB47BC.toInt(), tileRes = R.drawable.lesson_pic_tzipor),
            LessonWordEntry("w_צ_2", "צ", "צב", 0xFF78909C.toInt(), tileRes = R.drawable.lesson_pic_tzav),
            LessonWordEntry("w_צ_3", "צ", "צפרדע", 0xFF388E3C.toInt(), tileRes = R.drawable.lesson_pic_tzfardea),
            LessonWordEntry("w_צ_4", "צ", "צלחת", 0xFFB0BEC5.toInt(), tileRes = R.drawable.lesson_pic_tzalachat),
            LessonWordEntry("w_ק_1", "ק", "קוף", 0xFFFFAB40.toInt(), tileRes = R.drawable.lesson_pic_kof),
            LessonWordEntry("w_ק_2", "ק", "קוביה", 0xFF4FC3F7.toInt(), tileRes = R.drawable.lesson_pic_kubia),
            LessonWordEntry("w_ק_3", "ק", "קטר", 0xFF455A64.toInt(), tileRes = R.drawable.lesson_pic_katar),
            LessonWordEntry("w_ר_1", "ר", "ראש", 0xFFF48FB1.toInt(), tileRes = R.drawable.lesson_pic_rosh),
            // רכב: same car art + card tint as אוטו/מכונית (פרק 1).
            LessonWordEntry("w_ר_2", "ר", "רכב", carSynonymCardTint, tileRes = R.drawable.lesson_pic_car),
            LessonWordEntry("w_ר_3", "ר", "רגל", 0xFFFFC400.toInt(), tileRes = R.drawable.lesson_pic_regel),
            LessonWordEntry("w_ר_4", "ר", "רכבת", 0xFFD4E157.toInt(), tileRes = R.drawable.lesson_pic_rakevet),
            LessonWordEntry("w_ר_5", "ר", "רמזור", 0xFF00ACC1.toInt(), tileRes = R.drawable.lesson_pic_ramzor),
            LessonWordEntry("w_ש_1", "ש", "שמש", 0xFF90CAF9.toInt(), tileRes = R.drawable.lesson_pic_shemesh),
            LessonWordEntry("w_ש_2", "ש", "שולחן", 0xFFCE93D8.toInt(), tileRes = R.drawable.lesson_pic_shulchan),
            LessonWordEntry("w_ש_3", "ש", "שיניים", 0xFFECEFF1.toInt(), tileRes = R.drawable.lesson_pic_shinayim),
            LessonWordEntry("w_ש_4", "ש", "שעון", 0xFF29B6F6.toInt(), tileRes = R.drawable.lesson_pic_shaon),
            LessonWordEntry("w_ת_1", "ת", "תפוח", 0xFF80CBC4.toInt(), tileRes = R.drawable.lesson_pic_tapuach),
            LessonWordEntry("w_ת_2", "ת", "תיק", 0xFFFFE57E.toInt(), tileRes = R.drawable.lesson_pic_tik),
            LessonWordEntry("w_ת_3", "ת", "תינוק", 0xFFB39DDB.toInt(), tileRes = R.drawable.lesson_pic_tinok),
            LessonWordEntry("w_ת_4", "ת", "תוף", 0xFF26A69A.toInt(), tileRes = R.drawable.lesson_pic_tof),
        )

    init {
        validateCatalog(entries)
    }

    fun pickRandom(rnd: Random, letter: String, excludeIds: Set<String> = emptySet()): LessonWordEntry {
        val pool = entries.filter { it.letter == letter && it.id !in excludeIds }
        require(pool.isNotEmpty()) { "No catalog entry for letter=$letter" }
        return pool.random(rnd)
    }

    /** Image-match: cycle through different example words for the same letter before repeating. */
    fun pickRandomForImageMatch(rnd: Random, letter: String, excludeIds: Set<String>): LessonWordEntry {
        val pool = entries.filter { it.letter == letter && it.id !in excludeIds }
        if (pool.isNotEmpty()) return pool.random(rnd)
        return pickRandom(rnd, letter)
    }

    fun pickTwoDistinctForLetters(rnd: Random, letter1: String, letter2: String): Pair<LessonWordEntry, LessonWordEntry> {
        require(letter1 != letter2)
        val e1 = pickRandom(rnd, letter1)
        var e2 = pickRandom(rnd, letter2)
        var guard = 0
        while (e1.tintArgb == e2.tintArgb && guard++ < 8) {
            e2 = pickRandom(rnd, letter2, excludeIds = setOf(e2.id))
        }
        return e1 to e2
    }

    /** Package-private for tests; throws if invalid. */
    internal fun validateCatalog(list: List<LessonWordEntry>) {
        val ids = mutableSetOf<String>()
        for (e in list) {
            check(ids.add(e.id)) { "Duplicate lesson id: ${e.id}" }
            check(e.letter.isNotEmpty() && e.letter.length == 1) { "Bad letter for ${e.id}" }
            check(e.word.isNotEmpty()) { "Empty word for ${e.id}" }
            val first = e.word.first().toString()
            check(first == e.letter) { "Word first char mismatch: ${e.id} word=${e.word} letter=${e.letter} first=$first" }
        }
    }
}
