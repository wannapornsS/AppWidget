package wns.freewill.appwidget

import android.app.Notification.Action
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them

        Log.d("NewAppWidget onUpdate 1", "appWidgetIds : $appWidgetIds")
        for (appWidgetId in appWidgetIds) {
            Log.d("NewAppWidget onUpdate 2", "appWidgetIds : $appWidgetIds")
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

    }

    override fun onEnabled(context: Context) {
        Log.d("NewAppWidget onEnabled", "context : $context")
    }

    override fun onDisabled(context: Context) {
        Log.d("NewAppWidget onDisabled", "context : $context")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val i = intent?.dataString
        Log.d("NewAppWidget onReceive 1", "time : $intent :: $i")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val currentTime: Date = Calendar.getInstance().time

        intent?.let {
            val remoteViews = RemoteViews(context!!.packageName, R.layout.new_app_widget)
            val watchWidget = ComponentName(context, NewAppWidget::class.java)

            if (it.action == "ACTION_MY_BUTTON" ){
                Log.d("NewAppWidget onReceive 1.1", "${it.action}")
                val id = it.getIntExtra("ID", 0)
                Log.d("NewAppWidget onReceive 1.2", "$id")

                remoteViews.setTextViewText(R.id.textTime, "Loading...")

//                val serviceIntent = Intent(context, CallTimeService::class.java)
//                context.startService(serviceIntent)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(Intent(context, CallTimeService::class.java))
                } else {
                    context.startService(Intent(context, CallTimeService::class.java))
                }


//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
//                    PendingIntent.getBroadcast(context, 0, intent,
//                        PendingIntent.FLAG_IMMUTABLE).cancel()
//                }else{
//                    PendingIntent.getBroadcast(context, 0, intent,
//                        PendingIntent.FLAG_UPDATE_CURRENT).cancel()
//                }

                appWidgetManager.updateAppWidget(watchWidget, remoteViews)
            } else if (it.action == "DATA_TIME"){
                val data = it.getStringExtra("DATA") ?: "error"
                remoteViews.setTextViewText(R.id.textTime, data)

                context.startService(Intent(context, CallTimeService::class.java))

                appWidgetManager.updateAppWidget(watchWidget, remoteViews)

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    context.startForegroundService(Intent(context, CallTimeService::class.java))
//                } else {
//                    context.startService(Intent(context, CallTimeService::class.java))
//                }


            }
        }


        Log.d("NewAppWidget onReceive 2", "time : $currentTime")
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)
    val currentTime: Date = Calendar.getInstance().time

    views.setTextViewText(R.id.textTime, "Time : $currentTime : $appWidgetId")
    Log.d("NewAppWidget onUpdate _", "appWidgetIds : $currentTime / id : $appWidgetId")


    val intentButton = Intent(context, NewAppWidget::class.java)
    intentButton.action = "ACTION_MY_BUTTON"
    intentButton.putExtra("ID", appWidgetId)

    val pendingIntent : PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        PendingIntent.getBroadcast(context, 0, intentButton, PendingIntent.FLAG_IMMUTABLE)
    }else{
        PendingIntent.getBroadcast(context, 0, intentButton, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    views.setOnClickPendingIntent(R.id.textTime, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)

}

//private fun fetchTime(intent: Intent, context: Context){
//
//    val disposable = APIService().getService().fetchTime()
//        .subscribe({
//            if (it.isSuccessful){
//                it.body()?.string().let { response ->
//
//                    val oldDateFormat = "yyyy-MM-dd'T'HH:mm:ss"
//                    val newDateFormat = "yyyy-MM-dd'T'HH:mm:ss"
//
//
//                    val simpleDateFormatOld: SimpleDateFormat
//                    val simpleDateFormatNew: SimpleDateFormat
//
//                    try {
//                        simpleDateFormatOld = SimpleDateFormat()
//                        simpleDateFormatOld.timeZone = TimeZone.getTimeZone("GMT+07:00:00")
//                        simpleDateFormatOld.applyPattern(oldDateFormat)
//                        simpleDateFormatNew = SimpleDateFormat(newDateFormat, Locale.UK)
//                        val result = simpleDateFormatNew.format(simpleDateFormatOld.parse(response))
//                        sendUpdateData(this, result.replace("T", " เวลา : "))
//                        Log.d("fetchTime 1", "$result")
//                        stopService(intent)
//                    } catch (e: java.lang.Exception) {
//                        Log.d("fetchTime 2", "${e.message}")
//                        e.printStackTrace()
//                        stopService(intent)
//                    }
//                }
//            }
//        }, { e ->
//            stopService(intent)
//        })
//
//    //disposable.dispose()
//}

