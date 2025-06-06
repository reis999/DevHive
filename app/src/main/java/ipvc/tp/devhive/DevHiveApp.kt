package ipvc.tp.devhive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DevHiveApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
