package com.example.quiz.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quiz.data.AttemptRepository
import com.example.quiz.data.QuestionRepository

class QuizViewModelFactory(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val studentLogin: String,
    private val topicId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(
                questionRepository = questionRepository,
                attemptRepository = attemptRepository,
                studentLogin = studentLogin,
                topicId = topicId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
