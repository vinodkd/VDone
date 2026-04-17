package com.vdone.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.vdone.VDoneApp
import com.vdone.scheduler.FrequencyChecker
import kotlinx.coroutines.flow.first
import java.util.Calendar

val TASK_COUNT_KEY = intPreferencesKey("task_count")
fun taskTitleKey(i: Int) = stringPreferencesKey("task_title_$i")
fun taskIdKey(i: Int) = stringPreferencesKey("task_id_$i")

class DueTasksWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val count = prefs[TASK_COUNT_KEY] ?: 0
            val titles = (0 until count).map { prefs[taskTitleKey(it)] ?: "" }
            val ids    = (0 until count).map { prefs[taskIdKey(it)]    ?: "" }
            WidgetContent(titles, ids)
        }
    }

    companion object {
        suspend fun refresh(context: Context) {
            val repo = (context.applicationContext as VDoneApp).taskRepository
            val now  = System.currentTimeMillis()
            val cal  = Calendar.getInstance()
            val minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

            val endOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis

            val freq  = repo.getFrequencyTasks().first()
            val fixed = repo.getFixedTasks().first()

            val dueFreq  = freq.filter {
                it.status != "done" &&
                FrequencyChecker.isDueToday(it) &&
                (it.frequencyTime == null || minuteOfDay >= it.frequencyTime)
            }
            val dueFixed = fixed.filter {
                it.status != "done" &&
                it.fixedStart != null &&
                it.fixedStart in now..endOfToday
            }
            val due = (dueFreq + dueFixed).distinctBy { it.id }.take(5)

            GlanceAppWidgetManager(context)
                .getGlanceIds(DueTasksWidget::class.java)
                .forEach { glanceId ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[TASK_COUNT_KEY] = due.size
                            due.forEachIndexed { i, task ->
                                this[taskTitleKey(i)] = task.title
                                this[taskIdKey(i)]    = task.id
                            }
                        }
                    }
                    DueTasksWidget().update(context, glanceId)
                }
        }
    }
}

@Composable
private fun WidgetContent(titles: List<String>, ids: List<String>) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Next Up",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    "+",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 4.dp)
                        .clickable(
                            actionStartActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("vdone://open/detail/new"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                        ),
                )
            }

            if (titles.isEmpty()) {
                Text(
                    "No tasks due today",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.padding(top = 8.dp),
                )
            } else {
                titles.forEachIndexed { i, title ->
                    Text(
                        "\u2022  $title",
                        style = TextStyle(color = GlanceTheme.colors.onSurface),
                        maxLines = 1,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable(
                                actionStartActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("vdone://open/detail/${ids[i]}"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                )
                            ),
                    )
                }
            }
        }
    }
}
