package pl.simplyinc.pogoda.config

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

    private object DataBaseInfo{
        const val name = "Weather"
        const val version = 10
    }

    object WeatherTableInfo: BaseColumns{
        const val TableName = "WeatherData"
        const val ColumnTemp = "Temperature"
        const val ColumnTempImg = "TempImg"
        const val ColumnHumidity = "Humidity"
        const val ColumnPressure = "Pressure"
        const val ColumnWindSpeed = "Windspeed"
        const val ColumnRain = "Rainfall"
        const val ColumnWindDir = "Winddir"
        const val ColumnTime = "WeatherTime"
        const val ColumnUpdatedTime = "UpdatedTime"
        const val ColumnDescription = "WeatherDescription"
        const val ColumnWeatherStatus = "Main"
        const val ColumnStationID = "StationID"
    }

    object StationTableInfo : BaseColumns{
        const val TableName = "StationsData"
        const val ColumnTitle = "Title"
        const val ColumnCity = "City"
        const val ColumnGPS = "GPS"
        const val ColumnTimezone = "Timezone"
        const val ColumnTempUnit = "TempUnit"
        const val ColumnWindUnit = "WindUnit"
        const val ColumnSunset = "Sunset"
        const val ColumnSunrise = "Sunrise"
        const val ColumnLatitude = "Latitude"
        const val ColumnLongitude = "Longitude"
    }

    object ForecastsTableInfo: BaseColumns{
        const val TableName = "Forecast"
        const val ColumnTempMin = "TempMin"
        const val ColumnTempMax = "TempMax"
        const val ColumnTime = "Time"
        const val ColumnIcon = "Icon"
        const val ColumnIconBlack = "IconBlack"
        const val ColumnStationID = "StationID"
        const val ColumnDayNumber = "DayNumber"
    }


    object WidgetTableInfo: BaseColumns{
        const val TableName = "WidgetData"
        const val ColumnStationID = "StationID"
        const val ColumnBackground = "Background"
        const val ColumnThemeBlack = "ThemeBlack"
        const val ColumnTemp = "Temperature"
        const val ColumnHumidity = "Humidity"
        const val ColumnPressure = "Pressure"
        const val ColumnRainfall = "Rainfall"
        const val ColumnWindSpeed = "Windspeed"
        const val ColumnInsolation = "Insolation"
        const val ColumnIcon = "WeatherIcon"
        const val ColumnWidgetID = "WidgetID"
    }

    private object BasicCommand{
        const val SQL_CREATE_TABLE_WEATHER : String = "CREATE TABLE IF NOT EXISTS `${WeatherTableInfo.TableName}` (" +
                "`${BaseColumns._ID}` INTEGER PRIMARY KEY," +
                "`${WeatherTableInfo.ColumnDescription}` VARCHAR," +
                "`${WeatherTableInfo.ColumnWeatherStatus}` VARCHAR," +
                "`${WeatherTableInfo.ColumnHumidity}` UNSIGNED SMALLINT," +
                "`${WeatherTableInfo.ColumnPressure}` UNSIGNED MEDIUMINT," +
                "`${WeatherTableInfo.ColumnTemp}` SMALLINT," +
                "`${WeatherTableInfo.ColumnTempImg}` INT," +
                "`${WeatherTableInfo.ColumnRain}` SMALLINT," +
                "`${WeatherTableInfo.ColumnTime}` UNSIGNED INT DEFAULT 0," +
                "`${WeatherTableInfo.ColumnUpdatedTime}` UNSIGNED INT DEFAULT 0," +
                "`${WeatherTableInfo.ColumnWindDir}` UNSIGNED SMALLINT," +
                "`${WeatherTableInfo.ColumnWindSpeed}` UNSIGNED SMALLINT," +
                "`${WeatherTableInfo.ColumnStationID}` UNSIGNED INT" +
                ")"

        const val SQL_CREATE_TABLE_STATIONS: String = "CREATE TABLE IF NOT EXISTS `${StationTableInfo.TableName}` (" +
                "`${BaseColumns._ID}` INTEGER PRIMARY KEY," +
                "`${StationTableInfo.ColumnCity}` VARCHAR," +
                "`${StationTableInfo.ColumnTitle}` VARCHAR," +
                "`${StationTableInfo.ColumnGPS}` BOOLEAN DEFAULT FALSE," +
                "`${StationTableInfo.ColumnTempUnit}` VARCHAR DEFAULT '°C'," +
                "`${StationTableInfo.ColumnTimezone}` SMALLINT," +
                "`${StationTableInfo.ColumnWindUnit}` VARCHAR DEFAULT 'km/h'," +
                "`${StationTableInfo.ColumnLatitude}` DOUBLE," +
                "`${StationTableInfo.ColumnLongitude}` DOUBLE," +
                "`${StationTableInfo.ColumnSunrise}` UNSIGNED INT DEFAULT 0," +
                "`${StationTableInfo.ColumnSunset}` UNSIGNED INT DEFAULT 0" +
                ")"

        const val SQL_CREATE_TABLE_FORECAST: String = "CREATE TABLE IF NOT EXISTS `${ForecastsTableInfo.TableName}` (" +
                "`${BaseColumns._ID}` INTEGER PRIMARY KEY," +
                "`${ForecastsTableInfo.ColumnTempMin}` SMALLINT," +
                "`${ForecastsTableInfo.ColumnTempMax}` SMALLINT," +
                "`${ForecastsTableInfo.ColumnTime}` INT DEFAULT 0," +
                "`${ForecastsTableInfo.ColumnIcon}` INT," +
                "`${ForecastsTableInfo.ColumnIconBlack}` INT," +
                "`${ForecastsTableInfo.ColumnDayNumber}` TINYINT," +
                "`${ForecastsTableInfo.ColumnStationID}` UNSIGNED INT" +
                ")"

        const val SQL_CREATE_TABLE_WIDGETS: String = "CREATE TABLE IF NOT EXISTS `${WidgetTableInfo.TableName}` (" +
                "`${BaseColumns._ID}` INTEGER PRIMARY KEY," +
                "`${WidgetTableInfo.ColumnBackground}` UNSIGNED INT," +
                "`${WidgetTableInfo.ColumnWindSpeed}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnHumidity}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnIcon}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnPressure}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnTemp}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnRainfall}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnThemeBlack}` BOOLEAN," +
                "`${WidgetTableInfo.ColumnStationID}` UNSIGNED INT," +
                "`${WidgetTableInfo.ColumnWidgetID}` UNSIGNED INT" +
                ")"

        const val SQL_DELETE_TABLE_WEATHER:String = "DROP TABLE IF EXISTS `${WeatherTableInfo.TableName}`;"
        const val SQL_DELETE_TABLE_STATIONS:String = "DROP TABLE IF EXISTS `${StationTableInfo.TableName}`;"
        const val SQL_DELETE_TABLE_FORECAST:String = "DROP TABLE IF EXISTS `${ForecastsTableInfo.TableName}`;"
        const val SQL_DELETE_TABLE_WIDGETS:String = "DROP TABLE IF EXISTS `${WidgetTableInfo.TableName}`;"
    }

    class DataBaseHelper(context: Context) : SQLiteOpenHelper(context, DataBaseInfo.name, null, DataBaseInfo.version) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(BasicCommand.SQL_CREATE_TABLE_WEATHER)
            db.execSQL(BasicCommand.SQL_CREATE_TABLE_STATIONS)
            db.execSQL(BasicCommand.SQL_CREATE_TABLE_FORECAST)
            db.execSQL(BasicCommand.SQL_CREATE_TABLE_WIDGETS)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(BasicCommand.SQL_DELETE_TABLE_WEATHER)
            db.execSQL(BasicCommand.SQL_DELETE_TABLE_STATIONS)
            db.execSQL(BasicCommand.SQL_DELETE_TABLE_FORECAST)
            db.execSQL(BasicCommand.SQL_DELETE_TABLE_WIDGETS)

            onCreate(db)
        }

    }
