package pl.pwr.pogoda.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.forecast_row.view.*
import org.json.JSONArray
import pl.pwr.pogoda.R
import pl.pwr.pogoda.elements.WeatherTools
import java.text.SimpleDateFormat
import java.util.*

class ForecastAdapter(val context: Context,private val forecast: List<JSONArray>, private val tempunit:String):
    RecyclerView.Adapter<ViewHolder>() {

    var time = forecast[0].getInt(0)

    private val weatherdate = SimpleDateFormat("dd", Locale(Locale.getDefault().displayLanguage))
    private val weatherDay = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))


    private val tool = WeatherTools()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.forecast_row, parent, false))
    }

    override fun getItemCount(): Int {
        return forecast.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nameday = holder.itemView.forecastday
        val iconweather = holder.itemView.forecastimg
        val temp = holder.itemView.forecasttemp
        val container = holder.itemView.daycontainer

        val dayofWeek = weatherDay.format( Date(time*1000L)).substring(0,3)
        val dayofMonth = weatherdate.format( Date(time*1000L)).toInt()

        nameday.text = "$dayofWeek $dayofMonth"
        time += (24*3600)

        iconweather.setImageResource(forecast[position].getInt(1))

        val tempconvert = tool.roundto(tool.kelvintoTempUnit(forecast[position].getString(2), tempunit)) + "/" +
                tool.roundto(tool.kelvintoTempUnit(forecast[position].getString(3), tempunit)) + " $tempunit"
        temp.text = tempconvert

    }
}