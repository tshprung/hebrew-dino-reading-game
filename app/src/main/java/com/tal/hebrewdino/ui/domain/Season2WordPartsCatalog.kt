package com.tal.hebrewdino.ui.domain



/**

 * Curated first-part + second-part splits for Season 2 word-parts stations.

 * Each row must match [LessonWordCatalog] [word] exactly.

 */

data class Season2WordPartsEntry(

    val catalogId: String,

    val firstPart: String,

    val secondPart: String,

)



object Season2WordPartsCatalog {

    /** Prefer clearer two-part examples; avoid very short second parts in guided/hidden ramps. */

    private val guidedPreferredIds: Set<String> =

        setOf("w_ג_1", "w_ג_3", "w_פ_2", "w_ח_3", "w_ח_2", "w_ש_1", "w_ר_1", "w_ר_3")



    private val shortWordIds: Set<String> = setOf("w_נ_2", "w_צ_2", "w_ז_3")



    val curatedEntries: List<Season2WordPartsEntry> =

        listOf(

            Season2WordPartsEntry("w_ש_1", "ש", "מש"),

            Season2WordPartsEntry("w_ז_3", "ז", "חל"),

            Season2WordPartsEntry("w_ס_4", "ס", "פר"),

            Season2WordPartsEntry("w_ב_2", "ב", "לון"),

            Season2WordPartsEntry("w_ח_3", "ח", "לון"),

            Season2WordPartsEntry("w_ח_2", "ח", "לב"),

            Season2WordPartsEntry("w_ג_1", "ג", "מל"),

            Season2WordPartsEntry("w_נ_2", "נ", "ר"),

            Season2WordPartsEntry("w_פ_2", "פי", "ל"),

            Season2WordPartsEntry("w_צ_2", "צ", "ב"),

            Season2WordPartsEntry("w_ג_3", "ג", "דר"),

            Season2WordPartsEntry("w_ר_1", "רא", "ש"),

            Season2WordPartsEntry("w_ר_3", "ר", "גל"),

        )



    fun entriesForWordIds(wordCatalogIds: List<String>): List<Season2WordPartsEntry> {

        val allowed = wordCatalogIds.toSet()

        return curatedEntries.filter { spec ->

            spec.catalogId in allowed &&

                Season2StationContentValidator.validateWordPartsEntry(spec).isEmpty()

        }

    }



    fun entriesForPresentationMode(

        wordCatalogIds: List<String>,

        mode: Season2WordPartsPresentationMode,

    ): List<Season2WordPartsEntry> {

        val base = entriesForWordIds(wordCatalogIds)

        return when (mode) {

            Season2WordPartsPresentationMode.VisibleWordParts ->

                base.filter { it.catalogId in guidedPreferredIds }

            Season2WordPartsPresentationMode.GuidedWordParts,

            Season2WordPartsPresentationMode.HiddenWordPartsChallenge,

            ->

                base.filter { it.catalogId in guidedPreferredIds && it.catalogId !in shortWordIds }

        }.ifEmpty { base }

    }



    fun validatedEntries(): List<Season2WordPartsEntry> =

        curatedEntries.filter { Season2StationContentValidator.validateWordPartsEntry(it).isEmpty() }

}


