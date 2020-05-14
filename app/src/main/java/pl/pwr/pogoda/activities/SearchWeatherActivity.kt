package pl.pwr.pogoda.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_search_weather.*
import pl.pwr.pogoda.R
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.elements.*
import pl.pwr.pogoda.network.OpenWeatherRequests


@Suppress("IMPLICIT_CAST_TO_ANY")
class SearchWeatherActivity : AppCompatActivity() {

    private var permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val PERMISION_REQUEST = 10
    private val gpsManager = GpsLocation(this, this)
    private var searchedCity = ""
    private var searchedcountry = ""
    private var searchedid = ""
    private var searchedtimezone = 0
    private var loc:Location? = null
    var countcallback = 0
    var countcallfun = 0
    private lateinit var searchCity:OpenWeatherRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.addcity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_search_weather)


        searchCity = OpenWeatherRequests(this, "0")

        searchrecycler.layoutManager = LinearLayoutManager(this)
        progressBarSearch.visibility = View.GONE

        search.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->

            val city = ReplaceSign.removePolishSign(search.text.trim().toString())

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                countcallfun = 0
                countcallback = 0

                searcherror.text = ""

                if(city.length < 3) {
                    searcherror.text = getString(R.string.searchempty)
                }else if(countcallback == countcallfun){

                    progressBarSearch.visibility = View.VISIBLE
                    searchCity.searchFromOpenWeather(city, searchrecycler){
                        progressBarSearch.visibility = View.GONE

                        when (it) {
                            2 -> searcherror.text = getString(R.string.error)
                            1 -> searcherror.text = getString(R.string.wrongcity)
                            0 -> showtutorial()
                        }
                    }
                }

                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)

                return@OnKeyListener true
            }else false
        })

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val city = ReplaceSign.removePolishSign(search.text.trim().toString())

                if(city.length > 3) {
                    countcallback++
                    Handler().postDelayed({
                        countcallfun++
                        searchCity.searchFromOpenWeather(city, searchrecycler){
                            progressBarSearch.visibility = View.GONE

                            when (it) {
                                2 -> searcherror.text = getString(R.string.error)
                                1 -> searcherror.text = getString(R.string.wrongcity)
                                0 -> showtutorial()
                            }
                        }

                    }, 1300)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        gpsicon.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this,R.anim.fadein)
            anim.duration = 450
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkPermission(permission)){
                    gpsManager.getLocation()
                    confirm.visibility = View.VISIBLE
                    confirm.startAnimation(anim)
                }else{
                    requestPermissions(permission, PERMISION_REQUEST)
                }
            }else{
                gpsManager.getLocation()
                confirm.visibility = View.VISIBLE
                confirm.startAnimation(anim)
            }
        }

        buttonokay.setOnClickListener {
            if(loc != null) {
                createPlace()
                Toast.makeText(this, getString(R.string.waitfor), Toast.LENGTH_SHORT).show()
            }
        }

        buttonexit.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this,R.anim.fadeout)
            anim.duration = 450
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    confirm.visibility = View.GONE
                }
            })
            confirm.startAnimation(anim)
        }
    }

    fun setLocation(l:Location){
        loc = l
        searchCity.getCityFromGps(this,l)
    }

    fun setCity(newCity:String, country:String, timezone:Int, id:String=""){
        searchedCity = newCity
        searchedcountry = country
        searchedtimezone = timezone
        searchedid = id
        foundcity.text = newCity
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(checkPermission(permission)) {
            gpsManager.getLocation()
            val anim = AnimationUtils.loadAnimation(this,R.anim.fadein)
            anim.duration = 450
            confirm.visibility = View.VISIBLE
            confirm.startAnimation(anim)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun checkPermission(perarray:Array<String>):Boolean{
        var success = true

        for (i in perarray.indices){
            if(checkCallingOrSelfPermission(perarray[i]) == PackageManager.PERMISSION_DENIED)
                success = false
        }
        return success
    }

    private fun createPlace(){

        val stationData = ContentValues()

        stationData.put(StationTableInfo.ColumnCity, searchedCity)
        stationData.put(StationTableInfo.ColumnTitle, searchedCity)
        stationData.put(StationTableInfo.ColumnTimezone, searchedtimezone)
        stationData.put(StationTableInfo.ColumnLatitude, loc?.latitude)
        stationData.put(StationTableInfo.ColumnLongitude, loc?.longitude)
        stationData.put(StationTableInfo.ColumnGPS, true)

        val stationID = DbQueries(applicationContext).addNewStation(stationData)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("setweather", stationID)
        startActivity(intent)
        finish()

    }

    private fun showtutorial(){
        if(SessionPref(this).getPref("mapstutorial") != "true" && tutorial.visibility == View.GONE) {
            val anim = AnimationUtils.loadAnimation(this, R.anim.slidein_frombottom)
            anim.duration = 1000
            tutorial.visibility = View.VISIBLE
            tutorial.startAnimation(anim)
        }
    }

}
