package com.tal.hebrewdino.ui

import android.content.Context
import android.content.ContextWrapper

class TestAppContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}
