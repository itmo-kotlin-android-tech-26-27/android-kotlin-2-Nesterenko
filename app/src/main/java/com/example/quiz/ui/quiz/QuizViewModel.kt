
package com.example.quiz.ui.quiz

import androidx.lifecycle.ViewModel
import com.example.quiz.data.AttemptRepository
import com.example.quiz.data.QuestionRepository
import com.example.quiz.domain.AnswerRecord
import com.example.quiz.domain.Attempt
import com.example.quiz.domain.AttemptStatus
import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import com.example.quiz.domain.Question
import com.example.quiz.engine.QuizEngine
import com.example.quiz.engine.QuizState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizViewModel(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val studentLogin: String,
    private val topicId: Long
) : ViewModel() {

    private var engine: QuizEngine? = null
    private var attempt: Attempt? = null

    private val _uiState = MutableStateFlow(
        QuizUiState()
    )

    val uiState: StateFlow<QuizUiState> =
        _uiState.asStateFlow()

    init {
        startQuiz()
    }

    private fun startQuiz() {
        val topic = questionRepository
            .getTopics()
            .firstOrNull { it.id == topicId }

        if (topic == null) {
            _uiState.value = QuizUiState(
                studentLogin = studentLogin,
                topicId = topicId,
                errorMessage = "Тема не найдена"
            )
            return
        }

        val questions = questionRepository
            .getQuestions()
            .filter { it.topicId == topic.id }

        if (questions.isEmpty()) {
            _uiState.value = QuizUiState(
                studentLogin = studentLogin,
                topicId = topic.id,
                topicName = topic.name,
                maxLevel = topic.maxLevel,
                errorMessage = "В этой теме нет вопросов"
            )
            return
        }

        val settings = questionRepository.getSettings()

        engine = QuizEngine(
            topic = topic,
            questions = questions,
            settings = settings
        )

        attempt = Attempt(
            studentLogin = studentLogin,
            topicId = topic.id,
            topicName = topic.name
        )

        _uiState.value = QuizUiState(
            studentLogin = studentLogin,
            topicId = topic.id,
            topicName = topic.name,
            currentLevel = 1,
            maxLevel = topic.maxLevel,
            remainingSeconds = settings.timeLimitMinutes * 60L
        )

        loadNextQuestion()
    }

    private fun loadNextQuestion() {
        val currentEngine = engine ?: return

        currentEngine.nextQuestion()

        when (val state = currentEngine.state) {
            is QuizState.QuestionReady -> {
                _uiState.value = _uiState.value.copy(
                    currentQuestion = state.question,
                    currentLevel = currentEngine.currentLevel,
                    remainingSeconds = currentEngine.remainingSeconds,
                    answer = "",
                    isAnswerSubmitted = false,
                    errorMessage = null
                )
            }

            QuizState.Passed,
            QuizState.QuestionLimitReached,
            QuizState.NoQuestionsLeft -> {
                finishAttempt(AttemptStatus.COMPLETED)
            }

            QuizState.TimeExpired -> {
                recordTimedOutQuestion()
                finishAttempt(AttemptStatus.TIME_EXPIRED)
            }

            QuizState.Interrupted -> {
                finishAttempt(AttemptStatus.INTERRUPTED)
            }

            QuizState.Running -> Unit
        }
    }

    fun updateAnswer(answer: String) {
        if (_uiState.value.isFinished ||
            _uiState.value.isAnswerSubmitted
        ) {
            return
        }

        _uiState.value = _uiState.value.copy(
            answer = answer,
            errorMessage = null
        )
    }

    fun submitAnswer() {
        val state = _uiState.value

        if (state.isFinished || state.isAnswerSubmitted) {
            return
        }

        val currentEngine = engine ?: return
        val question = state.currentQuestion ?: return
        val selectedAnswer = state.answer.trim()

        if (selectedAnswer.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите ответ"
            )
            return
        }

        try {
            val engineAnswer = convertAnswerForEngine(
                question,
                selectedAnswer
            )

            val isCorrect = currentEngine.answer(engineAnswer)

            attempt?.addAnswer(
                AnswerRecord(
                    questionId = question.id,
                    questionText = question.text,
                    studentAnswer = selectedAnswer,
                    correctAnswer = correctAnswerText(question),
                    isCorrect = isCorrect,
                    topicId = question.topicId,
                    topicName = state.topicName
                )
            )

            when (currentEngine.state) {
                QuizState.Passed,
                QuizState.QuestionLimitReached,
                QuizState.NoQuestionsLeft -> {
                    finishAttempt(AttemptStatus.COMPLETED)
                }

                QuizState.Running -> {
                    _uiState.value = _uiState.value.copy(
                        isAnswerSubmitted = true,
                        remainingSeconds =
                            currentEngine.remainingSeconds,
                        errorMessage = null
                    )
                }

                else -> Unit
            }

        } catch (error: IllegalArgumentException) {
            _uiState.value = _uiState.value.copy(
                errorMessage =
                    error.message ?: "Некорректный ответ"
            )

        } catch (error: IllegalStateException) {
            if (currentEngine.state == QuizState.TimeExpired) {
                recordTimedOutQuestion()
                finishAttempt(AttemptStatus.TIME_EXPIRED)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage =
                        error.message ?: "Ошибка прохождения"
                )
            }
        }
    }

    fun nextQuestion() {
        if (!_uiState.value.isAnswerSubmitted ||
            _uiState.value.isFinished
        ) {
            return
        }

        val currentEngine = engine ?: return

        // Проверяем таймер до удаления текущего вопроса.
        if (currentEngine.remainingSeconds <= 0L) {
            currentEngine.markTimeExpired()
            finishAttempt(AttemptStatus.TIME_EXPIRED)
            return
        }

        _uiState.value = _uiState.value.copy(
            currentQuestion = null,
            answer = "",
            isAnswerSubmitted = false,
            errorMessage = null
        )

        loadNextQuestion()
    }

    fun interruptQuiz() {
        engine?.interrupt()

        if (attempt != null) {
            finishAttempt(AttemptStatus.INTERRUPTED)
        }
    }

    fun canLeaveWithoutSaving(): Boolean {
        return attempt == null
    }

    fun refreshTime() {
        val currentEngine = engine ?: return

        if (currentEngine.isFinished ||
            _uiState.value.isFinished
        ) {
            return
        }

        val remaining = currentEngine.remainingSeconds

        if (remaining <= 0L) {
            currentEngine.markTimeExpired()
            recordTimedOutQuestion()
            finishAttempt(AttemptStatus.TIME_EXPIRED)
        } else {
            _uiState.value = _uiState.value.copy(
                remainingSeconds = remaining
            )
        }
    }

    private fun recordTimedOutQuestion() {
        val question = _uiState.value.currentQuestion ?: return
        val currentAttempt = attempt ?: return

        if (currentAttempt.answers.any {
                it.questionId == question.id
            }
        ) {
            return
        }

        currentAttempt.addAnswer(
            AnswerRecord(
                questionId = question.id,
                questionText = question.text,
                studentAnswer = "",
                correctAnswer = correctAnswerText(question),
                isCorrect = false,
                timedOut = true,
                topicId = question.topicId,
                topicName = _uiState.value.topicName
            )
        )
    }

    private fun finishAttempt(status: AttemptStatus) {
        val currentAttempt = attempt ?: return

        if (_uiState.value.savedAttemptIndex != null) {
            return
        }

        if (currentAttempt.status == AttemptStatus.IN_PROGRESS) {
            currentAttempt.finish(status)
        }

        try {
            val newIndex = attemptRepository.loadAttempts().size
            attemptRepository.addAttempt(currentAttempt)

            _uiState.value = _uiState.value.copy(
                isFinished = true,
                isInterrupted =
                    currentAttempt.status == AttemptStatus.INTERRUPTED,
                isTimeExpired =
                    currentAttempt.status == AttemptStatus.TIME_EXPIRED,
                remainingSeconds =
                    if (currentAttempt.status == AttemptStatus.TIME_EXPIRED) {
                        0L
                    } else {
                        _uiState.value.remainingSeconds
                    },
                currentQuestion = null,
                answer = "",
                isAnswerSubmitted = false,
                savedAttemptIndex = newIndex,
                errorMessage = null
            )

        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage =
                    "Не удалось сохранить попытку: " +
                            (error.message ?: "ошибка файла")
            )
        }
    }

    private fun convertAnswerForEngine(
        question: Question,
        answer: String
    ): String {
        return when (question) {
            is MultipleListQ -> {
                val index = question.options.indexOf(answer)

                if (index < 0) {
                    throw IllegalArgumentException(
                        "Выберите вариант ответа"
                    )
                }

                (index + 1).toString()
            }

            is OpenQ -> answer

            is MatchParQuestion -> answer
                .replace(Regex("\\s*,\\s*"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }

    private fun correctAnswerText(
        question: Question
    ): String {
        return when (question) {
            is MultipleListQ -> question.correctAnswer
            is OpenQ -> question.correctAnswer

            is MatchParQuestion ->
                question.pairs.entries.joinToString(", ") {
                    "${it.key} — ${it.value}"
                }
        }
    }
}
