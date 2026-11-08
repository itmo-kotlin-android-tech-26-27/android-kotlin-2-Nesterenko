
package com.example.quiz.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quiz.data.AttemptRepository
import com.example.quiz.data.QuestionRepository
import com.example.quiz.domain.Attempt
import com.example.quiz.domain.Question
import com.example.quiz.domain.Topic
import com.example.quiz.ui.admin.AdminBankScreen
import com.example.quiz.ui.admin.AdminScreen
import com.example.quiz.ui.admin.AdminViewModel
import com.example.quiz.ui.admin.AdminViewModelFactory
import com.example.quiz.ui.history.HistoryScreen
import com.example.quiz.ui.login.LoginScreen
import com.example.quiz.ui.quiz.QuizScreen
import com.example.quiz.ui.quiz.QuizViewModel
import com.example.quiz.ui.quiz.QuizViewModelFactory
import com.example.quiz.ui.report.ReportScreen
import com.example.quiz.ui.topics.TopicsScreen
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Routes {
    const val LOGIN = "login"
    const val TOPICS = "topics/{login}"
    const val HISTORY = "history/{login}"
    const val QUIZ = "quiz/{login}/{topicId}"
    const val REPORT = "report/{login}/{attemptId}"
    const val ADMIN = "admin"
    const val ADMIN_BANK = "admin/bank"

    fun topics(login: String): String =
        "topics/${Uri.encode(login)}"

    fun history(login: String): String =
        "history/${Uri.encode(login)}"

    fun quiz(login: String, topicId: Long): String =
        "quiz/${Uri.encode(login)}/$topicId"

    fun report(login: String, attemptId: UUID): String =
        "report/${Uri.encode(login)}/$attemptId"
}

