package pl.simplyinc.pogoda.network

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.pogoda.R
import pl.simplyinc.pogoda.activities.SearchWeatherActivity
import pl.simplyinc.pogoda.adapters.SearchWeatherAdapter
import pl.simplyinc.pogoda.config.OpenWeather
import pl.simplyinc.pogoda.elements.SessionPref

class SearchCity(private val act:SearchWeatherActivity, private val searcherror:TextView, private val progressBarSearch:ProgressBar,
                 private val searchrecycler: RecyclerView, private val tutorial:TextView) {

    fun searchFromOpenWeather(city:String):Boolean{

        if(city.length < 3) {
            searcherror.text = act.getString(R.string.searchempty)
            return false
        }else{
            searcherror.text = ""
            //only last call is execute
            if(act.countcallback != act.countcallfun)
                return false
        }
        progressBarSearch.visibility = View.VISIBLE
        val url = "https://api.openweathermap.org/data/2.5/find?q=$city&appid=${OpenWeather.APIKey}"
        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->

                val response = JSONObject(res)


                if(response.has("cod")) {
                    var ids = ""
                    val names = mutableListOf<String>()
                    if (response.getString("cod") == "200") {
                        if (response.getString("count") == "0") {
                            searcherror.text = act.getString(R.string.wrongcity)
                            progressBarSearch.visibility = View.GONE
                        } else {
                            val list = response.getJSONArray("list")
                            for (i in 0 until list.length()) {
                                val data = list.getJSONObject(i)
                                ids += (if (i > 0) "," else "") + data.getString("id")
                                names.add(data.getString("name"))
                            }
                            setCitiesFromWeatherApi(ids, names)

                        }

                        showtutorial()
                    } else {
                        searcherror.text = response.getString("message")
                        progressBarSearch.visibility = View.GONE
                    }
                }else{
                    searchFromOpenWeather(city)
                }
            },
            Response.ErrorListener {
                progressBarSearch.visibility = View.GONE
                searcherror.text = act.getString(R.string.error)
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)

        return true
    }
    private fun showtutorial(){
        if(SessionPref(act).getPref("mapstutorial") != "true" && tutorial.visibility == View.GONE) {
            val anim = AnimationUtils.loadAnimation(act, R.anim.slidein_frombottom)
            anim.duration = 1000
            tutorial.visibility = View.VISIBLE
            tutorial.startAnimation(anim)
        }
    }


    private fun setCitiesFromWeatherApi(ids:String, names:MutableList<String>){
        val url = "https://api.openweathermap.org/data/2.5/group?id=$ids&appid=${OpenWeather.APIKey}"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->
                progressBarSearch.visibility = View.GONE

                val response = JSONObject(res)


                if(response.has("cod") || response.has("list")) {
                    if (response.has("list")) {
                        val list = response.getJSONArray("list")

                        val cities = mutableListOf<String>()
                        val countries = mutableListOf<String>()
                        val coords = mutableListOf<String>()
                        val numberstation = mutableListOf<String>()
                        val timezone = mutableListOf<Int>()

                        for (i in 0 until list.length()) {
                            val data = list.getJSONObject(i)

                            val sys = data.getJSONObject("sys")
                            timezone.add(sys.getInt("timezone"))
                            countries.add(sys.getString("country"))
                            cities.add(data.getString("name"))
                            numberstation.add(data.getString("id"))

                            val onecoord = data.getJSONObject("coord")
                            coords.add(onecoord.getString("lat") + "," + onecoord.getString("lon"))

                        }

                        val adaptersearch = SearchWeatherAdapter(
                            cities, countries, timezone, act, coords
                        )
                        searchrecycler.adapter = adaptersearch

                    } else {
                        searcherror.text = act.getString(R.string.error)
                    }
                }else{
                    setCitiesFromWeatherApi(ids, names)
                }
            },
            Response.ErrorListener {
                progressBarSearch.visibility = View.GONE
                searcherror.text = act.getString(R.string.error)
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)

    }

}