package pl.simplyinc.pogoda.network

import android.appwidget.AppWidgetManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import pl.simplyinc.pogoda.R
import pl.simplyinc.pogoda.config.*
import pl.simplyinc.pogoda.elements.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class NewestWeatherRequest(val c:Context, private val v:View?, private val appWidgetManager: AppWidgetManager? = null, private val appWidgetId: Int = -1) {

    private val tool = WeatherTools()
    private val dbHelper = DataBaseHelper(c)
    private val db = dbHelper.writableDatabase

    fun getNewestWeatherOpenWeather(stat: Cursor, lat:String = "", lon:String = "", onSuccess: (weathercursor: Cursor) -> Unit) {
        var weatherCursor = db.query(WeatherTableInfo.TableName, null, WeatherTableInfo.ColumnStationID + "=?", arrayOf(stat.getInt(stat.getColumnIndex(BaseColumns._ID)).toString()), null,null, null)
        weatherCursor.moveToFirst()

        val searchvalue = if(lat != "") {
            "lat=$lat&lon=$lon"
        }else {
            "q=${stat.getString(stat.getColumnIndex(StationTableInfo.ColumnCity))}"
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
                db.update(
                    WeatherTableInfo.TableName, newWeather, WeatherTableInfo.ColumnStationID + "=?", arrayOf(stat.getString(stat.getColumnIndex(
                        BaseColumns._ID))))

                val sys = response.getJSONObject("sys")

                    saveNewStation(
                        response.getString("name"),
                        sys.getInt("sunset"),
                        sys.getInt("sunrise"),
                        stat,
                        response.getInt("timezone"),
                        lat, lon
                    )
                weatherCursor = db.query(WeatherTableInfo.TableName, null, WeatherTableInfo.ColumnStationID + "=?", arrayOf(stat.getInt(stat.getColumnIndex(BaseColumns._ID)).toString()), null,null, null)
                weatherCursor.moveToFirst()

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
            onSuccess(weatherCursor)
        },
            Response.ErrorListener {
                if(appWidgetId == -1){
                    //setWeather(weatherCursor, stat, true)
                    onSuccess(weatherCursor)
                }else{
                    //setWidget
                }
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }



    private fun saveNewStation(city:String, sunset:Int, sunrise:Int, stat:Cursor, timezone:Int, lati:String, longi:String){

        if(stat.getInt(stat.getColumnIndex(StationTableInfo.ColumnGPS)) == 1
            && stat.getString(stat.getColumnIndex(StationTableInfo.ColumnCity)) != city) {

            val newStation = ContentValues()
            newStation.put(StationTableInfo.ColumnCity, city)
            newStation.put(StationTableInfo.ColumnSunrise, sunrise)
            newStation.put(StationTableInfo.ColumnSunset, sunset)
            newStation.put(StationTableInfo.ColumnTimezone, timezone)
            newStation.put(StationTableInfo.ColumnLatitude, lati)
            newStation.put(StationTableInfo.ColumnLongitude, longi)
            db.update(
                StationTableInfo.TableName, newStation, BaseColumns._ID + "=?", arrayOf(stat.getString(stat.getColumnIndex(
                    BaseColumns._ID))))
        }
    }

}