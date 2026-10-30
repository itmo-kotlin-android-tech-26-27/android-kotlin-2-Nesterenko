package com.example.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.quiz.data.AttemptRepository
import com.example.quiz.data.QuestionRepository
import com.example.quiz.ui.navigation.QuizNavigation
import com.example.quiz.ui.theme.KotlinQuizTask2Theme
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val questionRepository = QuestionRepository(
            file = File(filesDir, "quiz-data.json")
        )
        val attemptRepository = AttemptRepository(
            file = resolveAttemptsFile()
        )

        setContent {
            KotlinQuizTask2Theme {
                QuizNavigation(
                    questionRepository = questionRepository,
                    attemptRepository = attemptRepository
                )
            }
        }
    }

    private fun resolveAttemptsFile(): File {
        val existingFile = filesDir.listFiles()
            ?.filter { file ->
                file.isFile &&
                    file.name.startsWith("attempts_") &&
                    file.name.endsWith(".json")
            }
            ?.maxByOrNull { file -> file.lastModified() }

        if (existingFile != null) return existingFile

        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        )
        return File(filesDir, "attempts_$timestamp.json").apply {
            // Создаём файл сразу: после поворота экрана Activity найдёт тот же файл.
            writeText("[]")
        }
    }
}
