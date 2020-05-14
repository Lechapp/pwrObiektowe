package pl.pwr.pogoda.fragments

import android.content.ContentValues
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.provider.BaseColumns
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_weatherinfo.*
import org.json.JSONArray
import org.json.JSONObject
import pl.pwr.pogoda.R
import pl.pwr.pogoda.activities.MainActivity
import pl.pwr.pogoda.activities.SettingsActivity
import pl.pwr.pogoda.adapters.DayByDayAdapter
import pl.pwr.pogoda.adapters.ForecastAdapter
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.config.WeatherTableInfo
import pl.pwr.pogoda.elements.*
import pl.pwr.pogoda.elements.DbQueries
import pl.pwr.pogoda.network.*
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM1 = "position"

class Weatherinfo : Fragment() {

    private lateinit var stationData: JSONObject
    private lateinit var weatherData: JSONObject
    private lateinit var weatherRequest : OpenWeatherRequests
    private var position:Int = -1
    private lateinit var dbQueries: DbQueries
    private var forecastWeatherData = mutableListOf<JSONArray>()
    private var wantToOpenDayByDay = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_weatherinfo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        day_ProgressBar.setType("day")
        night_ProgressBar.setType("night")
        daybydayrec.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        dbQueries = DbQueries(activity!!.applicationContext)
        val allStationCount = dbQueries.getStationCount()

        if(allStationCount > position){
            stationData = dbQueries.getStationData(position)
            weatherData = dbQueries.getWeatherData(stationData.getString(BaseColumns._ID))
            weatherRequest = OpenWeatherRequests(activity!!.applicationContext, stationData.getString(BaseColumns._ID))

            startWeather()
            setClickDayByDay()
            backFromDayByDay()

            refresh.setOnClickListener {
                startWeather()
            }
        }

