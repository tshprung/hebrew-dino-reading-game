package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * End-to-end [LevelSession] runs for every playable Season 2 station (chapters 1–7).
 * Memory-match screens are excluded — they use a dedicated screen with no quiz plan.
 */
class Season2LevelSessionMatrixTest {
    @Test
    fun playableStationCount_isForty() {
        assertEquals(40, Season2LevelSessionTestSupport.playableStations().size)
    }

    @Test
    fun allPlayableSeason2Stations_completeFullRun() {
        for ((chapter, station) in Season2LevelSessionTestSupport.playableStations()) {
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter1_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 1).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter2_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 2).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter3_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 3).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter4_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 4).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter5_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 5).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter6_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 6).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }

    @Test
    fun chapter7_stations_completeFullRun() {
        Season2LevelSessionTestSupport.playableStations(chapter = 7).forEach { (chapter, station) ->
            Season2LevelSessionTestSupport.runFullStation(chapter, station)
        }
    }
}
