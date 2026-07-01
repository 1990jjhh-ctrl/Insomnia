package com.insomnia.diary.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.insomnia.diary.data.db.InsomniaDatabase
import com.insomnia.diary.data.repository.EveningRepository
import com.insomnia.diary.data.repository.MorningRepository
import com.insomnia.diary.domain.EveningProtocol
import com.insomnia.diary.domain.MorningProtocol
import kotlinx.coroutines.flow.Flow

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = InsomniaDatabase.getInstance(app)
    val latestMorning: Flow<MorningProtocol?> = MorningRepository(db).observeLatest()
    val latestEvening: Flow<EveningProtocol?> = EveningRepository(db).observeLatest()
}
