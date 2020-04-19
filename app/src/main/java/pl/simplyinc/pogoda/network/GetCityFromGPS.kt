package pl.simplyinc.pogoda.network

import android.app.Activity
import android.location.Location
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.pogoda.R
import pl.simplyinc.pogoda.activities.SearchWeatherActivity
import pl.simplyinc.pogoda.config.OpenWeather

class GetCityFromGPS {

    fun getCity(act: Activity, loc: Location) {

        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${loc.latitude}&lon=${loc.longitude}&appid=${OpenWeather.APIKey}"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)

                if (response.has("cod")) {
                    if (response.getString("cod") == "200") {
                        val sys = response.getJSONObject("sys")

                        val activity = act as SearchWeatherActivity
                        activity.setCity(
                            response.getString("name"), sys.getString("country"),
                            response.getInt("timezone"), response.getString("id")
                        )

                    }else{
                        Toast.makeText(act, act.getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Response.ErrorListener {
                Toast.makeText(act, act.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)
    }

}