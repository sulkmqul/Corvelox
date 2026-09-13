package com.sulkmqul.corvelox.learning

import androidx.lifecycle.ViewModel
import com.sulkmqul.corvelox.data.QuestionData
import com.sulkmqul.corvelox.data.QuestionService
import com.sulkmqul.corvelox.data.Word
import com.sulkmqul.corvelox.data.WordBookService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
public class LearningViewModel @Inject constructor(
    private val questionService: QuestionService
): ViewModel() {

    /**
     * 今回の問題
     */
    private val questionStateFlow: MutableStateFlow<QuestionData?> = MutableStateFlow(null)
    public val questionState
        get() = questionStateFlow.asStateFlow()

    init {
        questionStateFlow.value = questionService.currentQuestion
    }
}