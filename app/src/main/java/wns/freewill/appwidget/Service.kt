package wns.freewill.appwidget

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface Service {

    @GET("/getDateTime")
    fun fetchTime(): Observable<Response<ResponseBody>>

}