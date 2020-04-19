package pl.simplyinc.pogoda.fragments

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.provider.BaseColumns
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_weatherinfo.*
import org.json.JSONArray
import pl.simplyinc.pogoda.R
import pl.simplyinc.pogoda.activities.SettingsActivity
import pl.simplyinc.pogoda.config.DataBaseHelper
import pl.simplyinc.pogoda.config.StationTableInfo
import pl.simplyinc.pogoda.config.WeatherTableInfo
import pl.simplyinc.pogoda.elements.*
import pl.simplyinc.pogoda.network.NewestWeatherRequest


private const val ARG_PARAM1 = "position"

class Weatherinfo : Fragment() {

    private lateinit var stationsCursor: Cursor
    private lateinit var weatherCursor: Cursor
    private lateinit var rootview:View
    private var position:Int = -1
    private var forecastWeatherData = mutableListOf<JSONArray>()
    private lateinit var db:SQLiteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootview = inflater.inflate(R.layout.fragment_weatherinfo, container, false)
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val dbHelper = DataBaseHelper(activity!!.applicationContext)
        db = dbHelper.writableDatabase
        stationsCursor = db.query(StationTableInfo.TableName, null, null, null, null,null, null)

        if(stationsCursor.count > position){
            stationsCursor.moveToPosition(position)
            weatherCursor = db.query(WeatherTableInfo.TableName, null, WeatherTableInfo.ColumnStationID + "=?", arrayOf(stationsCursor.getInt(stationsCursor.getColumnIndex(BaseColumns._ID)).toString()), null,null, null)
            weatherCursor.moveToFirst()

            setBackground()

            val update = NewestWeatherRequest(activity!!, rootview)

            update.getNewestWeatherOpenWeather(stationsCursor){
                weatherCursor = it
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


    private fun setBackground():Boolean {

        if(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnWeatherStatus)).isNullOrEmpty()){
            Handler().postDelayed({
                setBackground()
            }, 300)
            return false
        }

        val img = WeatherTools().setBackgroundOpenWeather(
                    weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnWeatherStatus)),
                    weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnDescription)),
                    stationsCursor.getInt(stationsCursor.getColumnIndex(StationTableInfo.ColumnTimezone)),
                    stationsCursor.getInt(stationsCursor.getColumnIndex(StationTableInfo.ColumnSunrise)),
                    stationsCursor.getInt(stationsCursor.getColumnIndex(StationTableInfo.ColumnSunset)),
                    weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnRain))
        )

       bgweather.background = ContextCompat.getDrawable(context!!, img)
        return true
    }
}
