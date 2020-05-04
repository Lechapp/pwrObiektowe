package pl.simplyinc.pogoda.elements

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import pl.simplyinc.pogoda.activities.SearchWeatherActivity
import pl.simplyinc.pogoda.fragments.Weatherinfo


class GpsLocation(private val c:Context, private val searchActivity: SearchWeatherActivity? = null, private val widget:String? = null, private val weatherInfo:Weatherinfo? = null) {

    private lateinit var locationManager : LocationManager
    private var gpslocation:Location? = null
    private var netlocation:Location? = null
    private var countLoc = 0
    private var counter = 0


    inner class GpsLocationListener(private var gpslocation:Location?, private val netlocation:Location?) :LocationListener{
        override fun onLocationChanged(location: Location?) {
            if (location != null)
            {
                gpslocation = location
                updatelocation(gpslocation,netlocation)
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }

   inner class NetLocationListener(private  var netlocation:Location?,private val gpslocation: Location?) :LocationListener{
        override fun onLocationChanged(location: Location?) {
            if (location != null)
            {
                netlocation = location
                updatelocation(gpslocation, netlocation)
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }
    private val gpsloclis= GpsLocationListener(gpslocation, netlocation)
    private val netloclis = NetLocationListener(netlocation, gpslocation)


    @SuppressLint("MissingPermission")
    fun getLocation(){
        counter = 0
        countLoc = 0
        locationManager = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(hasGps || hasNetwork){
            if(hasGps){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                    0F, gpsloclis)

                val localgps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localgps != null)
                    gpslocation = localgps
            }
            if(hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0F,
                    netloclis)
                val localnet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localnet != null)
                    netlocation = localnet
            }
            updatelocation(gpslocation,netlocation)
        }else{
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            if (intent.resolveActivity(c.packageManager) != null) {
                startActivity(c, intent, null)
            }
        }
    }
    private fun stopListener(){
        locationManager.removeUpdates(netloclis)
        locationManager.removeUpdates(gpsloclis)
    }

    private fun updatelocation(gloc:Location?,nloc:Location?){
        //longitude latitude accuarycy
        if(gloc != null) {
            checkLoc(gloc)
        }
        if(nloc != null) {
            checkLoc(nloc)
        }
    }

    private fun checkLoc(loc:Location){
        counter++

        if(loc.accuracy < (1000 + (counter*150))) {
            countLoc++
        }
        if(countLoc == 2){
            stopListener()


            if(searchActivity !== null){
                searchActivity.setLocation(loc)
            }

            if(weatherInfo !== null){
                weatherInfo.setWeather(loc)
            }

        }
    }

}

