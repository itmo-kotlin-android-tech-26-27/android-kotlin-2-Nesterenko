package com.example.quiz.domain

data class AnswerRecord(
    val questionId: Long,
    val questionText: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timedOut: Boolean = false,
    val topicId: Long,
    val topicName: String
)