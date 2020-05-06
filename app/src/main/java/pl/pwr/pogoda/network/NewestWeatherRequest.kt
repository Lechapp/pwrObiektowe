package pl.pwr.pogoda.network

import android.appwidget.AppWidgetManager
import android.content.ContentValues
import android.content.Context
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.pwr.pogoda.R
import pl.pwr.pogoda.config.*
import pl.pwr.pogoda.elements.*
import java.lang.Exception
import kotlin.math.roundToInt

class NewestWeatherRequest(private val c:Context, private val appWidgetManager: AppWidgetManager? = null, private val appWidgetId: Int = -1) {

    private val tool = WeatherTools()
    private val dbQueries = DbQueries(c)

    fun getNewestWeatherOpenWeather(idStation:String, lat:String = "", lon:String = "", onSuccess: (finishRequest: Boolean) -> Unit) {

        val searchvalue = if(lat != "") {
            "lat=$lat&lon=$lon"
        }else {
            "q=${dbQueries.getStationValue(idStation, StationTableInfo.ColumnCity)}"
        }
        val url = "https://api.openweathermap.org/data/2.5/weather?$searchvalue&appid=${OpenWeather.APIKey}"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)

            if (response.getString("cod") == "200") {
                val w = response.getJSONObject("main")

                val wind = response.getJSONObject("wind")
                val windToSave = if(wind.has("deg")){
                    wind.getString("deg")
                }else{
                    "null"
                }

                val unixTime = (System.currentTimeMillis() / 1000L).toDouble().roundToInt()

                val main = response.getJSONArray("weather").getJSONObject(0).getString("main")
                val description = response.getJSONArray("weather").getJSONObject(0).getString("description")
                val tempout = w.getString("temp")
                val rain = try {
                    if(response.has("rain")) {
                        response.getJSONObject("rain").getString("3h")
                    }else "null"
                }catch (e: Exception){
                    "null"
                }

                val tempimg = tool.getTempImgId(tempout, true)
                val humiout = tool.roundto(w.getString("humidity"))
                val pressure = tool.roundto(w.getString("pressure"))
                val time = response.getInt("dt") + response.getInt("timezone")

                val newWeather = ContentValues()
                newWeather.put(WeatherTableInfo.ColumnDescription, description)
                newWeather.put(WeatherTableInfo.ColumnTemp, tempout)
                newWeather.put(WeatherTableInfo.ColumnHumidity, humiout)
                newWeather.put(WeatherTableInfo.ColumnPressure, pressure)
                newWeather.put(WeatherTableInfo.ColumnTempImg, tempimg)
                newWeather.put(WeatherTableInfo.ColumnUpdatedTime, unixTime)
                newWeather.put(WeatherTableInfo.ColumnTime, time)
                newWeather.put(WeatherTableInfo.ColumnWeatherStatus, main)
                newWeather.put(WeatherTableInfo.ColumnRain, rain)
                newWeather.put(WeatherTableInfo.ColumnWindSpeed, wind.getString("speed"))
                newWeather.put(WeatherTableInfo.ColumnWindDir, windToSave)

                dbQueries.updateWeatherData(newWeather, idStation)

                val sys = response.getJSONObject("sys")

                    saveNewStation(
                        response.getString("name"),
                        sys.getInt("sunset"),
                        sys.getInt("sunrise"),
                        idStation,
                        response.getInt("timezone"),
                        lat, lon
                    )

                if(appWidgetId == -1)
                   // setWeather
                else{
                   //setWidget
                }
            }else{
                if(appWidgetId == -1)
                    //setWeather
                else{
                    //setWidget
                }
            }
            onSuccess(true)
        },
            Response.ErrorListener {
                if(appWidgetId == -1){
                    //setWeather
                }else{
                    //setWidget
                }
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }



    private fun saveNewStation(city:String, sunset:Int, sunrise:Int, idStation: String, timezone:Int, lati:String, longi:String){

        if(dbQueries.getStationValue(idStation, StationTableInfo.ColumnGPS).toInt() == 1
            && dbQueries.getStationValue(idStation, StationTableInfo.ColumnCity) != city) {

            val newStation = ContentValues()
            newStation.put(StationTableInfo.ColumnCity, city)
            newStation.put(StationTableInfo.ColumnSunrise, sunrise)
            newStation.put(StationTableInfo.ColumnSunset, sunset)
            newStation.put(StationTableInfo.ColumnTimezone, timezone)
            newStation.put(StationTableInfo.ColumnLatitude, lati)
            newStation.put(StationTableInfo.ColumnLongitude, longi)

            dbQueries.updateStationData(newStation, idStation)
        }
    }

}