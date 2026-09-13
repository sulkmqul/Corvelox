package com.sulkmqul.corvelox.title

import android.util.Printer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.CorveloxViewId
import com.sulkmqul.corvelox.data.LearningHistoryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TitleViewModel @Inject constructor(
    private val eventService: CorveloxEventService,
    private val historyService: LearningHistoryService
) : ViewModel() {

    public fun navigate(viewId: CorveloxViewId) {
        eventService.changeView(viewId)
    }

    public fun clearHistory() {
        viewModelScope.launch {
            historyService.clearShelfHistory()
        }
    }

    public fun clearBookmarkWord() {
        viewModelScope.launch {
            historyService.clearBookmarkWord()
        }
    }
}