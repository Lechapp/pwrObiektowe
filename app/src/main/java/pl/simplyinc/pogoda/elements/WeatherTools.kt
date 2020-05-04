package pl.simplyinc.pogoda.elements

import android.util.Log
import pl.simplyinc.pogoda.R
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WeatherTools {

    fun roundto(value:String):String{
        if(value == "null")
            return "null"

        var valu = "null"
        try{
            val w = value.toDouble()
            valu = (w.roundToInt()).toString()

        }catch (e: Exception){}
        return valu
    }

    fun kelvintoTempUnit(number:String,tempunit:String):String{

        val numb:Double

        if(number == "null")
            return "null"
        else {
            try {
                numb = number.toDouble()
                val temp = when (tempunit) {
                    "°F" -> (numb * 1.8) - 459.67
                    "°C" -> (numb - 273.15)
                    else -> numb
                }

                return ((temp * 10).roundToInt() / 10.0).toString()
            }catch (e: Exception){ }

            return "null"
        }
    }

    fun mstoWindUnit(number:String, windunit: String):String{

        val numb:Double
        if(number == "null")
            return "null"
        else {
            try {
                numb = number.toDouble()
                val windspeed = when (windunit) {
                    "km/h" -> (numb * 3.6)
                    "mph" -> (numb * (3600 / 1609.344))
                    else -> numb
                }

                return ((windspeed * 10).roundToInt() / 10.0).toString()
            }catch (e: Exception){}
        }

        return "null"
    }

    fun getTempImgId(temp:String, black:Boolean):Int{

        var number = -1
        try {
            number = temp.toInt()
        }catch (e: Exception){}

        return when(black){
            true -> when(number){
                in 309..2001 -> R.drawable.temp4_w
                in 293..309 -> R.drawable.temp3_w
                in 280..293 -> R.drawable.temp2_w
                in 265..280 -> R.drawable.temp1_w
                in 0..265 -> R.drawable.temp0_w
                else -> R.drawable.temp2_w
            }
            false -> when(number){
                    in 309..2001 -> R.drawable.temp4_b
                    in 293..309 -> R.drawable.temp3_b
                    in 280..293 -> R.drawable.temp2_b
                    in 265..280 -> R.drawable.temp1_b
                    in 0..265 -> R.drawable.temp0_b
                    else -> R.drawable.temp2_b
                }
        }
    }

    fun setBackgroundOpenWeather(mainn:String, descriptionn:String, timezone:Int, sunrisee: Int, sunsett: Int, rainn:String):Int{

        var main:String = mainn
        var description:String = descriptionn


        if(rainn != "null") {
            try{
                val rain = rainn.toInt()
                if(rain < 5){
                    main = "Clouds"
                    description = "few clouds"
                }else if(rain < 11){
                    main = "Clouds"
                    description = "broken clouds"
                }
            }catch (e:Exception){ }
        }
        val background:Int

        val sunrise = sunrisee + timezone
        val sunset = sunsett + timezone

        //get time with day of sunrise and time of now
        val dayMonthYearSDF = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dayMonthYearSDF.timeZone = TimeZone.getTimeZone("GMT")
        val dayMonthYear = dayMonthYearSDF.format(Date(sunrise * 1000L))

        val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        nowTime.timeZone = TimeZone.getTimeZone("GMT")
        val nowHourMinutes = nowTime.format(Date(System.currentTimeMillis() + (timezone*1000)))

        val nowDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            nowDate.timeZone = TimeZone.getTimeZone("GMT")
        val now = nowDate.parse("$dayMonthYear $nowHourMinutes").time/1000

        if(now in (sunrise-2100)..(sunrise+2100) || now in (sunset-2100)..(sunset+2100)){
            //sunet 0-4
            val z = (0..4).random()
            background = when(z){
                0 -> R.drawable.sun_sunset_0
                1 -> R.drawable.sun_sunset_1
                2 -> R.drawable.sun_sunset_2
                3 -> R.drawable.sun_sunset_3
                4 -> R.drawable.sun_sunset_4
                else -> R.drawable.sun_sunset_3
            }
        }else if((now > sunset || now < sunrise) && sunset != 0){
            //moon 0-3
            val z = (0..3).random()
            background = when (z) {
                0 -> R.drawable.moon_0
                1 -> R.drawable.moon_1
                2 -> R.drawable.moon_2
                3 -> R.drawable.moon_3
                else -> R.drawable.moon_3
            }
        }else{

            when(main){
                "Clear" -> {
                    val z = (0..4).random()
                    background = when(z){
                        0 -> R.drawable.sun_0
                        1 -> R.drawable.sun_1
                        2 -> R.drawable.sun_2
                        3 -> R.drawable.sun_3
                        4 -> R.drawable.sun_4
                        else -> R.drawable.sun_4
                    }
                }
                "Clouds" -> {
                    when(description){
                        "few clouds", "scattered clouds" -> {
                            val z = (0..3).random()
                            background = when(z){
                                0 -> R.drawable.sun_cloud_0
                                1 -> R.drawable.sun_cloud_1
                                2 -> R.drawable.sun_cloud_2
                                3 -> R.drawable.sun_cloud_3
                                else -> R.drawable.sun_cloud_2
                            }
                        }
                        else ->{
                            val z = (0..5).random()
                            background = when(z){
                                0 -> R.drawable.cloud_0
                                1 -> R.drawable.cloud_1
                                2 -> R.drawable.cloud_2
                                3 -> R.drawable.cloud_3
                                4 -> R.drawable.cloud_4
                                5 -> R.drawable.cloud_5
                                else -> R.drawable.cloud_2
                            }
                        }

                    }
                }
                "Rain", "Drizzle", "Thunderstorm" -> {
                    val z = (0..4).random()
                    background = when(z){
                        0 -> R.drawable.rain_0
                        1 -> R.drawable.rain_1
                        2 -> R.drawable.rain_2
                        3 -> R.drawable.rain_3
                        4 -> R.drawable.dew_0
                        else -> R.drawable.rain_0
                    }
                }
                "Snow" -> {
                    val w = (0..2).random()
                    background = when(w){
                        0 -> R.drawable.snow_0
                        1 -> R.drawable.snow_1
                        2 -> R.drawable.snow_2
                        else -> R.drawable.snow_1
                    }
                }
                else -> {
                    val z = (0..5).random()
                    background = when(z){
                        0 -> R.drawable.cloud_0
                        1 -> R.drawable.cloud_1
                        2 -> R.drawable.cloud_2
                        3 -> R.drawable.cloud_3
                        4 -> R.drawable.cloud_4
                        5 -> R.drawable.cloud_5
                        else -> R.drawable.cloud_2
                    }
                }
            }
        }

        return background
    }


    fun weatherIconOpenWeather(mainn:String,descriptionn: String, sunrisee:Int, sunsett:Int, timezone:Int, black:Boolean, rainn:String, timeDate:Int = 0):Int{

        val icon:Int
        val systemtime = System.currentTimeMillis()/1000L
        val systemtimezone = (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings)/1000L
        val weathertimetoday = if(timeDate == 0){
            systemtime - systemtimezone + timezone
        }else{
            timeDate.toLong()
        }

        val subDay = SimpleDateFormat("D", Locale.getDefault())
        subDay.timeZone = TimeZone.getTimeZone("GMT")
        val sunset = sunsett + timezone
        val sunrise = sunrisee + timezone
        val today = subDay.format(Date(weathertimetoday * 1000L)).toInt()
        val sunriseDay = subDay.format(Date(sunrise*1000L)).toInt()
        val substractDay = today - sunriseDay
        val main:String = mainn
        val description:String = descriptionn

        var rain = 0

        if(rainn != "null") {
            try{
                rain = rainn.toInt()
            }catch (e:Exception){ }
        }

        if((weathertimetoday - (substractDay * 3600*24) in sunrise-3000..sunset+3000)){
            when(main){
                "Clear" -> {
                    icon = when (black) {
                        true -> R.drawable.sun_w
                        false -> R.drawable.sun_b
                    }
                }
                "Clouds" -> {
                    icon = when(description) {
                        "few clouds" -> {
                            when (black) {
                                true -> R.drawable.little_cloud_sun_w
                                false -> R.drawable.little_cloud_sun_b
                            }
                        }
                        "scattered clouds", "broken clouds" -> {
                            when (black) {
                                true -> R.drawable.cloud_sun_w
                                false -> R.drawable.cloud_sun_b
                            }
                        }
                        else -> {
                            when (black) {
                                true -> R.drawable.cloud_w
                                false -> R.drawable.cloud_b
                            }
                        }
                    }
                }
                "Rain", "Drizzle", "Thunderstorm" -> {
                    icon = if(description.contains("light")) {
                        if(rain < 9){
                            when (black) {
                                true -> R.drawable.cloud_sun_w
                                false -> R.drawable.cloud_sun_b
                            }
                        }else {
                            when (black) {
                                true -> R.drawable.cloud_sun_little_rain_w
                                false -> R.drawable.cloud_sun_little_rain_b
                            }
                        }
                    }else {
                        when (black) {
                            true -> R.drawable.cloud_sun_rain_w
                            false -> R.drawable.cloud_sun_rain_b
                        }
                    }
                }
                "Snow" -> {
                    icon = if(rain < 9){
                        when (black) {
                            true -> R.drawable.cloud_w
                            false -> R.drawable.cloud_b
                        }
                    }else{
                        when (black) {
                            true -> R.drawable.cloud_snow_w
                            false -> R.drawable.cloud_snow_b
                        }
                    }
                }
                else -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_w
                        false -> R.drawable.cloud_b
                    }
                }
            }
        }else{
            when(main){
                "Clear" -> {
                    icon = when (black) {
                        true -> R.drawable.moon_w
                        false -> R.drawable.moon_b
                    }
                }
                "Clouds" -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_moon_w
                        false -> R.drawable.cloud_moon_b
                    }
                }
                "Rain", "Drizzle", "Thunderstorm" -> {
                    icon = if(description.contains("light")) {
                        if(rain < 9){
                            when (black) {
                                true -> R.drawable.cloud_moon_w
                                false -> R.drawable.cloud_moon_b
                            }
                        }else {
                            when (black) {
                                true -> R.drawable.cloud_moon_little_rain_w
                                false -> R.drawable.cloud_moon_little_rain_b
                            }
                        }
                    }else{
                        when (black) {
                            true -> R.drawable.cloud_moon_rain_w
                            false -> R.drawable.cloud_moon_rain_b
                        }
                    }
                }
                "Snow" -> {
                    icon = if(rain < 9){
                        when (black) {
                            true -> R.drawable.cloud_moon_w
                            false -> R.drawable.cloud_moon_b
                        }
                    }else{
                        when (black) {
                            true -> R.drawable.cloud_moon_snow_w
                            false -> R.drawable.cloud_moon_snow_b
                        }
                    }
                }
                else -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_moon_w
                        false -> R.drawable.cloud_moon_b
                    }
                }
            }
        }

        return icon
    }
}
