package com.example.quiz.data

import com.example.quiz.domain.TestSettings
import com.example.quiz.domain.Topic

data class QuizData(
    val settings: TestSettings,
    val topics: List<Topic>,
    val questions: List<QuestionData>
)