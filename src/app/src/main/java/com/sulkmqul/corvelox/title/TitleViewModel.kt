package com.sulkmqul.corvelox.title

import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.CorveloxViewId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TitleViewModel @Inject constructor(
    private val eventService: CorveloxEventService
) : ViewModel() {

    public fun navigate(viewId: CorveloxViewId) {
        eventService.changeView(viewId)
    }

}