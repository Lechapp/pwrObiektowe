package pl.simplyinc.pogoda.adapters

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.search_row.view.*
import pl.simplyinc.pogoda.R
import pl.simplyinc.pogoda.activities.MainActivity
import pl.simplyinc.pogoda.elements.SessionPref
import java.util.*
import android.net.Uri
import pl.simplyinc.pogoda.config.DataBaseHelper
import pl.simplyinc.pogoda.config.ForecastsTableInfo
import pl.simplyinc.pogoda.config.StationTableInfo
import pl.simplyinc.pogoda.config.WeatherTableInfo
import pl.simplyinc.pogoda.elements.DbQueries


@Suppress("IMPLICIT_CAST_TO_ANY")
class SearchWeatherAdapter(private val cities: MutableList<String>, private val countries: MutableList<String>, val timezone:MutableList<Int>, val context: Context, private val coord:MutableList<String>): RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.search_row, parent, false))
    }

    override fun getItemCount(): Int {
        return if (cities.size == 0) 1 else cities.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = holder.itemView.city
        val country = holder.itemView.country
        val showmaps = holder.itemView.showmaps
        val flag = holder.itemView.flag


        if(cities.size == 0){

            showmaps.visibility = View.GONE
            city.text = context.getString(R.string.emptysearch)

        }else{

            val url = "drawable/" + countries[position].toLowerCase(Locale.getDefault())
            val imageKey = context.resources.getIdentifier(url, "drawable", context.packageName)
            flag.setImageResource(imageKey)

            country.text = countries[position]
            city.text = cities[position]


            city.setOnClickListener {
                val stationData = ContentValues()

                stationData.put(StationTableInfo.ColumnCity, cities[position])
                stationData.put(StationTableInfo.ColumnTitle, cities[position])
                stationData.put(StationTableInfo.ColumnTimezone, timezone[position])
                stationData.put(StationTableInfo.ColumnGPS, false)

                val stationID = DbQueries(context).addNewStation(stationData)


                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("setweather", stationID)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

             showmaps.setOnClickListener {
                 val session = SessionPref(context)
                 val gmmIntentUri = Uri.parse("geo:${coord[position]}?z=6&q=${coord[position]}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

                 session.setPref("mapstutorial", "true")
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    }
                }
        }
    }
}
