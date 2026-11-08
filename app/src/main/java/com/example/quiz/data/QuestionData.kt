package com.example.quiz.data

import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import com.example.quiz.domain.Question

data class QuestionData(
    val id: Long?,
    val topicId: Long?,
    val level: Int?,
    val type: QuestionDataType?,
    val text: String?,
    val options: List<String>? = null,
    val correctAnswer: String? = null,
    val pairs: Map<String, String>? = null
) {

    fun toDomain(): Question {
        val id =
            requireNotNull(id) {
                "У вопроса отсутствует id"
            }

        val topicId =
            requireNotNull(topicId) {
                "У вопроса $id отсутствует topicId"
            }

        val level =
            requireNotNull(level) {
                "У вопроса $id отсутствует level"
            }

        val text =
            requireNotNull(text) {
                "У вопроса $id отсутствует text"
            }

        return when (
            requireNotNull(type) {
                "У вопроса $id отсутствует type"
            }
        ) {
            QuestionDataType.TEST ->
                MultipleListQ(
                    id = id,
                    topicId = topicId,
                    level = level,
                    text = text,
                    options =
                        requireNotNull(options) {
                            "У тестового вопроса $id отсутствуют варианты"
                        },
                    correctAnswer =
                        requireNotNull(correctAnswer) {
                            "У тестового вопроса $id отсутствует правильный ответ"
                        }
                )

            QuestionDataType.OPEN ->
                OpenQ(
                    id = id,
                    topicId = topicId,
                    level = level,
                    text = text,
                    correctAnswer =
                        requireNotNull(correctAnswer) {
                            "У открытого вопроса $id отсутствует правильный ответ"
                        }
                )

            QuestionDataType.MATCHING ->
                MatchParQuestion(
                    id = id,
                    topicId = topicId,
                    level = level,
                    text = text,
                    pairs =
                        requireNotNull(pairs) {
                            "У вопроса $id отсутствуют пары"
                        }
                )
        }
    }

    companion object {

        fun fromDomain(
            question: Question
        ): QuestionData {
            return when (question) {
                is MultipleListQ ->
                    QuestionData(
                        id = question.id,
                        topicId = question.topicId,
                        level = question.level,
                        type = QuestionDataType.TEST,
                        text = question.text,
                        options = question.options,
                        correctAnswer = question.correctAnswer
                    )

                is OpenQ ->
                    QuestionData(
                        id = question.id,
                        topicId = question.topicId,
                        level = question.level,
                        type = QuestionDataType.OPEN,
                        text = question.text,
                        correctAnswer = question.correctAnswer
                    )

                is MatchParQuestion ->
                    QuestionData(
                        id = question.id,
                        topicId = question.topicId,
                        level = question.level,
                        type = QuestionDataType.MATCHING,
                        text = question.text,
                        pairs = question.pairs
                    )
            }
        }
    }
}