package wns.freewill.appwidget

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class CallTimeService : Service() {



    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CallTimeService 1", "")
        Log.d("CallTimeService 2", "$intent")


        if (intent != null){
//            val policy = ThreadPolicy.Builder().permitAll().build()
//            StrictMode.setThreadPolicy(policy)

            val thread = Thread {
                try {
                    fetchTime(intent,this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()

        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun fetchTime(intent: Intent, context: Context){

        val disposable = APIService().getService().fetchTime()
                .subscribe({
                    if (it.isSuccessful){
                        it.body()?.string().let { response ->

                            val oldDateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                            val newDateFormat = "yyyy-MM-dd'T'HH:mm:ss"


                            val simpleDateFormatOld: SimpleDateFormat
                            val simpleDateFormatNew: SimpleDateFormat

                            try {
                                simpleDateFormatOld = SimpleDateFormat()
                                simpleDateFormatOld.timeZone = TimeZone.getTimeZone("GMT+07:00:00")
                                simpleDateFormatOld.applyPattern(oldDateFormat)
                                simpleDateFormatNew = SimpleDateFormat(newDateFormat, Locale.UK)
                                val result = simpleDateFormatNew.format(simpleDateFormatOld.parse(response))
                                sendUpdateData(this, result.replace("T", " เวลา : "))
                                Log.d("fetchTime 1", "$result")
                                stopService(intent)
                            } catch (e: java.lang.Exception) {
                                Log.d("fetchTime 2", "${e.message}")
                                e.printStackTrace()
                                stopService(intent)
                            }
                        }
                    }
                }, { e ->
                    stopService(intent)
                })

        //disposable.dispose()
    }

    private fun stopService0() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        } else {
            stopSelf()
        }
    }

    private fun sendUpdateData(context: Context, data: String){
        val intent = Intent(context, NewAppWidget::class.java)
        intent.action = "DATA_TIME"
        intent.putExtra("DATA", data)
        context.sendBroadcast(intent)
    }



}

