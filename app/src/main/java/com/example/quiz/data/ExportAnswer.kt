package com.example.quiz.data

data class ExportAnswer(
    val topicId: Long,
    val topicName: String,
    val questionId: Long,
    val questionText: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timedOut: Boolean
)