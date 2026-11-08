package com.example.quiz.engine

import com.example.quiz.domain.Question

sealed interface QuizState {

    data object Running : QuizState

    data class QuestionReady(
        val question: Question
    ) : QuizState

    data object Passed : QuizState

    data object QuestionLimitReached : QuizState

    data object NoQuestionsLeft : QuizState

    data object TimeExpired : QuizState

    data object Interrupted : QuizState
}