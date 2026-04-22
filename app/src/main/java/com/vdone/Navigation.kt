package com.vdone

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vdone.data.repository.ConditionRepository
import com.vdone.data.repository.TaskRepository
import com.vdone.ui.detail.TaskDetailScreen
import com.vdone.ui.detail.TaskDetailViewModel
import com.vdone.ui.doing.DoingScreen
import com.vdone.ui.doing.DoingViewModel
import com.vdone.ui.done.DoneScreen
import com.vdone.ui.done.DoneViewModel
import com.vdone.ui.home.HomeScreen
import com.vdone.ui.home.HomeViewModel
import com.vdone.ui.loops.OpenLoopsScreen
import com.vdone.ui.loops.OpenLoopsViewModel
import com.vdone.ui.settings.SettingsScreen
import com.vdone.ui.tasks.TaskListScreen
import com.vdone.ui.tasks.TaskListViewModel

private const val NEW = "new"
private val TAB_ROUTES = setOf("plan", "next", "doing", "waiting", "done")

@Composable
fun VDoneNavHost(
    repository: TaskRepository,
    conditionRepository: ConditionRepository,
    initialRoute: String? = null,
    onNavControllerReady: (NavController) -> Unit = {},
) {
    val rootNav = rememberNavController()
    LaunchedEffect(rootNav) {
        onNavControllerReady(rootNav)
        if (initialRoute != null) rootNav.navigate(initialRoute)
    }
    val currentEntry by rootNav.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val showBottomBar = currentRoute in TAB_ROUTES

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "plan",
                        onClick = {
                            rootNav.navigate("plan") {
                                popUpTo("next") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Plan") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "next",
                        onClick = {
                            rootNav.navigate("next") {
                                popUpTo("next") { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Start") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "doing",
                        onClick = {
                            rootNav.navigate("doing") {
                                popUpTo("next") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        label = { Text("Doing") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "waiting",
                        onClick = {
                            rootNav.navigate("waiting") {
                                popUpTo("next") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        label = { Text("Waiting") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "done",
                        onClick = {
                            rootNav.navigate("done") {
                                popUpTo("next") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                        label = { Text("Done") },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = rootNav,
            startDestination = "next",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("plan") {
                val vm: TaskListViewModel = viewModel(factory = TaskListViewModel.Factory(repository, conditionRepository))
                TaskListScreen(
                    viewModel = vm,
                    onAddTask = { rootNav.navigate("detail/$NEW") },
                    onEditTask = { id -> rootNav.navigate("detail/$id") },
                    onNavigateToSettings = { rootNav.navigate("settings") },
                    title = "Plan",
                )
            }

            composable("next") {
                val vm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(repository, conditionRepository)
                )
                HomeScreen(
                    viewModel = vm,
                    onAddTask = { rootNav.navigate("detail/$NEW") },
                    onEditTask = { id -> rootNav.navigate("detail/$id") },
                    onNavigateToSettings = { rootNav.navigate("settings") },
                )
            }

            composable("doing") {
                val vm: DoingViewModel = viewModel(factory = DoingViewModel.Factory(repository))
                DoingScreen(
                    viewModel = vm,
                    onEditTask = { id -> rootNav.navigate("detail/$id") },
                    onNavigateToSettings = { rootNav.navigate("settings") },
                )
            }

            composable("waiting") {
                val vm: OpenLoopsViewModel = viewModel(factory = OpenLoopsViewModel.Factory(repository))
                OpenLoopsScreen(
                    viewModel = vm,
                    onEditTask = { id -> rootNav.navigate("detail/$id") },
                    onNavigateToSettings = { rootNav.navigate("settings") },
                )
            }

            composable("done") {
                val vm: DoneViewModel = viewModel(factory = DoneViewModel.Factory(repository))
                DoneScreen(
                    viewModel = vm,
                    onEditTask = { id -> rootNav.navigate("detail/$id") },
                    onNavigateToSettings = { rootNav.navigate("settings") },
                )
            }

            composable("settings") {
                SettingsScreen(onBack = { rootNav.popBackStack() })
            }

            composable(
                route = "detail/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val rawId = backStackEntry.arguments!!.getString("taskId")!!
                val taskId = if (rawId == NEW) null else rawId
                val vm: TaskDetailViewModel = viewModel(
                    factory = TaskDetailViewModel.Factory(repository, conditionRepository, taskId),
                )
                TaskDetailScreen(
                    viewModel = vm,
                    onBack = { rootNav.popBackStack() },
                    onAddSubtask = { pid -> rootNav.navigate("detail/$NEW-child-$pid") },
                    onEditSubtask = { id -> rootNav.navigate("detail/$id") },
                    taskId = taskId,
                )
            }

            composable(
                route = "detail/{raw}-child-{parentId}",
                arguments = listOf(
                    navArgument("raw") { type = NavType.StringType },
                    navArgument("parentId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val parentId = backStackEntry.arguments!!.getString("parentId")!!
                val vm: TaskDetailViewModel = viewModel(
                    factory = TaskDetailViewModel.Factory(
                        repository, conditionRepository, taskId = null, parentId = parentId
                    ),
                )
                TaskDetailScreen(
                    viewModel = vm,
                    onBack = { rootNav.popBackStack() },
                    onAddSubtask = {},
                    onEditSubtask = { id -> rootNav.navigate("detail/$id") },
                    taskId = null,
                )
            }
        }
    }
}
