package com.tal.hebrewdino.ui.audio.routing

sealed interface AudioSource {
    data class Asset(val path: String) : AudioSource

    data class RawRes(val resId: Int) : AudioSource

}