        settings.setOnClickListener{
            val intent = Intent(activity!!.applicationContext, SettingsActivity::class.java)
            intent.putExtra("position", position)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_PARAM1)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            Weatherinfo().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }

    private fun startWeather(){
        val unixTime = System.currentTimeMillis() / 1000L

        forecastrecycler.layoutManager = LinearLayoutManager(
            activity!!.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        setBackground()
        val gps = GpsLocation(
            activity!!.applicationContext,
            null,
            null,
            this
        )

        if(weatherData.getInt(WeatherTableInfo.ColumnTime) + 1800 <= unixTime){
            if(stationData.getInt(StationTableInfo.ColumnGPS) == 1){
                gps.getLocation()
            }else{
                weatherRequest.getNewestWeatherOpenWeather(stationData.getString(BaseColumns._ID)){
                    setWeather()
                }
            }
        }else{
            if(stationData.getInt(StationTableInfo.ColumnGPS) == 1){
                setWeather()
                gps.getLocation()
            }else{
                setWeather()
            }
        }
        setdayprogress()

        mChart.visibility = View.GONE
        chartProgress.visibility = View.VISIBLE
        weatherRequest.getChartDataOpenWeather(
            stationData.getString(StationTableInfo.ColumnCity),
            stationData.getString(StationTableInfo.ColumnTempUnit),
            "","",
            mChart, swipetutorial
        ){
            chartProgress.visibility = View.GONE

            if(it.size > 0){
                forecastWeatherData = it
                mChart.visibility = View.VISIBLE
                charttitle.visibility = View.VISIBLE
            }
        }

    }

    private fun setdayprogress(){
        val sdf = SimpleDateFormat("dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val currentday = sdf.format(Date()).toInt()

        val saveday = sdf.format(Date(stationData.getLong(StationTableInfo.ColumnSunset)*1000L)).toInt()

        val searchval = if(stationData.getInt(StationTableInfo.ColumnGPS) == 1){
            "lat=${stationData.getString(StationTableInfo.ColumnLatitude)}&lon=${stationData.getString(StationTableInfo.ColumnLongitude)}"
        }else {
            "q=${stationData.getString(StationTableInfo.ColumnCity)}"
        }

        if(saveday != currentday || stationData.getInt(StationTableInfo.ColumnSunset) == 0){
            weatherRequest.getNewestSunset(searchval){
                setProgressBar()
            }
        }else{
            setProgressBar()
        }
    }


    private fun setBackground():Boolean {
        if(!weatherData.has(WeatherTableInfo.ColumnWeatherStatus) || weatherData.getString(WeatherTableInfo.ColumnWeatherStatus).isNullOrEmpty()){
            Handler().postDelayed({
                setBackground()
            }, 300)
            return false
        }

        val img = WeatherTools().setBackgroundOpenWeather(
                    weatherData.getString(WeatherTableInfo.ColumnWeatherStatus),
                    weatherData.getString(WeatherTableInfo.ColumnDescription),
                    stationData.getInt(StationTableInfo.ColumnTimezone),
                    stationData.getInt(StationTableInfo.ColumnSunrise),
                    stationData.getInt(StationTableInfo.ColumnSunset),
                    weatherData.getString(WeatherTableInfo.ColumnRain)
        )

       bgweather.background = ContextCompat.getDrawable(context!!, img)
        return true
    }


    private fun setProgressBar(){

        val risetime = stationData.getInt(StationTableInfo.ColumnSunrise) + stationData.getInt(StationTableInfo.ColumnTimezone)
        val settime = stationData.getInt(StationTableInfo.ColumnSunset) + stationData.getInt(StationTableInfo.ColumnTimezone)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val sunsett = sdf.format(Date((settime)*1000L))
        val sunrisee = sdf.format(Date((risetime)*1000L))
        val unixTime = System.currentTimeMillis() / 1000
        val timeInCity = unixTime + stationData.getInt(StationTableInfo.ColumnTimezone)

        sunsettime.text = sunsett
        sunrisetime.text = sunrisee

        sunsettime.visibility = View.VISIBLE
        sunrisetime.visibility = View.VISIBLE

        val dayprogres:Float
        val nightprogres:Float

        if(timeInCity > settime || timeInCity < risetime){
            dayprogres = 50.toFloat()
            val all = risetime+86400-settime
            var piece = timeInCity - settime
            if(piece < 0)
                piece = all - (risetime-timeInCity)

            nightprogres = ((piece/all.toDouble()) * 50).toFloat()
        }else{
            nightprogres = 0.toFloat()
            val all = settime - risetime
            val piece = timeInCity - risetime

            dayprogres = ((piece/all.toDouble()) * 50).toFloat()
        }


        day_ProgressBar.setProgressWithAnimation(dayprogres)
        if(nightprogres > 0) {
            Handler().postDelayed({
                night_ProgressBar.setProgressWithAnimation(nightprogres)
            },1100)
        }else{
            night_ProgressBar.setProgressWithAnimation(nightprogres)
        }
    }


    fun setWeather(loc:Location? = null):Boolean{
        if(loc != null){
            val oldLocation = Location("")
            oldLocation.latitude = stationData.getDouble(StationTableInfo.ColumnLatitude)
            oldLocation.longitude = stationData.getDouble(StationTableInfo.ColumnLongitude)

            val distance = loc.distanceTo(oldLocation)
            val unixTime = System.currentTimeMillis() / 1000L

            if(distance > 7000){
                val station = ContentValues()
                station.put(StationTableInfo.ColumnLatitude, loc.latitude.toString())
                station.put(StationTableInfo.ColumnLongitude, loc.longitude.toString())
                dbQueries.updateStationData(station, stationData.getString(BaseColumns._ID))

                weatherRequest.getNewestWeatherOpenWeather(stationData.getString(BaseColumns._ID),  loc.latitude.toString(),  loc.longitude.toString()){
                    weatherData = dbQueries.getWeatherData(stationData.getString(BaseColumns._ID))
                    startWeather()
                }

            }else if(weatherData.getInt(WeatherTableInfo.ColumnTime) + 1800 <= unixTime){

                weatherRequest.getNewestWeatherOpenWeather(stationData.getString(BaseColumns._ID),  loc.latitude.toString(),  loc.longitude.toString()){
                    weatherData = dbQueries.getWeatherData(stationData.getString(BaseColumns._ID))
                    startWeather()
                }
            }
        }

        weatherData = dbQueries.getWeatherData(stationData.getString(BaseColumns._ID))
        val tool = WeatherTools()
        citymain.text = stationData.getString(StationTableInfo.ColumnCity)
            .toLowerCase(Locale.getDefault())
            .capitalize()

        if(stationData.getInt(StationTableInfo.ColumnGPS) == 1)
            citymain.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_gps_fixed_white_24dp,0)

        if(!weatherData.has(WeatherTableInfo.ColumnTemp))
            return false
        //temp
        setanimListener(tempout)
        setanimListener(tempoutunit)

        tempoutunit.text = stationData.getString(StationTableInfo.ColumnTempUnit)
        val converttemp = tool.kelvintoTempUnit(weatherData.getString(WeatherTableInfo.ColumnTemp) , stationData.getString(StationTableInfo.ColumnTempUnit))
        tempout.text = converttemp

        //humidity
        val humitext = weatherData.getString(WeatherTableInfo.ColumnHumidity)+"%"
        humiout.text = humitext

        //pressure
        val presstext = weatherData.getString(WeatherTableInfo.ColumnPressure) + "hPa"
        press.text = presstext

        //windspeed
        val windval = tool.mstoWindUnit(weatherData.getString(WeatherTableInfo.ColumnWindSpeed), stationData.getString(StationTableInfo.ColumnWindUnit))
        val windtext = windval + stationData.getString(StationTableInfo.ColumnWindUnit)
        wind.text = windtext

        if(weatherData.getString(WeatherTableInfo.ColumnRain).isNullOrEmpty()) {
            rain.text = weatherData.getString(WeatherTableInfo.ColumnRain)
        }else{
            allrain.visibility = View.GONE
        }
        if(weatherData.getString(WeatherTableInfo.ColumnWindDir).isNullOrEmpty())
            windmark.rotation = (weatherData.getString(WeatherTableInfo.ColumnWindDir)).toFloat()
        else windmark.visibility = View.GONE

        setanimListener(null, null, null, null, winddir, windmark, winddiricon)



        val weatherimage = tool.weatherIconOpenWeather(
            weatherData.getString(WeatherTableInfo.ColumnWeatherStatus),
            weatherData.getString(WeatherTableInfo.ColumnDescription),
            stationData.getInt(StationTableInfo.ColumnSunrise),
            stationData.getInt(StationTableInfo.ColumnSunset),
            stationData.getInt(StationTableInfo.ColumnTimezone),
            true,
            weatherData.getString(WeatherTableInfo.ColumnRain))


        weathericon.background = ContextCompat.getDrawable(activity!!.applicationContext, weatherimage)

        setanimListener(null, linearLayout, null)
        setanimListener(null, null, weathericon)

        return true
    }

    private fun setanimListener(textView: TextView? = null, layout: LinearLayout? = null, image: ImageView? = null, additionalTemp: HorizontalScrollView? = null, windDir: ImageView? = null, windMark: ImageView? = null, windIcon: ImageView? = null){
        val anim = AlphaAnimation(0f, 1f)
        anim.interpolator = DecelerateInterpolator() //add this
        anim.duration = 1000

        anim.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                when {
                    textView != null -> textView.visibility = View.VISIBLE
                    layout != null -> layout.visibility = View.VISIBLE
                    image != null -> image.visibility = View.VISIBLE
                }

                if(additionalTemp != null)
                    additionalTemp.visibility = View.VISIBLE
                else if(windDir != null && windMark != null && windIcon != null) {
                    windDir.visibility = View.VISIBLE
                    windMark.visibility = View.VISIBLE
                    windIcon.visibility = View.VISIBLE
                }
            }
        })
        if(textView != null) {
            textView.startAnimation(anim)
        }else if(layout != null){
            layout.startAnimation(anim)
        }else if(image != null)
            image.startAnimation(anim)
        else if(additionalTemp != null)
            additionalTemp.startAnimation(anim)
        else if(windDir != null && windMark != null && windIcon != null){
            windDir.startAnimation(anim)
            windMark.startAnimation(anim)
            windIcon.startAnimation(anim)
        }
    }


    private fun setClickDayByDay(){

        daybyday.setOnClickListener {
            charttitle.visibility = View.GONE
            val now = System.currentTimeMillis()/1000L
            val forecast = dbQueries.getAllForecast(stationData.getString(BaseColumns._ID))

            if(forecast[0].getInt(0) < (now - 3600)) {

                val city = "q=${stationData.getString(StationTableInfo.ColumnCity)}"

                weatherRequest.getNewestForecast(city){
                    val newForecast = dbQueries.getAllForecast(stationData.getString(BaseColumns._ID))
                    forecastrecycler.adapter = ForecastAdapter(activity!!.applicationContext, newForecast, stationData.getString(StationTableInfo.ColumnTempUnit))
                }
            }else{
                forecastrecycler.adapter = ForecastAdapter(activity!!.applicationContext, forecast, stationData.getString(StationTableInfo.ColumnTempUnit))
            }

            forecastrecycler.visibility = View.VISIBLE

            setDayByDayForecastOpenWeather()

            allmain.visibility = View.GONE
            weathercontainer.visibility = View.GONE
            dayfragment.visibility = View.VISIBLE
            dayfragment.animation = AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.slidein_from_right_to_left)

        }
    }

    private fun setDayByDayForecastOpenWeather() {
        if (forecastWeatherData.size > 10) {
            val adapt = DayByDayAdapter(context!!, stationData, forecastWeatherData)

            daybydayrec.adapter = adapt
            daybydayrec.visibility = View.VISIBLE
        }else wantToOpenDayByDay = true
    }

    fun hideDayByDay():Boolean{
        return if(dayfragment != null && dayfragment.visibility == View.VISIBLE) {
            charttitle.visibility = View.VISIBLE
            dayfragment.visibility = View.GONE
            allmain.visibility = View.VISIBLE
            weathercontainer.visibility = View.VISIBLE
            allmain.animation = AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.slidein_from_left_to_right)
            true
        }else false
    }

    private fun backFromDayByDay(){
        val act = activity as MainActivity

        backToMain.setOnClickListener {
            act.hideListDayByDay()
        }


        val scrollListener = object : RecyclerView.OnScrollListener() {
            var lastItemMemory = 0
            var oneDirection = 0
            var direction = true

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(daybydayrec, newState)
                val lastVisible = (daybydayrec.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                if(lastItemMemory < lastVisible){
                    if(direction){
                        oneDirection++
                    }else{
                        direction = true
                        oneDirection = 0
                    }
                    if(oneDirection >= 2)
                        topBar.visibility = View.GONE

                }else if(lastItemMemory > lastVisible){
                    if(!direction){
                        oneDirection++
                    }else{
                        direction = false
                        oneDirection = 0
                    }

                    if(oneDirection >= 2 || lastVisible < 2)
                        topBar.visibility = View.VISIBLE
                }
                lastItemMemory = lastVisible
            }
        }
        daybydayrec.addOnScrollListener(scrollListener)
    }
}
