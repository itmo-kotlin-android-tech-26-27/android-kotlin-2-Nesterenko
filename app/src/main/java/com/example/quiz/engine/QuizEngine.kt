package com.example.quiz.engine

import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import com.example.quiz.domain.Question
import com.example.quiz.domain.TestSettings
import com.example.quiz.domain.Topic
import kotlin.math.abs

class QuizEngine(
    private val topic: Topic,
    private val questions: List<Question>,
    private val settings: TestSettings,
    private val sharedDeadlineMillis: Long? = null
) {

    private val usedQuestionIds =
        mutableSetOf<Long>()

    private val deadlineMillis =
        sharedDeadlineMillis
            ?: System.currentTimeMillis() +
            settings.timeLimitMinutes * 60_000L

    var currentLevel: Int = 1
        private set

    var correctAnswers: Int = 0
        private set

    var wrongAnswers: Int = 0
        private set

    var state: QuizState =
        QuizState.Running
        private set

    val answeredCount: Int
        get() =
            correctAnswers + wrongAnswers

    val isFinished: Boolean
        get() =
            when (state) {
                QuizState.Running,
                is QuizState.QuestionReady ->
                    false

                else ->
                    true
            }

    val remainingSeconds: Long
        get() {
            val remaining =
                deadlineMillis -
                        System.currentTimeMillis()

            return if (remaining <= 0) {
                0
            } else {
                (remaining + 999) / 1000
            }
        }

    fun nextQuestion() {
        updateTimeState()

        if (isFinished) {
            return
        }

        if (state is QuizState.QuestionReady) {
            return
        }

        if (
            answeredCount >=
            settings.maxQuestionsPerTopic
        ) {
            state =
                QuizState.QuestionLimitReached
            return
        }

        val unusedQuestions =
            questions.filter { question ->
                question.id !in usedQuestionIds
            }

        if (unusedQuestions.isEmpty()) {
            state =
                QuizState.NoQuestionsLeft
            return
        }

        val question =
            unusedQuestions
                .firstOrNull { question ->
                    question.level == currentLevel
                }
                ?: unusedQuestions.minBy { question ->
                    abs(
                        question.level -
                                currentLevel
                    )
                }

        currentLevel =
            question.level

        usedQuestionIds.add(
            question.id
        )

        state =
            QuizState.QuestionReady(
                question
            )
    }

    fun answer(
        input: String
    ): Boolean {
        updateTimeState()

        if (
            state ==
            QuizState.TimeExpired
        ) {
            throw IllegalStateException(
                "Время вышло"
            )
        }

        val questionState =
            state as? QuizState.QuestionReady
                ?: throw IllegalStateException(
                    "Нет активного вопроса"
                )

        val question =
            questionState.question

        val isCorrect =
            when (question) {
                is MultipleListQ ->
                    checkMultipleListAnswer(
                        question,
                        input
                    )

                is OpenQ ->
                    checkOpenAnswer(
                        question,
                        input
                    )

                is MatchParQuestion ->
                    checkMatchingAnswer(
                        question,
                        input
                    )
            }

        registerResult(
            isCorrect = isCorrect,
            questionLevel = question.level
        )

        return isCorrect
    }

    fun markTimeExpired() {
        if (!isFinished) {
            state =
                QuizState.TimeExpired
        }
    }

    fun interrupt() {
        if (!isFinished) {
            state =
                QuizState.Interrupted
        }
    }

    private fun updateTimeState() {
        if (
            !isFinished &&
            remainingSeconds <= 0
        ) {
            state =
                QuizState.TimeExpired
        }
    }

    private fun registerResult(
        isCorrect: Boolean,
        questionLevel: Int
    ) {
        if (isCorrect) {
            correctAnswers++

            if (
                questionLevel ==
                topic.maxLevel
            ) {
                state =
                    QuizState.Passed
                return
            }

            currentLevel =
                minOf(
                    questionLevel + 1,
                    topic.maxLevel
                )
        } else {
            wrongAnswers++

            currentLevel =
                maxOf(
                    questionLevel - 1,
                    1
                )
        }

        state =
            if (
                answeredCount >=
                settings.maxQuestionsPerTopic
            ) {
                QuizState.QuestionLimitReached
            } else {
                QuizState.Running
            }
    }

    private fun checkMultipleListAnswer(
        question: MultipleListQ,
        input: String
    ): Boolean {
        val number =
            input
                .trim()
                .toIntOrNull()
                ?: throw IllegalArgumentException(
                    "Введите номер ответа"
                )

        if (
            number !in
            1..question.options.size
        ) {
            throw IllegalArgumentException(
                "Такого варианта ответа нет"
            )
        }

        val studentAnswer =
            question.options[
                number - 1
            ]

        return studentAnswer ==
                question.correctAnswer
    }

    private fun checkOpenAnswer(
        question: OpenQ,
        input: String
    ): Boolean {
        val studentAnswer =
            normalizeAnswer(
                input
            )

        val correctAnswer =
            normalizeAnswer(
                question.correctAnswer
            )

        return studentAnswer ==
                correctAnswer
    }

    private fun checkMatchingAnswer(
        question: MatchParQuestion,
        input: String
    ): Boolean {
        val leftItems =
            question.pairs
                .keys
                .toList()

        val rightItems =
            question.pairs
                .values
                .toList()

        val enteredPairs =
            input
                .trim()
                .split(
                    Regex("\\s+")
                )

        if (
            enteredPairs.size !=
            question.pairs.size
        ) {
            return false
        }

        val studentPairs =
            mutableMapOf<String, String>()

        for (pair in enteredPairs) {
            val parts =
                pair.split("-")

            if (parts.size != 2) {
                return false
            }

            val leftNumber =
                parts[0]
                    .toIntOrNull()
                    ?: return false

            val rightLetter =
                parts[1]
                    .uppercase()
                    .firstOrNull()
                    ?: return false

            if (
                leftNumber !in
                1..leftItems.size
            ) {
                return false
            }

            val rightIndex =
                rightLetter - 'A'

            if (
                rightIndex !in
                rightItems.indices
            ) {
                return false
            }

            val leftItem =
                leftItems[
                    leftNumber - 1
                ]

            val rightItem =
                rightItems[
                    rightIndex
                ]

            studentPairs[leftItem] =
                rightItem
        }

        return studentPairs ==
                question.pairs
    }

    private fun normalizeAnswer(
        answer: String
    ): String {
        return answer
            .trim()
            .lowercase()
            .replace(
                Regex("\\s+"),
                " "
            )
    }
}