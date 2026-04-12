package com.vdone

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vdone.data.repository.TaskRepository
import com.vdone.ui.detail.TaskDetailScreen
import com.vdone.ui.detail.TaskDetailViewModel
import com.vdone.ui.tasks.TaskListScreen
import com.vdone.ui.tasks.TaskListViewModel

private const val NEW = "new"

@Composable
fun VDoneNavHost(repository: TaskRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tasks") {

        composable("tasks") {
            val vm: TaskListViewModel = viewModel(factory = TaskListViewModel.Factory(repository))
            TaskListScreen(
                viewModel = vm,
                onAddTask = { navController.navigate("detail/$NEW") },
                onEditTask = { id -> navController.navigate("detail/$id") },
            )
        }

        composable(
            route = "detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments!!.getString("taskId")!!
            val taskId = if (rawId == NEW) null else rawId
            val vm: TaskDetailViewModel = viewModel(
                factory = TaskDetailViewModel.Factory(repository, taskId),
            )
            TaskDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onAddSubtask = { pid -> navController.navigate("detail/$NEW-child-$pid") },
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
                factory = TaskDetailViewModel.Factory(repository, taskId = null, parentId = parentId),
            )
            TaskDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onAddSubtask = {},
                taskId = null,
            )
        }
    }
}
