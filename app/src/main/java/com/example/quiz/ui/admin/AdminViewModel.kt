
package com.example.quiz.ui.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.AttemptRepository
import com.example.quiz.data.ExportService
import com.example.quiz.data.QuestionRepository
import com.example.quiz.domain.Attempt
import com.example.quiz.domain.TestSettings
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminUiState(
    val settings: TestSettings? = null,
    val attempts: List<IndexedValue<Attempt>> = emptyList(),
    val isBusy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

class AdminViewModel(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    context: Context
) : ViewModel() {

    private val appContext = context.applicationContext

    private val exportService = ExportService(
        questionRepository,
        attemptRepository
    )

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBusy = true,
                message = null
            )

            try {
                reloadData()
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    fun saveSettings(
        minutesText: String,
        questionsText: String
    ) {
        val minutes = minutesText.toIntOrNull()
        val questions = questionsText.toIntOrNull()

        if (minutes == null || minutes <= 0 ||
            questions == null || questions <= 0
        ) {
            _uiState.value = _uiState.value.copy(
                message = "Введите положительные целые числа",
                isError = true
            )
            return
        }

        runOperation("Настройки сохранены") {
            withContext(Dispatchers.IO) {
                questionRepository.updateSettings(
                    TestSettings(
                        timeLimitMinutes = minutes,
                        maxQuestionsPerTopic = questions
                    )
                )
            }
        }
    }

    fun deleteAttempt(index: Int) {
        runOperation("Попытка удалена") {
            withContext(Dispatchers.IO) {
                check(attemptRepository.deleteAttempt(index)) {
                    "Попытка не найдена"
                }
            }
        }
    }

    fun exportTo(uri: Uri) {
        runOperation("JSON-файл успешно сохранён") {
            withContext(Dispatchers.IO) {
                val temporaryFile = File(
                    appContext.cacheDir,
                    "quiz_export_${System.nanoTime()}.json"
                )

                try {
                    exportService.exportAll(temporaryFile)

                    val output = appContext.contentResolver
                        .openOutputStream(uri)
                        ?: throw IOException(
                            "Не удалось открыть файл для записи"
                        )

                    output.use { destination ->
                        temporaryFile.inputStream().use { source ->
                            source.copyTo(destination)
                        }
                    }
                } finally {
                    temporaryFile.delete()
                }
            }
        }
    }

    private fun runOperation(
        successMessage: String,
        operation: suspend () -> Unit
    ) {
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBusy = true,
                message = null
            )

            try {
                operation()
                reloadData(successMessage)
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    private suspend fun reloadData(
        message: String? = null
    ) {
        val data = withContext(Dispatchers.IO) {
            val settings = questionRepository.getSettings()

            val attempts = attemptRepository
                .loadAttempts()
                .withIndex()
                .sortedByDescending {
                    it.value.startedAt
                }

            settings to attempts
        }

        _uiState.value = AdminUiState(
            settings = data.first,
            attempts = data.second,
            message = message
        )
    }

    private fun showError(error: Exception) {
        _uiState.value = _uiState.value.copy(
            isBusy = false,
            message = error.message ?: "Ошибка операции",
            isError = true
        )
    }
}

class AdminViewModelFactory(
    private val questionRepository: QuestionRepository,
    private val attemptRepository: AttemptRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                questionRepository,
                attemptRepository,
                context
            ) as T
        }

        throw IllegalArgumentException(
            "Неизвестный класс ViewModel"
        )
    }
}
