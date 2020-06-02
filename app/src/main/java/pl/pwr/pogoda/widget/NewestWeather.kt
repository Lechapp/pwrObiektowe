package pl.pwr.pogoda.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import org.json.JSONArray
import pl.pwr.pogoda.R
import pl.pwr.pogoda.activities.MainActivity
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.config.WeatherTableInfo
import pl.pwr.pogoda.config.WidgetTableInfo
import pl.pwr.pogoda.elements.DbQueries
import pl.pwr.pogoda.elements.GpsLocation
import pl.pwr.pogoda.elements.WeatherTools
import pl.pwr.pogoda.network.OpenWeatherRequests
import java.text.SimpleDateFormat
import java.util.*


class NewestWeather : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val dbQueries = DbQueries(context)
        for (appWidgetId in appWidgetIds) {
            dbQueries.deleteWidget(appWidgetId.toString())
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        lateinit var update:OpenWeatherRequests
        lateinit var stationID:String
        lateinit var c:Context
        private lateinit var appWidgetManager: AppWidgetManager
        private var appWidgetId:Int = 0

        internal fun updateAppWidget(context: Context, appWidgetManagerr: AppWidgetManager, appWidgetIdd: Int) {


            val dbQueries = DbQueries(context)
            appWidgetManager = appWidgetManagerr
            appWidgetId = appWidgetIdd
            val widgetData = dbQueries.getWidgetData(appWidgetId.toString())
            stationID = widgetData.getString(WidgetTableInfo.ColumnStationID)
            val gpsStation = dbQueries.getStationValue(stationID, StationTableInfo.ColumnGPS)
            val forecastData = dbQueries.getAllForecast(stationID)
            update = OpenWeatherRequests(context, stationID)
            c = context
            val now = System.currentTimeMillis() / 1000L

            if (forecastData[0].getInt(0) < (now - 12800)) {
                if (gpsStation == "false") {
                    update.getNewestForecast(dbQueries.getStationValue(stationID, StationTableInfo.ColumnCity)){
                        setData(update, gpsStation,  forecastData[0].getInt(0))
                    }
                }else{
                    setData(update, gpsStation,  forecastData[0].getInt(0))
                }

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                intent.putExtra("setweather", -1)
                val views = RemoteViews(context.packageName, R.layout.newest_weather)
                views.setOnClickPendingIntent(R.id.widget, pendingIntent)
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)

            }
        }
        private fun setData(update:OpenWeatherRequests, gpsStation:String, weatherTime:Int){
            val gps = GpsLocation(c, null, this)
            val unixTime = System.currentTimeMillis() / 1000L

            if(weatherTime + 1800 <= unixTime){
                if(gpsStation == "true") {
                    gps.getLocation()
                }else{
                    update.getNewestWeatherOpenWeather{
                        setWidget()
                    }
                }
            }else{
                if(gpsStation == "true") {
                    gps.getLocation()
                }else{
                    //bez oczekiwania nie chce się odpalić za pierwszym razem tfu
                    Handler().postDelayed({
                        setWidget()
                    },700)
                }
            }
        }

        fun setWidgetFromGps(loc:Location){
            update.getNewestWeatherOpenWeather(loc.latitude.toString(), loc.longitude.toString()){
               setWidget()
            }
        }

        private fun setWidget():Boolean{
            val dbQueries = DbQueries(c)
            val widgetData = dbQueries.getWidgetData(stationID)
            val weather = dbQueries.getWeatherData(stationID)
            val forecast = dbQueries.getAllForecast(stationID)
            val views = RemoteViews(c.packageName, R.layout.newest_weather)
            val tool = WeatherTools()
            val blackBg = widgetData.getInt(WidgetTableInfo.ColumnThemeBlack) == 1
            val tempunit = dbQueries.getStationValue(stationID, StationTableInfo.ColumnTempUnit)
            val windunit = dbQueries.getStationValue(stationID, StationTableInfo.ColumnWindUnit)

            views.setTextViewText(R.id.title, StationTableInfo.ColumnCity)

            setForecast(views, forecast, tempunit, blackBg)


            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val lastdat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
            lastdat.timeZone = TimeZone.getTimeZone("GMT")
            val unixTime = System.currentTimeMillis()
            val update = Date(unixTime)
            val datafrom = Date(weather.getInt(WeatherTableInfo.ColumnTime)*1000L)
            val text = c.getString(R.string.lastupdate) + " " + lastdat.format(datafrom) + ". " + c.getString(R.string.updated) +" "+ sdf.format(update)
            views.setTextViewText(R.id.updatedat, text)

            //temp
            if(widgetData.getInt(WidgetTableInfo.ColumnTemp) == 1){
                val temp = tool.kelvintoTempUnit(weather.getString(WeatherTableInfo.ColumnTemp), tempunit)
                views.setTextViewText(R.id.tempout, temp + tempunit)

                views.setInt(R.id.tempimage, "setBackgroundResource", R.drawable.temp2_b)
            }else views.setViewVisibility(R.id.alltemp, View.GONE)


            //humidity
            if(widgetData.getInt(WidgetTableInfo.ColumnHumidity) == 1)
                views.setTextViewText(R.id.humidityout, weather.getString(WeatherTableInfo.ColumnHumidity)+"%")
            else views.setViewVisibility(R.id.allhumidity, View.GONE)



            //pressure
            if(widgetData.getInt(WidgetTableInfo.ColumnPressure) == 1)
                views.setTextViewText(R.id.pressure, weather.getString(WeatherTableInfo.ColumnPressure) + "hPa")
            else {
                views.setViewVisibility(R.id.pressure, View.GONE)
                views.setViewVisibility(R.id.pressureimg, View.GONE)
            }

            //windspeed
            if(widgetData.getInt(WidgetTableInfo.ColumnWindSpeed) == 1) {
                val wind = tool.mstoWindUnit(weather.getString(WeatherTableInfo.ColumnWindSpeed), windunit)

                views.setTextViewText(
                    R.id.windspeed,
                    wind + windunit
                )
            }else {
                views.setViewVisibility(R.id.windspeed, View.GONE)
                views.setViewVisibility(R.id.windspeedimg, View.GONE)
            }

            //rainfall
            if(widgetData.getInt(WidgetTableInfo.ColumnRainfall) == 1){
                val raintext = tool.roundto(weather.getString(WeatherTableInfo.ColumnRain)) + "mm"
                views.setTextViewText(R.id.rainfall, raintext)
            }else{
                views.setViewVisibility(R.id.rainfall, View.GONE)
                views.setViewVisibility(R.id.rainfallimg, View.GONE)
            }


            //weatherimg
            if(widgetData.getInt(WidgetTableInfo.ColumnIcon) == 1) {
                val weatherImg = tool.weatherIconOpenWeather(
                    weather.getString(WeatherTableInfo.ColumnWeatherStatus),
                    weather.getString(WeatherTableInfo.ColumnDescription),
                    dbQueries.getStationValue(stationID, StationTableInfo.ColumnSunrise).toInt(),
                    dbQueries.getStationValue(stationID, StationTableInfo.ColumnSunset).toInt(),
                    dbQueries.getStationValue(stationID, StationTableInfo.ColumnTimezone).toInt(),
                    widgetData.getBoolean(WidgetTableInfo.ColumnThemeBlack),
                    weather.getString(WeatherTableInfo.ColumnRain)
                )
                views.setInt(R.id.weathericon, "setBackgroundResource", weatherImg)
            }else views.setViewVisibility(R.id.weathericon, View.GONE)

            //setTheme
            views.setInt(R.id.widget, "setBackgroundColor", widgetData.getInt(WidgetTableInfo.ColumnBackground))
            if(blackBg) {
                views.setInt(R.id.tempimage, "setBackgroundResource", R.drawable.temp2_w)
                views.setInt(R.id.humidityimg, "setBackgroundResource", R.drawable.humidity_w)
                views.setInt(R.id.rainfallimg, "setBackgroundResource", R.drawable.umbrella_w)
                views.setInt(R.id.pressureimg, "setBackgroundResource", R.drawable.pressure_w)
                views.setInt(R.id.windspeedimg, "setBackgroundResource", R.drawable.wind_w)

                val white = ContextCompat.getColor(c, R.color.white)
                views.setTextColor(R.id.title, white)
                views.setTextColor(R.id.tempout, white)
                views.setTextColor(R.id.humidityout,white)
                views.setTextColor(R.id.rainfall,white)
                views.setTextColor(R.id.pressure,white)
                views.setTextColor(R.id.windspeed,white)
                views.setTextColor(R.id.widgetday1,white)
                views.setTextColor(R.id.widgetday2,white)
                views.setTextColor(R.id.widgetday3,white)
                views.setTextColor(R.id.widgetday4,white)
                views.setTextColor(R.id.widgetday5,white)
                views.setTextColor(R.id.forecasttemp1,white)
                views.setTextColor(R.id.forecasttemp2,white)
                views.setTextColor(R.id.forecasttemp3,white)
                views.setTextColor(R.id.forecasttemp4,white)
                views.setTextColor(R.id.forecasttemp5,white)
                views.setTextColor(R.id.updatedat, ContextCompat.getColor(c, R.color.whitesmoke))
            }else views.setInt(R.id.tempimage, "setBackgroundResource", R.drawable.temp2_b)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return true

        }


        private fun setForecast(v:RemoteViews, f:MutableList<JSONArray>, tempunit:String, blackBg:Boolean){
            val tool = WeatherTools()
            val arrayimg = listOf(
                R.id.forecastimg1,
                R.id.forecastimg2,
                R.id.forecastimg3,
                R.id.forecastimg4,
                R.id.forecastimg5)

            val arraytemp = listOf(
                R.id.forecasttemp1,
                R.id.forecasttemp2,
                R.id.forecasttemp3,
                R.id.forecasttemp4,
                R.id.forecasttemp5)

            val arraydate = listOf(
                R.id.widgetday1,
                R.id.widgetday2,
                R.id.widgetday3,
                R.id.widgetday4,
                R.id.widgetday5)


            var time = f[0].getInt(0)
            val weatherdate = SimpleDateFormat("dd", Locale(Locale.getDefault().displayLanguage))
            val weatherDay = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))

            for(i in 0 until f.size){
                val dayofWeekText = weatherDay.format( Date(time*1000L)).substring(0,3)
                val dayofMonth = weatherdate.format( Date(time*1000L)).toInt()
                time += (24*3600)

                val icon = if(blackBg) f[i].getInt(4) else f[i].getInt(1)
                v.setInt(arrayimg[i], "setBackgroundResource", icon)
                v.setTextViewText(arraytemp[i],tool.roundto(tool.kelvintoTempUnit(f[i].getString(2),tempunit))
                        +"/"
                        +tool.roundto(tool.kelvintoTempUnit(f[i].getString(3), tempunit)) + tempunit)
                v.setTextViewText(arraydate[i],"$dayofWeekText $dayofMonth")
            }
        }

    }

}

