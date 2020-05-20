package pl.pwr.pogoda.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.one_day_row.view.*
import org.json.JSONArray
import org.json.JSONObject
import pl.pwr.pogoda.R
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.elements.WeatherTools
import java.text.SimpleDateFormat
import java.util.*


class DayByDayAdapter(val context:Context, private val station : JSONObject, private val forecast:MutableList<JSONArray>): RecyclerView.Adapter<ViewHolder>() {

    private lateinit var tunit:String
    lateinit var wunit:String

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        wunit = station.getString(StationTableInfo.ColumnWindUnit)
        tunit =  station.getString(StationTableInfo.ColumnTempUnit)
        return ViewHolder(layoutInflater.inflate(R.layout.one_day_row, parent, false))
    }

    override fun getItemCount(): Int {
        return forecast.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val weather = forecast[position]
        val tool = WeatherTools()
        val tempout = holder.itemView.daytempout
        val humiout = holder.itemView.dayhumiout
        val press = holder.itemView.daypress
        val wind = holder.itemView.daywind
        val rain = holder.itemView.dayrain
        val date = holder.itemView.dayday
        val icon = holder.itemView.dayicon
        val wmark = holder.itemView.windmarkk

        val dateformat = "HH:mm dd.MM"


        val weatherdate = SimpleDateFormat(dateformat, Locale(Locale.getDefault().displayLanguage))
        weatherdate.timeZone = TimeZone.getTimeZone("GMT")

        val dayinweek = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))
        dayinweek.timeZone = TimeZone.getTimeZone("GMT")
        val dayOfWeek = dayinweek.format(Date(weather.getInt(5)*1000L))

        val alldate = "${weatherdate.format(Date(weather.getInt(5)*1000L))}, $dayOfWeek"
        date.text = alldate

        val tempoutt = tool.kelvintoTempUnit(weather.getString(0), tunit)
        val temp = "$tempoutt $tunit"
        tempout.text = temp


        val tempText = weather.getString(1) + " %"
        humiout.text = tempText



        val pressu = weather.getString(2) + " hPa"
        press.text = pressu

        val rainT = weather.getString(3) + " %"
        rain.text = rainT


        val t =  weather.getString(4) + " $wunit"
        wind.text = t


        wmark.rotation = (weather.getInt(10)).toFloat()



        val iconimg = tool.weatherIconOpenWeather(
            weather.getString(8),
            weather.getString(9),
            station.getInt(StationTableInfo.ColumnSunrise),
            station.getInt(StationTableInfo.ColumnSunset),
            station.getInt(StationTableInfo.ColumnTimezone),
            true,  weather.getString(3),
            weather.getInt(5)
        )


        icon.setImageResource(iconimg)

    }
}