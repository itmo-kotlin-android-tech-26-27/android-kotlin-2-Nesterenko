package com.example.quiz.data

import com.example.quiz.domain.AnswerRecord

data class AnswerRecordData(
    val questionId: Long,
    val questionText: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timedOut: Boolean,
    val topicId: Long,
    val topicName: String
) {

    fun toDomain(): AnswerRecord {
        return AnswerRecord(
            questionId = questionId,
            questionText = questionText,
            studentAnswer = studentAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            timedOut = timedOut,
            topicId = topicId,
            topicName = topicName
        )
    }

    companion object {

        fun fromDomain(
            answer: AnswerRecord
        ): AnswerRecordData {
            return AnswerRecordData(
                questionId = answer.questionId,
                questionText = answer.questionText,
                studentAnswer = answer.studentAnswer,
                correctAnswer = answer.correctAnswer,
                isCorrect = answer.isCorrect,
                timedOut = answer.timedOut,
                topicId = answer.topicId,
                topicName = answer.topicName
            )
        }
    }
}