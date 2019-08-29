package com.example.backup_befearless

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class MultiDexAppliction : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}