package pl.pwr.pogoda.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.newest_weather_configure.*
import java.lang.Exception
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.view.ViewGroup
import kotlinx.android.synthetic.main.newest_weather.view.*
import kotlinx.android.synthetic.main.newest_weather.view.humidityout
import kotlinx.android.synthetic.main.newest_weather.view.pressure
import kotlinx.android.synthetic.main.newest_weather.view.rainfall
import kotlinx.android.synthetic.main.newest_weather.view.tempout
import kotlinx.android.synthetic.main.newest_weather.view.windspeed
import pl.pwr.pogoda.R
import pl.pwr.pogoda.activities.MainActivity
import pl.pwr.pogoda.adapters.WidgetChooseAdapter
import pl.pwr.pogoda.config.WidgetTableInfo
import pl.pwr.pogoda.elements.DbQueries


class NewestWeatherConfigureActivity : Activity() {
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var choosedPlace = -1
    var transparency = 70
    private var darktheme = false
    private val visibilityelement = HashMap<String,Boolean>()
    private lateinit var widgetlayout: View

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.newest_weather_configure)
        initVisibilityArray()
        val dbQueries = DbQueries(this)
        val title = dbQueries.getWeatherTitles()
        val stationID = dbQueries.getStationIDs()

        val extras = intent.extras
        if (extras != null)
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        //preview
        val container = preview as ViewGroup
        val inflater = LayoutInflater.from(this)
        widgetlayout = inflater.inflate(R.layout.newest_weather, container)


        mylocations.layoutManager = LinearLayoutManager(this)
        mylocations.adapter = WidgetChooseAdapter(this, title)
        if(title.size == 0){
            addwidget.text = getString(R.string.addcity)
            addwidget.setOnClickListener {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }else{
            addwidget.setOnClickListener {

                if(choosedPlace != -1) {

                    transparency *= (255/100)
                    val bg = when(darktheme){
                        true -> "000000"
                        false -> "FFFFFF"
                    }
                    val color = "#"+Integer.toHexString(transparency)+bg
                    val saveColor:Int = try{
                            Color.parseColor(color)
                        }catch (e:Exception){
                            Color.parseColor("#7000000")
                        }


                    val widgetInfo = ContentValues()
                    widgetInfo.put(WidgetTableInfo.ColumnBackground, saveColor)
                    widgetInfo.put(WidgetTableInfo.ColumnThemeBlack, darktheme)
                    widgetInfo.put(WidgetTableInfo.ColumnStationID, stationID[choosedPlace])
                    widgetInfo.put(WidgetTableInfo.ColumnWidgetID, mAppWidgetId)
                    widgetInfo.put(WidgetTableInfo.ColumnTemp, visibilityelement["tempout"])
                    widgetInfo.put(WidgetTableInfo.ColumnHumidity, visibilityelement["humidityout"])
                    widgetInfo.put(WidgetTableInfo.ColumnPressure, visibilityelement["pressure"])
                    widgetInfo.put(WidgetTableInfo.ColumnRainfall, visibilityelement["rainfall"])
                    widgetInfo.put(WidgetTableInfo.ColumnWindSpeed, visibilityelement["windspeed"])
                    widgetInfo.put(WidgetTableInfo.ColumnIcon, visibilityelement["iconw"])
                    dbQueries.addNewWidget(widgetInfo)

                    val appWidgetManager = AppWidgetManager.getInstance(this)
                    NewestWeather.updateAppWidget(this, appWidgetManager, mAppWidgetId)

                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    setResult(RESULT_OK,resultValue)
                    finish()

                }else{
                    Toast.makeText(this,getString(R.string.chooseplace), Toast.LENGTH_SHORT).show()
                }
            }
        }


        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
                transparency = progresValue
                procent.text = "$progresValue %"
                setPreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        blackbg.setOnCheckedChangeListener { _, isChecked ->
            darktheme = isChecked
            setPreview()
        }


    }


    private var previewTheme = false
    private fun setPreview(){

        val trans = ((transparency * 255) / 100)
        val bg = when(darktheme){
            true -> "000000"
            false -> "FFFFFF"
        }
        var trans0 = Integer.toHexString(trans)

        if(trans0.length == 1)
            trans0 = "0$trans0"

        val color = "#$trans0$bg"
        widgetlayout.widget.setBackgroundColor(Color.parseColor(color))

        if(previewTheme != darktheme){
            previewTheme = darktheme
            setTheme()
        }
    }

    private fun setTheme(){
        if(darktheme){
            widgetlayout.tempimage.setBackgroundResource(R.drawable.temp2_w)
            widgetlayout.humidityimg.setBackgroundResource(R.drawable.humidity_w)
            widgetlayout.windspeedimg.setBackgroundResource(R.drawable.wind_w)
            widgetlayout.rainfallimg.setBackgroundResource(R.drawable.umbrella_w)
            widgetlayout.pressureimg.setBackgroundResource(R.drawable.pressure_w)
            widgetlayout.weathericon.setBackgroundResource(R.drawable.cloud_sun_w)
            val white = ContextCompat.getColor(this,R.color.white)
            widgetlayout.title.setTextColor(white)
            widgetlayout.updatedat.setTextColor(white)
            widgetlayout.tempout.setTextColor(white)
            widgetlayout.humidityout.setTextColor(white)
            widgetlayout.pressure.setTextColor(white)
            widgetlayout.rainfall.setTextColor(white)
            widgetlayout.windspeed.setTextColor(white)
            widgetlayout.forecastimg1.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp1.setTextColor(white)
            widgetlayout.widgetday1.setTextColor(white)
            widgetlayout.forecastimg2.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp2.setTextColor(white)
            widgetlayout.widgetday2.setTextColor(white)
            widgetlayout.forecastimg3.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp3.setTextColor(white)
            widgetlayout.widgetday3.setTextColor(white)
            widgetlayout.forecastimg4.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp4.setTextColor(white)
            widgetlayout.widgetday4.setTextColor(white)
            widgetlayout.forecastimg5.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp5.setTextColor(white)
            widgetlayout.widgetday5.setTextColor(white)
        }else{
            widgetlayout.tempimage.setBackgroundResource(R.drawable.temp2_b)
            widgetlayout.humidityimg.setBackgroundResource(R.drawable.humidity_b)
            widgetlayout.windspeedimg.setBackgroundResource(R.drawable.wind_b)
            widgetlayout.rainfallimg.setBackgroundResource(R.drawable.umbrella_b)
            widgetlayout.pressureimg.setBackgroundResource(R.drawable.pressure_b)
            widgetlayout.weathericon.setBackgroundResource(R.drawable.cloud_sun_b)
            val white = ContextCompat.getColor(this,R.color.blackfont)
            widgetlayout.title.setTextColor(white)
            widgetlayout.updatedat.setTextColor(white)
            widgetlayout.tempout.setTextColor(white)
            widgetlayout.humidityout.setTextColor(white)
            widgetlayout.pressure.setTextColor(white)
            widgetlayout.rainfall.setTextColor(white)
            widgetlayout.windspeed.setTextColor(white)
            widgetlayout.forecastimg1.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp1.setTextColor(white)
            widgetlayout.widgetday1.setTextColor(white)
            widgetlayout.forecastimg2.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp2.setTextColor(white)
            widgetlayout.widgetday2.setTextColor(white)
            widgetlayout.forecastimg3.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp3.setTextColor(white)
            widgetlayout.widgetday3.setTextColor(white)
            widgetlayout.forecastimg4.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp4.setTextColor(white)
            widgetlayout.widgetday4.setTextColor(white)
            widgetlayout.forecastimg5.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp5.setTextColor(white)
            widgetlayout.widgetday5.setTextColor(white)
        }
    }

    private fun initVisibilityArray(){
        visibilityelement["tempout"] = true
        visibilityelement["humidityout"] = true
        visibilityelement["rainfall"] = true
        visibilityelement["windspeed"] = true
        visibilityelement["pressure"] = true
        visibilityelement["iconw"] = true


        iconw.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["iconw"] = isChecked
            if(isChecked){
                widgetlayout.weathericon.visibility = View.VISIBLE
            }else widgetlayout.weathericon.visibility = View.GONE
        }
        tempout.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["tempout"] = isChecked
            if(isChecked){
                widgetlayout.alltemp.visibility = View.VISIBLE
            }else widgetlayout.alltemp.visibility = View.GONE
        }
        humidityout.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["humidityout"] = isChecked
            if(isChecked){
                widgetlayout.allhumidity.visibility = View.VISIBLE
            }else widgetlayout.allhumidity.visibility = View.GONE
        }

        rainfall.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["rainfall"] = isChecked
            if(isChecked){
                widgetlayout.rainfall.visibility = View.VISIBLE
                widgetlayout.rainfallimg.visibility = View.VISIBLE
            }else{
                widgetlayout.rainfall.visibility = View.GONE
                widgetlayout.rainfallimg.visibility = View.GONE
            }
        }
        windspeed.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["windspeed"] = isChecked
            if(isChecked){
                widgetlayout.windspeed.visibility = View.VISIBLE
                widgetlayout.windspeedimg.visibility = View.VISIBLE
            }else{
                widgetlayout.windspeed.visibility = View.GONE
                widgetlayout.windspeedimg.visibility = View.GONE
            }
        }
        pressure.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["pressure"] = isChecked
            if(isChecked){
                widgetlayout.pressure.visibility = View.VISIBLE
                widgetlayout.pressureimg.visibility = View.VISIBLE
            }else{
                widgetlayout.pressure.visibility = View.GONE
                widgetlayout.pressureimg.visibility = View.GONE
            }
        }
    }
}

