package com.insomnia.diary

import android.app.Application
import com.insomnia.diary.data.db.InsomniaDatabase

class InsomniaApp : Application() {
    val database by lazy { InsomniaDatabase.getInstance(this) }
}
