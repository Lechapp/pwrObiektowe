package pl.pwr.pogoda.network

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONArray
import org.json.JSONObject
import pl.pwr.pogoda.R
import pl.pwr.pogoda.activities.SearchWeatherActivity
import pl.pwr.pogoda.adapters.SearchWeatherAdapter
import pl.pwr.pogoda.config.ForecastsTableInfo
import pl.pwr.pogoda.config.OpenWeather
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.config.WeatherTableInfo
import pl.pwr.pogoda.elements.DbQueries
import pl.pwr.pogoda.elements.OnLeftRightTouchListener
import pl.pwr.pogoda.elements.SessionPref
import pl.pwr.pogoda.elements.WeatherTools
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class OpenWeatherRequests(private val c: Context, private val stationID:String) {

    private val tool = WeatherTools()
    private val dbQueries = DbQueries(c)

    fun getNewestWeatherOpenWeather(lat:String = "", lon:String = "", onSuccess: (finishRequest: Boolean) -> Unit) {

        val searchvalue = if(lat != "") {
            "lat=$lat&lon=$lon"
        }else {
            "q=${dbQueries.getStationValue(stationID, StationTableInfo.ColumnCity)}"
        }
        val url = "https://api.openweathermap.org/data/2.5/weather?$searchvalue&appid=${OpenWeather.APIKey}"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener { res ->

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

                dbQueries.updateWeatherData(newWeather, stationID)


                val sys = response.getJSONObject("sys")

                saveNewStation(
                    response.getString("name"),
                    sys.getInt("sunset"),
                    sys.getInt("sunrise"),
                    stationID,
                    response.getInt("timezone"),
                    lat, lon
                )

            }else{

            }
            onSuccess(true)
        },
            Response.ErrorListener {
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

    fun getCityFromGps(act: Activity, loc: Location) {

        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${loc.latitude}&lon=${loc.longitude}&appid=${OpenWeather.APIKey}"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)

                if (response.has("cod")) {
                    if (response.getString("cod") == "200") {
                        val sys = response.getJSONObject("sys")

                        val activity = act as SearchWeatherActivity
                        activity.setCity(
                            response.getString("name"), sys.getString("country"),
                            response.getInt("timezone"), response.getString("id")
                        )

                    }else{
                        Toast.makeText(act, act.getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Response.ErrorListener {
                Toast.makeText(act, act.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)
    }


    fun getNewestForecast(city:String, onSuccess: (finish: Boolean) -> Unit) {

        val url = "https://api.openweathermap.org/data/2.5/forecast?$city&appid=${OpenWeather.APIKey}"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)


            if (response.has("cod") && response.getString("cod") == "200") {

                val timezone = response.getJSONObject("city").getInt("timezone")
                val timeforecast = (System.currentTimeMillis()/1000).toInt()

                val weathers = response.getJSONArray("list")

                val weatherdate = SimpleDateFormat("dd", Locale(Locale.getDefault().displayLanguage))
                var oneday = "0"
                var tempmin = 999
                var tempmax = 0
                var iconid:List<Int>
                var minweather:Int
                var maxweather:Int
                var icon:String
                var time:Int
                var day:String
                var licznik = 0
                var dayCount = 0
                var arrayiconOpenWeather = arrayListOf(0,0,0,0,0,0,0,0,0)

                for(i in 0 until weathers.length()){
                    licznik++
                    try {
                        val obj = weathers.getJSONObject(i)
                        time = obj.getInt("dt")
                        day = weatherdate.format(Date(time * 1000L))
                        if(oneday == "0")
                            oneday = day

                        val weatherdata = obj.getJSONObject("main")
                        icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon")
                        minweather = weatherdata.getDouble("temp_min").roundToInt()
                        maxweather = weatherdata.getDouble("temp_max").roundToInt()
                    }catch (e:Exception){
                        continue
                    }

                    if(tempmin > minweather)
                        tempmin = minweather

                    if(tempmax < maxweather)
                        tempmax = maxweather

                    when("${icon[0]}${icon[1]}"){
                        "01"-> arrayiconOpenWeather[0]++
                        "02"-> arrayiconOpenWeather[1]++
                        "03"-> arrayiconOpenWeather[3]++
                        "04"-> arrayiconOpenWeather[4]++
                        "09"-> arrayiconOpenWeather[7]++
                        "10"-> arrayiconOpenWeather[6]++
                        "13"-> arrayiconOpenWeather[8]++
                        else -> arrayiconOpenWeather[3]++
                    }

                    if(oneday != day) {
                        oneday = day
                        arrayiconOpenWeather[5] = arrayiconOpenWeather[1] + arrayiconOpenWeather[3] + arrayiconOpenWeather[4]
                        arrayiconOpenWeather[2] = arrayiconOpenWeather[0] + arrayiconOpenWeather[1]

                        iconid = getIcon(arrayiconOpenWeather)
                        arrayiconOpenWeather = arrayListOf(0,0,0,0,0,0,0,0,0)


                        val forecastData = ContentValues()
                        forecastData.put(ForecastsTableInfo.ColumnTempMin, tempmin)
                        forecastData.put(ForecastsTableInfo.ColumnTempMax, tempmax)
                        forecastData.put(ForecastsTableInfo.ColumnIcon, iconid[0])
                        forecastData.put(ForecastsTableInfo.ColumnIconBlack, iconid[1])
                        forecastData.put(ForecastsTableInfo.ColumnTime, timeforecast)
                        forecastData.put(ForecastsTableInfo.ColumnDayNumber, dayCount++)

                        dbQueries.saveForecast(forecastData, stationID)

                        tempmin = 999
                        tempmax = 0
                        licznik = 0

                    }

                }

                arrayiconOpenWeather[5] = arrayiconOpenWeather[1] + arrayiconOpenWeather[3] + arrayiconOpenWeather[4]
                arrayiconOpenWeather[2] = arrayiconOpenWeather[0] + arrayiconOpenWeather[1]
                iconid = getIcon(arrayiconOpenWeather)
                //the last one
                val forecastData = ContentValues()
                forecastData.put(ForecastsTableInfo.ColumnTempMin, tempmin)
                forecastData.put(ForecastsTableInfo.ColumnTempMax, tempmax)
                forecastData.put(ForecastsTableInfo.ColumnIcon, iconid[0])
                forecastData.put(ForecastsTableInfo.ColumnIconBlack, iconid[1])
                forecastData.put(ForecastsTableInfo.ColumnTime, timeforecast)
                forecastData.put(ForecastsTableInfo.ColumnDayNumber, dayCount)

                //save
                dbQueries.saveForecast(forecastData, stationID)

            }

            onSuccess(true)
        },
            Response.ErrorListener {
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }


    private fun getIcon(arrayIcon:ArrayList<Int>):List<Int>{
        //0 - sun, 1 - little_cloud_sun, 2 - cloud_sun, 3 - cloud, 4 - cloud_rain, 5 - cloud_little_rain, 6 - cloud_snow

        //5 suma wszystkich chmur
        //2 suma slonca i slonca z chmurką małą

        val maxValue = arrayIcon.max()
        val allmaxs = ArrayList<Int>()
        while (arrayIcon.indexOf(maxValue) != -1 && arrayIcon[arrayIcon.indexOf(maxValue)] != 0){
            allmaxs.add(arrayIcon.indexOf(maxValue))
            arrayIcon[arrayIcon.indexOf(maxValue)] = 0
        }

        //for sure
        if(allmaxs.size < 2)
            allmaxs.add(0)

        val icon = if(allmaxs.size > 1) ((allmaxs[0] + allmaxs[1] - 0.01)/2.0).roundToInt() else allmaxs[0]

        return when(icon){
            0 -> listOf(R.drawable.sun_w, R.drawable.sun_b)
            1,2 -> listOf(R.drawable.little_cloud_sun_w, R.drawable.little_cloud_sun_b)
            3 -> listOf(R.drawable.cloud_sun_w, R.drawable.cloud_sun_b)
            4,5 -> listOf(R.drawable.cloud_w, R.drawable.cloud_b)
            6 -> listOf(R.drawable.cloud_little_rain_w, R.drawable.cloud_little_rain_b)
            7 -> listOf(R.drawable.cloud_rain_w, R.drawable.cloud_rain_b)
            8 -> listOf(R.drawable.cloud_snow_w, R.drawable.cloud_snow_b)
            else -> listOf(R.drawable.cloud_w, R.drawable.cloud_b)
        }

    }


    fun searchFromOpenWeather(city:String, searchrecycler: RecyclerView, onSuccess: (errorCode :Int) -> Unit){


        val url = "https://api.openweathermap.org/data/2.5/find?q=$city&appid=${OpenWeather.APIKey}"
        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->

                val response = JSONObject(res)


                if(response.has("cod")) {
                    var ids = ""
                    val names = mutableListOf<String>()
                    if (response.getString("cod") == "200") {
                        if (response.getString("count") == "0") {
                                onSuccess(1)
                        }else{
                            val list = response.getJSONArray("list")
                            for (i in 0 until list.length()) {
                                val data = list.getJSONObject(i)
                                ids += (if (i > 0) "," else "") + data.getString("id")
                                names.add(data.getString("name"))
                            }
                            setCitiesFromWeatherApi(ids, names, searchrecycler){
                                onSuccess(it)
                            }
                        }

                    }else{
                        onSuccess(2)
                    }
                }
            },
            Response.ErrorListener {
                onSuccess(2)
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }


    private fun setCitiesFromWeatherApi(ids:String, names:MutableList<String>, searchrecycler:RecyclerView, onSuccess: (errorCode: Int) -> Unit){
        val url = "https://api.openweathermap.org/data/2.5/group?id=$ids&appid=${OpenWeather.APIKey}"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->

                val response = JSONObject(res)


                if(response.has("cod") || response.has("list")) {

                        val list = response.getJSONArray("list")

                        val cities = mutableListOf<String>()
                        val countries = mutableListOf<String>()
                        val coords = mutableListOf<String>()
                        val numberstation = mutableListOf<String>()
                        val timezone = mutableListOf<Int>()

                        for (i in 0 until list.length()) {
                            val data = list.getJSONObject(i)

                            val sys = data.getJSONObject("sys")
                            timezone.add(sys.getInt("timezone"))
                            countries.add(sys.getString("country"))
                            cities.add(data.getString("name"))
                            numberstation.add(data.getString("id"))

                            val onecoord = data.getJSONObject("coord")
                            coords.add(onecoord.getString("lat") + "," + onecoord.getString("lon"))

                        }

                        val adaptersearch = SearchWeatherAdapter(
                            cities, countries, timezone, c, coords
                        )
                        searchrecycler.adapter = adaptersearch
                        onSuccess(0)
                }else{
                    onSuccess(2)
                }
            },
            Response.ErrorListener {
                onSuccess(2)
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)

    }

    fun getNewestSunset(searchval:String, onSuccess: (finishRequest: Boolean) -> Unit) {

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
                    saveSunset(c,sunset,sunrise, stationID)
                    onSuccess(true)
                }catch (e:Exception){}
            }

        },
            Response.ErrorListener {
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }

    private fun saveSunset(c:Context,sunset:Int, sunrise:Int, stationID : String){
        val newStation = ContentValues()
        newStation.put(StationTableInfo.ColumnSunset,sunset)
        newStation.put(StationTableInfo.ColumnSunrise, sunrise)

        DbQueries(c).updateStationData(newStation, stationID)
    }


    fun getChartDataOpenWeather(city:String, tempunit:String, lat:String = "", lon:String = "", mChart:CombinedChart, swipe: ImageView, onSuccess: (data:MutableList<JSONArray>) -> Unit){
        val allforecast = mutableListOf<JSONArray>()
        val searchval = if(lat == "") "q=$city" else "lat=$lat&lon=$lon"

        val url = "https://api.openweathermap.org/data/2.5/forecast?$searchval&appid=${OpenWeather.APIKey}"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)

            if (response.has("cod") && response.getString("cod") == "200") {
                val list = response.getJSONArray("list")
                val timezone = response.getJSONObject("city").getInt("timezone")
                val endloop = list.length()-1
                val weatherData = JSONArray()

                for(i in 0 until endloop){
                    val pack = list.getJSONObject(endloop-i-1)
                    val onePack = JSONArray()
                    val forecast = JSONArray()
                    val main = pack.getJSONObject("main")
                    val rain = try {
                        pack.getJSONObject("rain").getDouble("3h")
                    }catch (e:Exception){
                        0
                    }
                    onePack.put(main.getString("temp"))
                    onePack.put(rain)

                    forecast.put(main.getString("temp"))
                    forecast.put(main.getString("humidity"))
                    forecast.put(main.getString("pressure"))
                    forecast.put(rain)
                    forecast.put(pack.getJSONObject("wind").getString("speed"))
                    forecast.put(pack.getString("dt"))
                    forecast.put(main.getString("temp_min"))
                    forecast.put(main.getString("temp_max"))
                    forecast.put(pack.getJSONArray("weather").getJSONObject(0).getString("main"))
                    forecast.put(pack.getJSONArray("weather").getJSONObject(0).getString("description"))
                    forecast.put(pack.getJSONObject("wind").getString("deg"))

                    val date = (pack.getInt("dt") + timezone) * 1000L

                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dayinweek = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))

                    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
                    dayinweek.timeZone = TimeZone.getTimeZone("GMT")
                    val textdate = dateFormat.format(date) + " " + dayinweek.format(date)
                    onePack.put(textdate)
                    weatherData.put(onePack)
                    allforecast.add(forecast)
                }


                setChart(weatherData, tempunit, mChart, swipe)
            }else{
                Toast.makeText(c, c.getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
            onSuccess(allforecast)

        },
            Response.ErrorListener {
                onSuccess(allforecast)
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setChart(weatherData:JSONArray, tempunit:String, mChart:CombinedChart, swipe: ImageView) {
        val xAxis = mChart.xAxis
        val templist: ArrayList<Entry> = ArrayList()
        val rainlist: ArrayList<BarEntry> = ArrayList()
        val xValsDateLabel = ArrayList<String>()
        val tool = WeatherTools()

        var prevHour = 0
        var countDataPack = 0


        for (i in 0 until weatherData.length()) {
            val fromEnd = weatherData.length() - i - 1

            val onePack = weatherData.getJSONArray(fromEnd)

            //vertical day line
            val limeoffset = 2.401f

            if (i > 3) {
                val hour = ((onePack.getString(2).split(" ")[0]).split(":")[0]).toInt()
                countDataPack++

                if (prevHour > hour) {
                    var dayOfWeek = onePack.getString(2).split(" ")[1]

                    val checkTypeDay = dayOfWeek.toIntOrNull()
                    if(checkTypeDay !== null){
                        dayOfWeek = when(checkTypeDay){
                            1 -> c.getString(R.string.fmonday)
                            2 -> c.getString(R.string.ftuesday)
                            3 -> c.getString(R.string.fwednesday)
                            4 -> c.getString(R.string.fthursday)
                            5 -> c.getString(R.string.ffriday)
                            6 -> c.getString(R.string.fsaturday)
                            7 -> c.getString(R.string.fsunday)
                            else -> c.getString(R.string.dayafterday)
                        }

                    }

                    val limlinee = LimitLine(((countDataPack * 1f) + limeoffset), dayOfWeek)
                    limlinee.textColor = ContextCompat.getColor(c, R.color.white)
                    limlinee.enableDashedLine(6f, 6f, 2f)
                    xAxis.addLimitLine(limlinee)
                }
                prevHour = hour
            }
            val temp = tool.kelvintoTempUnit(onePack.getString(0), tempunit).toFloat()
            templist.add(Entry(i.toFloat(), temp))

            val rainToProcent = 100 * (onePack.getString(1).toFloat() / 12)

            rainlist.add(BarEntry(i.toFloat(), rainToProcent))
            xValsDateLabel.add(onePack.getString(2).split(" ")[0])
        }

        val tempDataSet = LineDataSet(templist, "Temperature")
        tempDataSet.color = ContextCompat.getColor(c, R.color.sunrise)
        tempDataSet.lineWidth = 2f
        tempDataSet.valueTextSize = 10.5f
        tempDataSet.valueTextColor = ContextCompat.getColor(c, R.color.white)
        tempDataSet.setDrawCircles(false)
        tempDataSet.valueFormatter = MyDecimalValueFormatter()
        tempDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val linearTemp = LineData(tempDataSet)

        val rainDataSet = BarDataSet(rainlist, "Rainfall")
        rainDataSet.color = ContextCompat.getColor(c, R.color.rainfall)
        rainDataSet.setDrawValues(false)
        rainDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        val barRainfall = BarData(rainDataSet)


        val combinedData = CombinedData()
        combinedData.setData(barRainfall)
        combinedData.setData(linearTemp)

        mChart.data = combinedData
        mChart.axisLeft.setDrawLabels(false)
        mChart.axisRight.setDrawLabels(false)
        mChart.setTouchEnabled(false)
        val description = Description()
        description.text = ""
        mChart.description = description
        mChart.legend.isEnabled = false
        mChart.axisLeft.setDrawGridLines(false)
        mChart.axisRight.setDrawGridLines(false)
        mChart.axisLeft.setDrawAxisLine(false)
        mChart.axisRight.setDrawAxisLine(false)
        mChart.axisLeft.axisMinimum = mChart.axisLeft.axisMinimum - 3
        mChart.axisLeft.axisMaximum = mChart.axisLeft.axisMaximum + 6
        mChart.axisRight.axisMaximum = 105f
        mChart.axisRight.axisMinimum = 0f

        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.mAxisMaximum = combinedData.xMax + 0.25f
        xAxis.valueFormatter = MyValueFormatter(xValsDateLabel)
        xAxis.textColor = ContextCompat.getColor(c, R.color.white)

        mChart.visibility = View.VISIBLE
        mChart.animateX(325)
        mChart.animateY(325)
        mChart.setVisibleXRangeMaximum(13f)

        val startline = 0f
        val limline = LimitLine(startline, c.getString(R.string.today))
        mChart.moveViewToX(startline)

        limline.textColor = ContextCompat.getColor(c, R.color.white)
        limline.enableDashedLine(6f,6f,2f)
        xAxis.addLimitLine(limline)

        val listeer = OnLeftRightTouchListener(c, mChart, swipe)
        mChart.setOnTouchListener(listeer)


    }

    class MyValueFormatter(private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return value.toString()
        }

        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            return if (value.toInt() >= 0 && value.toInt() <= xValsDateLabel.size - 1)
                xValsDateLabel[value.toInt()]
            else
                ("").toString()

        }
    }


    class MyDecimalValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return  value.roundToInt().toString()
        }
    }

}