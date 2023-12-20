package com.sph.diagnostics.dji

import android.content.Context

class Application: android.app.Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        com.secneo.sdk.Helper.install(this)
    }
}