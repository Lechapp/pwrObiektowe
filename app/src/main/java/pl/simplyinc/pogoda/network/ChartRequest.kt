package pl.simplyinc.pogoda.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.Toast
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
import pl.simplyinc.pogoda.R
import pl.simplyinc.pogoda.config.OpenWeather
import pl.simplyinc.pogoda.elements.WeatherTools
import pl.simplyinc.pogoda.elements.OnLeftRightTouchListener
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ChartRequest(private val c:Context, private val mChart:CombinedChart, private val swipe : ImageView) {


    fun getChartDataOpenWeather(city:String, tempunit:String, lat:String = "", lon:String = "", onSuccess: (data:MutableList<JSONArray>) -> Unit){
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


                setChart(weatherData, tempunit)
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
    private fun setChart(weatherData:JSONArray, tempunit:String) {
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