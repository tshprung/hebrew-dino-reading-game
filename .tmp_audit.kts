import com.tal.hebrewdino.ui.domain.*

fun eligible(ids: List<String>): List<String> =
    ids.filter { DragStationGenerators.isValidForDragMissingLetter(it, 0) }
        .filter { Season2StationContentValidator.wordAssetCheck(it)?.isValid == true }

val stations = listOf(
    1 to 6, 2 to 5, 3 to 3, 4 to 4, 5 to 3, 6 to 3, 7 to 2
)
val pools = mapOf(
    1 to Season2ChapterContent.ch1Words,
    2 to Season2ChapterContent.ch2Words,
    3 to Season2ChapterContent.ch3Words,
    4 to Season2ChapterContent.ch4Words,
    5 to Season2ChapterContent.ch5Words,
    6 to Season2ChapterContent.ch6Words,
    7 to Season2ChapterContent.ch7Words,
)
println("=== Eligible counts ===")
stations.forEach { (ch, st) ->
    val e = eligible(pools[ch]!!)
    println("Ch$ch st$st: ${e.size} eligible / ${pools[ch]!!.size} total")
}
println("\n=== Pairwise overlaps (eligible words) ===")
stations.forEachIndexed { i, (chA, stA) ->
    val setA = eligible(pools[chA]!!).toSet()
    stations.drop(i + 1).forEach { (chB, stB) ->
        val setB = eligible(pools[chB]!!).toSet()
        val overlap = setA.intersect(setB)
        if (overlap.isNotEmpty()) {
            val words = overlap.mapNotNull { id -> LessonWordCatalog.entries.find { it.id == id }?.word }
            println("Ch$chA st$stA vs Ch$chB st$stB: ${overlap.size} -> $words")
        }
    }
}
