package com.sulkmqul.corvelox.data.text

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
public class TextTrainingService @Inject constructor() {

    /**
     * 今回の学習一覧
     */
    private var trainingSrcList: List<TextData> = listOf()
    public val trainingList: List<TextData>
        get() = trainingSrcList

    /**
     * 現在の状況
     */
    public var currentIndex = 0



    public fun startTraining(srcList: List<TextData>, startIndex: Int = 0) {
        trainingSrcList = srcList
        currentIndex = startIndex
    }
}