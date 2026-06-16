package com.tal.hebrewdino.test

import java.io.File

/** Reads a repo source file from JVM unit tests (works from module or repo root). */
object ProjectSource {
    fun read(relativePath: String): String {
        val candidates =
            listOf(
                File(relativePath),
                File("../$relativePath"),
                File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
