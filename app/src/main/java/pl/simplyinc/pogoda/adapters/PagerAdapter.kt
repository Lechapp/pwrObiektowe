package pl.simplyinc.pogoda.adapters

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import pl.simplyinc.pogoda.config.DataBaseHelper
import pl.simplyinc.pogoda.config.StationTableInfo
import pl.simplyinc.pogoda.elements.SessionPref
import pl.simplyinc.pogoda.fragments.AddLocation
import pl.simplyinc.pogoda.fragments.Weatherinfo

class PagerAdapter(fm: FragmentManager, val context:Context, private val stationsCount:Int) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val frags = arrayOfNulls<Weatherinfo>(stationsCount+1)
    override fun getItem(position: Int): Fragment {
        if(frags[position] == null) {
            frags[position] = Weatherinfo.newInstance(position)
        }

        return when(position){
            in 0 until (stationsCount) -> frags[position]!!
            else -> AddLocation()
        }
    }

    override fun getCount(): Int {
        return stationsCount+1
    }

}