@Composable
fun QuizNavigation(
    questionRepository: QuestionRepository,
    attemptRepository: AttemptRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onStudentLogin = { login ->
                    navController.navigate(
                        Routes.topics(login)
                    )
                },
                onAdminLogin = {
                    navController.navigate(Routes.ADMIN)
                }
            )
        }

        composable(
            route = Routes.TOPICS,
            arguments = listOf(
                navArgument("login") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val login = entry.arguments
                ?.getString("login")
                .orEmpty()

            var retryKey by remember {
                mutableIntStateOf(0)
            }

            var loadState by remember {
                mutableStateOf<LoadState<List<Topic>>>(
                    LoadState.Loading
                )
            }

            LaunchedEffect(login, retryKey) {
                loadState = LoadState.Loading
                loadState = loadRepository {
                    questionRepository.getTopics()
                }
            }

            when (val result = loadState) {
                LoadState.Loading -> LoadingScreen()

                is LoadState.Failure -> ErrorScreen(
                    message = result.message,
                    onRetry = { retryKey++ },
                    onBack = {
                        navController.popBackStack()
                    }
                )

                is LoadState.Success -> TopicsScreen(
                    topics = result.data,
                    onTopicClick = { topic ->
                        navController.navigate(
                            Routes.quiz(login, topic.id)
                        )
                    },
                    onHistoryClick = {
                        navController.navigate(
                            Routes.history(login)
                        )
                    }
                )
            }
        }

        composable(
            route = Routes.HISTORY,
            arguments = listOf(
                navArgument("login") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val login = entry.arguments
                ?.getString("login")
                .orEmpty()

            var retryKey by remember {
                mutableIntStateOf(0)
            }

            var loadState by remember {
                mutableStateOf<LoadState<List<Attempt>>>(
                    LoadState.Loading
                )
            }

            LaunchedEffect(login, retryKey) {
                loadState = LoadState.Loading
                loadState = loadRepository {
                    attemptRepository
                        .getAttemptsForStudent(login)
                        .sortedByDescending {
                            it.startedAt
                        }
                }
            }

            when (val result = loadState) {
                LoadState.Loading -> LoadingScreen()

                is LoadState.Failure -> ErrorScreen(
                    message = result.message,
                    onRetry = { retryKey++ },
                    onBack = {
                        navController.popBackStack()
                    }
                )

                is LoadState.Success -> HistoryScreen(
                    attempts = result.data,
                    onAttemptClick = { attemptId ->
                        navController.navigate(
                            Routes.report(login, attemptId)
                        )
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = Routes.QUIZ,
            arguments = listOf(
                navArgument("login") {
                    type = NavType.StringType
                },
                navArgument("topicId") {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            val login = entry.arguments
                ?.getString("login")
                .orEmpty()

            val topicId = entry.arguments
                ?.getLong("topicId")
                ?: return@composable

            val quizViewModel: QuizViewModel = viewModel(
                factory = QuizViewModelFactory(
                    questionRepository = questionRepository,
                    attemptRepository = attemptRepository,
                    studentLogin = login,
                    topicId = topicId
                )
            )

            val navigateToReport: () -> Unit = {
                val savedId = quizViewModel
                    .uiState
                    .value
                    .savedAttemptId

                val attemptId = runCatching {
                    savedId?.let(UUID::fromString)
                }.getOrNull()

                if (attemptId != null) {
                    navController.navigate(
                        Routes.report(login, attemptId)
                    ) {
                        popUpTo(Routes.QUIZ) {
                            inclusive = true
                        }
                    }
                } else if (
                    quizViewModel.canLeaveWithoutSaving()
                ) {
                    navController.popBackStack()
                }
            }

            QuizScreen(
                viewModel = quizViewModel,
                onQuizFinished = navigateToReport,
                onQuizInterrupted = navigateToReport
            )
        }

        composable(
            route = Routes.REPORT,
            arguments = listOf(
                navArgument("login") {
                    type = NavType.StringType
                },
                navArgument("attemptId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val login = entry.arguments
                ?.getString("login")
                .orEmpty()

            val attemptId = runCatching {
                entry.arguments
                    ?.getString("attemptId")
                    ?.let(UUID::fromString)
            }.getOrNull()

            var retryKey by remember {
                mutableIntStateOf(0)
            }

            var loadState by remember {
                mutableStateOf<LoadState<Attempt?>>(
                    LoadState.Loading
                )
            }

            LaunchedEffect(attemptId, retryKey) {
                loadState = LoadState.Loading

                loadState = if (attemptId == null) {
                    LoadState.Failure(
                        "Некорректный идентификатор попытки"
                    )
                } else {
                    loadRepository {
                        attemptRepository.getAttempt(attemptId)
                    }
                }
            }

            when (val result = loadState) {
                LoadState.Loading -> LoadingScreen()

                is LoadState.Failure -> ErrorScreen(
                    message = result.message,
                    onRetry = { retryKey++ },
                    onBack = {
                        navController.popBackStack()
                    }
                )

                is LoadState.Success -> {
                    val attempt = result.data?.takeIf {
                        it.studentLogin.equals(
                            login,
                            ignoreCase = true
                        )
                    }

                    ReportScreen(
                        attempt = attempt,
                        onRetryClick = { topicId ->
                            navController.navigate(
                                Routes.quiz(login, topicId)
                            ) {
                                popUpTo(Routes.REPORT) {
                                    inclusive = true
                                }
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        composable(Routes.ADMIN) {
            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(
                    questionRepository = questionRepository,
                    attemptRepository = attemptRepository,
                    context = context.applicationContext
                )
            )

            AdminScreen(
                viewModel = adminViewModel,
                onBankClick = {
                    navController.navigate(Routes.ADMIN_BANK)
                },
                onLogoutClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN_BANK) {
            var retryKey by remember {
                mutableIntStateOf(0)
            }

            var loadState by remember {
                mutableStateOf<
                        LoadState<Pair<List<Topic>, List<Question>>>
                        >(LoadState.Loading)
            }

            LaunchedEffect(retryKey) {
                loadState = LoadState.Loading

                loadState = loadRepository {
                    questionRepository.getTopics() to
                            questionRepository.getQuestions()
                }
            }

            when (val result = loadState) {
                LoadState.Loading -> LoadingScreen()

                is LoadState.Failure -> ErrorScreen(
                    message = result.message,
                    onRetry = { retryKey++ },
                    onBack = {
                        navController.popBackStack()
                    }
                )

                is LoadState.Success -> AdminBankScreen(
                    topics = result.data.first,
                    questions = result.data.second,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

private sealed interface LoadState<out T> {

    data object Loading : LoadState<Nothing>

    data class Success<T>(
        val data: T
    ) : LoadState<T>

    data class Failure(
        val message: String
    ) : LoadState<Nothing>
}

private suspend fun <T> loadRepository(
    operation: () -> T
): LoadState<T> {
    return try {
        val data = withContext(Dispatchers.IO) {
            operation()
        }

        LoadState.Success(data)

    } catch (error: CancellationException) {
        throw error

    } catch (error: Exception) {
        LoadState.Failure(
            "Не удалось загрузить данные. " +
                    "Проверьте JSON-файлы приложения. " +
                    (error.message ?: "")
        )
    }
}

@Composable
private fun LoadingScreen() {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()

            Text(
                text = "Загрузка данных...",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Повторить")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад")
            }
        }
    }
}
