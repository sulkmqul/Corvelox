package com.sulkmqul.corvelox.textlerning

import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.CorveloxEventService
import com.sulkmqul.corvelox.data.text.TextDataService
import com.sulkmqul.corvelox.data.text.TextTrainingService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
public class TextLearningViewModel @Inject constructor(
    public val textTrainingService: TextTrainingService,
    private val eventService: CorveloxEventService
): ViewModel() {

}