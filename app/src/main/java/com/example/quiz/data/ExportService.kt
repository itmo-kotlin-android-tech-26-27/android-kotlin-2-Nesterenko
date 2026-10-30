package com.example.quiz.data

import com.example.quiz.domain.AnswerRecord
import com.example.quiz.domain.Attempt
import com.example.quiz.domain.TopicAttemptResult
import com.google.gson.GsonBuilder
import java.io.File

class ExportService(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository
) {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

    fun exportAll(
        file: File
    ): File {
        file.parentFile?.mkdirs()

        val exportData =
            ExportData(
                settings =
                    questionRepository.getSettings(),

                topics =
                    questionRepository.getTopics(),

                questions =
                    questionRepository
                        .getQuestions()
                        .map { question ->
                            QuestionData.fromDomain(
                                question
                            )
                        },

                attempts =
                    attemptRepository
                        .loadAttempts()
                        .map { attempt ->
                            attempt.toExportAttempt()
                        }
            )

        file.writeText(
            gson.toJson(exportData)
        )

        return file
    }

    private fun Attempt.toExportAttempt():
            ExportAttempt {
        return ExportAttempt(
            studentLogin = studentLogin,
            topicId = topicId,
            topicName = topicName,
            startedAt = startedAt.toString(),
            finishedAt =
                finishedAt?.toString(),
            status = status,
            score = score,
            correctAnswers =
                correctAnswers,
            wrongAnswers =
                wrongAnswers,
            topicResults =
                getTopicResults()
                    .map { topicResult ->
                        topicResult
                            .toExportTopicResult()
                    },
            answers =
                answers.map { answer ->
                    answer.toExportAnswer()
                }
        )
    }

    private fun TopicAttemptResult
            .toExportTopicResult():
            ExportTopicResult {
        return ExportTopicResult(
            topicId = topicId,
            topicName = topicName,
            score = score,
            correctAnswers =
                correctAnswers,
            wrongAnswers =
                wrongAnswers,
            timeExpiredAnswers =
                timeExpiredAnswers,
            answeredCount =
                answeredCount
        )
    }

    private fun AnswerRecord.toExportAnswer():
            ExportAnswer {
        return ExportAnswer(
            topicId = topicId,
            topicName = topicName,
            questionId = questionId,
            questionText = questionText,
            studentAnswer = studentAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            timedOut = timedOut
        )
    }
}