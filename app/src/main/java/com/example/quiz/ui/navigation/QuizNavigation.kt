
package com.example.quiz.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quiz.data.AttemptRepository
import com.example.quiz.data.QuestionRepository
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

object Routes {
    const val LOGIN = "login"
    const val TOPICS = "topics/{login}"
    const val HISTORY = "history/{login}"
    const val QUIZ = "quiz/{login}/{topicId}"
    const val REPORT = "report/{login}/{attemptIndex}"

    const val ADMIN = "admin"
    const val ADMIN_BANK = "admin/bank"

    fun topics(login: String): String =
        "topics/${Uri.encode(login)}"

    fun history(login: String): String =
        "history/${Uri.encode(login)}"

    fun quiz(
        login: String,
        topicId: Long
    ): String =
        "quiz/${Uri.encode(login)}/$topicId"

    fun report(
        login: String,
        attemptIndex: Int
    ): String =
        "report/${Uri.encode(login)}/$attemptIndex"
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

            TopicsScreen(
                topics = questionRepository.getTopics(),
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

            val attempts = runCatching {
                attemptRepository.loadAttempts()
            }
                .getOrDefault(mutableListOf())
                .withIndex()
                .filter {
                    it.value.studentLogin.equals(
                        login,
                        ignoreCase = true
                    )
                }
                .sortedByDescending {
                    it.value.startedAt
                }

            HistoryScreen(
                attempts = attempts,
                onAttemptClick = { index ->
                    navController.navigate(
                        Routes.report(login, index)
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
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
                val index = quizViewModel
                    .uiState
                    .value
                    .savedAttemptIndex

                if (index != null) {
                    navController.navigate(
                        Routes.report(login, index)
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
                navArgument("attemptIndex") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val login = entry.arguments
                ?.getString("login")
                .orEmpty()

            val index = entry.arguments
                ?.getInt("attemptIndex")
                ?: -1

            val attempt = runCatching {
                attemptRepository.loadAttempts()
            }
                .getOrNull()
                ?.getOrNull(index)
                ?.takeIf {
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
                    navController.navigate(
                        Routes.ADMIN_BANK
                    )
                },
                onLogoutClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN_BANK) {
            val topicsResult = runCatching {
                questionRepository.getTopics()
            }

            val questionsResult = runCatching {
                questionRepository.getQuestions()
            }

            AdminBankScreen(
                topics = topicsResult.getOrDefault(
                    emptyList()
                ),
                questions = questionsResult.getOrDefault(
                    emptyList()
                ),
                errorMessage =
                    topicsResult.exceptionOrNull()?.message
                        ?: questionsResult.exceptionOrNull()?.message,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
