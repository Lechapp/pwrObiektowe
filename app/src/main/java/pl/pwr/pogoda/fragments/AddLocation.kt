package pl.pwr.pogoda.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_location.*

import pl.pwr.pogoda.R
import pl.pwr.pogoda.activities.SearchWeatherActivity


class AddLocation : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_location, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        addcity.setOnClickListener {
            val addcity = Intent(context, SearchWeatherActivity::class.java)
            startActivity(addcity)
        }

    }
}
