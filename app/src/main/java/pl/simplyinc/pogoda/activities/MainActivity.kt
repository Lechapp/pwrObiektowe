package pl.simplyinc.pogoda.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import pl.simplyinc.pogoda.adapters.StationNameAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import pl.simplyinc.pogoda.R
import android.database.sqlite.SQLiteDatabase
import androidx.viewpager.widget.ViewPager
import pl.simplyinc.pogoda.adapters.PagerAdapter
import pl.simplyinc.pogoda.config.DataBaseHelper
import pl.simplyinc.pogoda.config.StationTableInfo
import pl.simplyinc.pogoda.elements.DbQueries


class MainActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: PagerAdapter
    lateinit var adaptername:StationNameAdapter
    lateinit var dbQueries: DbQueries

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbQueries = DbQueries(this)


        if(intent.getBooleanExtra("slideanim", false))
            overridePendingTransition(R.anim.slidein_from_left_to_right, R.anim.activity_slide_out_right)


        supportActionBar?.hide()
        pagerAdapter = PagerAdapter(supportFragmentManager, this, dbQueries.getStationCount())
        weathers.adapter = pagerAdapter

        setTitleBar()

        checkactiveposition()

        weathers.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                adaptername.setActive(position)
            }
        })
    }

    override fun onBackPressed() {

    }

    private fun setTitleBar(){
        val title = dbQueries.getWeatherTitles()

        title.add(getString(R.string.addcity))

        adaptername = StationNameAdapter(title,this, weathers)
        namerecycler.adapter = adaptername
        namerecycler.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

    }

    private fun checkactiveposition(){
        val weatherposition = intent.getIntExtra("setweather", -1)

        if(weatherposition != -1){
            adaptername.setActive(weatherposition)
            weathers.setCurrentItem(weatherposition,true)
        }
    }

}