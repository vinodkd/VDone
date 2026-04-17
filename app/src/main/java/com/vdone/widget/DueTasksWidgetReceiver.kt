package com.vdone.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DueTasksWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = DueTasksWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            DueTasksWidget.refresh(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        DueTasksWidgetWorker.schedule(context)
        CoroutineScope(Dispatchers.IO).launch {
            DueTasksWidget.refresh(context)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        DueTasksWidgetWorker.cancel(context)
    }
}
