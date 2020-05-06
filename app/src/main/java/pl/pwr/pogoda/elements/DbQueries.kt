package pl.pwr.pogoda.elements

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import org.json.JSONObject
import pl.pwr.pogoda.config.DataBaseHelper
import pl.pwr.pogoda.config.ForecastsTableInfo
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.config.WeatherTableInfo

class DbQueries(context: Context) {
    private val dbHelper = DataBaseHelper(context)
    private val db = dbHelper.writableDatabase

    fun getWeatherTitles(): ArrayList<String> {
        val title = arrayListOf<String>()

        val cursor = db.query(StationTableInfo.TableName, arrayOf(StationTableInfo.ColumnTitle), null, null, null,null, null)
        cursor.moveToFirst()
        if(cursor.count > 0) {
            title.add(cursor.getString(0))

            while (cursor.moveToNext()) {
                title.add(cursor.getString(0))
            }
        }
        cursor.close()

        return title
    }

    fun getStationCount(): Int {
        val cursor = db.query(StationTableInfo.TableName, null, null, null, null,null, null)
        val stationsCount = cursor.count
        cursor.close()

        return stationsCount
    }

    fun getStationData(position:Int):JSONObject{
        val stationData = JSONObject()

        val cursor = db.query(StationTableInfo.TableName, null, null, null, null,null, null)
        cursor.moveToPosition(position)

        stationData.put(StationTableInfo.ColumnCity, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnCity)))
        stationData.put(StationTableInfo.ColumnTitle, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTitle)))
        stationData.put(StationTableInfo.ColumnTempUnit, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTempUnit)))
        stationData.put(StationTableInfo.ColumnWindUnit, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnWindUnit)))
        stationData.put(StationTableInfo.ColumnTimezone, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTimezone)))
        stationData.put(StationTableInfo.ColumnSunset, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnSunset)))
        stationData.put(StationTableInfo.ColumnGPS, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnGPS)))
        stationData.put(StationTableInfo.ColumnSunrise, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnSunrise)))
        stationData.put(StationTableInfo.ColumnTimezone, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTimezone)))
        stationData.put(StationTableInfo.ColumnLatitude, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnLatitude)))
        stationData.put(StationTableInfo.ColumnLongitude, cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnLongitude)))
        stationData.put(BaseColumns._ID, cursor.getString(cursor.getColumnIndex(BaseColumns._ID)))

        cursor.close()
        return stationData
    }

    fun updateStationData(data:ContentValues, id: String){
        db.update(StationTableInfo.TableName, data, BaseColumns._ID + "=?", arrayOf(id))
    }

    fun deleteStation(id:String){
        db.delete(StationTableInfo.TableName, BaseColumns._ID+"=?", arrayOf(id))
    }

    fun updateWeatherData(newWeather:ContentValues, id:String){
        db.update(WeatherTableInfo.TableName, newWeather, WeatherTableInfo.ColumnStationID + "=?", arrayOf(id))
    }

    fun getStationValue(id:String, value:String) : String{
        val city:String
        val cursor = db.query(StationTableInfo.TableName, null, BaseColumns._ID + "=?", arrayOf(id), null,null, null)
        cursor.moveToFirst()
        city = cursor.getString(cursor.getColumnIndex(value))

        cursor.close()
        return city
    }


    fun getWeatherData(stationID:String) :JSONObject{
        val weatherData = JSONObject()
        val weatherCursor = db.query(WeatherTableInfo.TableName, null, WeatherTableInfo.ColumnStationID + "=?", arrayOf(stationID), null,null, null)
        weatherCursor.moveToFirst()
        weatherData.put(WeatherTableInfo.ColumnWeatherStatus, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnWeatherStatus)))
        weatherData.put(WeatherTableInfo.ColumnDescription, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnDescription)))
        weatherData.put(WeatherTableInfo.ColumnRain, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnRain)))
        weatherData.put(WeatherTableInfo.ColumnTime, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnTime)))
        weatherData.put(WeatherTableInfo.ColumnTemp, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnTemp)))
        weatherData.put(WeatherTableInfo.ColumnWindDir, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnWindDir)))
        weatherData.put(WeatherTableInfo.ColumnWindSpeed, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnWindSpeed)))
        weatherData.put(WeatherTableInfo.ColumnHumidity, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnHumidity)))
        weatherData.put(WeatherTableInfo.ColumnPressure, weatherCursor.getString(weatherCursor.getColumnIndex(WeatherTableInfo.ColumnPressure)))

        weatherCursor.close()
        return weatherData
    }

    fun addNewStation(stationData:ContentValues) : Int{
        val weatherData = ContentValues()
        val stationID = db.insertOrThrow(StationTableInfo.TableName, null, stationData).toInt()
        weatherData.put(ForecastsTableInfo.ColumnStationID, stationID)

        db.insertOrThrow(WeatherTableInfo.TableName, null, weatherData)

        for(i in 0..4) {
            weatherData.put(ForecastsTableInfo.ColumnDayNumber, i)
            db.insertOrThrow(ForecastsTableInfo.TableName, null, weatherData)
        }

        return stationID
    }
}