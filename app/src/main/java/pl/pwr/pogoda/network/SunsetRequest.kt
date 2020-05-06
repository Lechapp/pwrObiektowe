package pl.pwr.pogoda.network

import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.pwr.pogoda.R
import pl.pwr.pogoda.config.OpenWeather
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.elements.DbQueries
import java.lang.Exception

class SunsetRequest {

    fun getNewestSunset(context: Context, searchval:String, stationID: String, onSuccess: (finishRequest: Boolean) -> Unit) {

        val url = "https://api.openweathermap.org/data/2.5/weather?$searchval&appid=${OpenWeather.APIKey}"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)

                if (response.has("cod") && response.getString("cod") == "200") {

                    try {
                        val timezone = response.getInt("timezone")
                        val data = response.getJSONObject("sys")
                        val sunrise = data.getInt("sunrise")
                        val sunset = data.getInt("sunset")

                        //setDayProgress
                        saveSunset(context,sunset,sunrise, stationID)
                        onSuccess(true)
                    }catch (e:Exception){}
                }

            },
            Response.ErrorListener {
                Toast.makeText(context, context.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    private fun saveSunset(c:Context,sunset:Int, sunrise:Int, stationID : String){
        val newStation = ContentValues()
        newStation.put(StationTableInfo.ColumnSunset,sunset)
        newStation.put(StationTableInfo.ColumnSunrise, sunrise)

        DbQueries(c).updateStationData(newStation, stationID)
    }
}