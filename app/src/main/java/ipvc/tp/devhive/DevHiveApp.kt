package ipvc.tp.devhive

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import ipvc.tp.devhive.presentation.util.LocaleHelper

@HiltAndroidApp
class DevHiveApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}
