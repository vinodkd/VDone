package com.vdone.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vdone.VDoneApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val repository = (context.applicationContext as VDoneApp).taskRepository
        CoroutineScope(Dispatchers.IO).launch {
            repository.getFixedTasks().first().forEach { task ->
                val fireAt = task.fixedStart ?: return@forEach
                if (fireAt > System.currentTimeMillis()) {
                    AlarmScheduler.schedule(context, task)
                }
            }
        }
    }
}
