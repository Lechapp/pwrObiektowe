package pl.pwr.pogoda.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_settings.*
import pl.pwr.pogoda.R
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject
import pl.pwr.pogoda.config.StationTableInfo
import pl.pwr.pogoda.elements.DbQueries


class SettingsActivity : AppCompatActivity() {

    private var position:Int = -1
    private lateinit var dbQueries: DbQueries
    private lateinit var stationData:JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        position = intent.getIntExtra("position",-1)
        dbQueries = DbQueries(this)
        stationData = dbQueries.getStationData(position)

        settingscity.text = stationData.getString(StationTableInfo.ColumnCity)

        setTempunit()

        setWindunit()

        setTitle()

        delete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.deleteTitle))
            val content = "${getString(R.string.deleteContent)} ${stationData.getString(StationTableInfo.ColumnTitle)}?"
            builder.setMessage(content)
            builder.setPositiveButton(getString(R.string.yes)){_, _ ->
                deleteWeather()
            }

            builder.setNegativeButton(getString(R.string.no)){_,_->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


        apply.setOnClickListener {

            val selectedwindUnit = findViewById<RadioButton>(wtempunit.checkedRadioButtonId).text.toString()
            val selectedtempUnit = findViewById<RadioButton>(stempunit.checkedRadioButtonId).text.toString()
            val newtitle = edittextsname.text.trim().toString()


            saveStation(selectedwindUnit, selectedtempUnit, newtitle)

            windtextunit.text = selectedwindUnit
            editwunit.visibility = View.VISIBLE
            windtextunit.visibility = View.VISIBLE
            wtempunit.visibility = View.GONE

            snamee.text = newtitle
            snamee.visibility = View.VISIBLE
            edittextsname.visibility = View.GONE
            editsname.visibility = View.VISIBLE

            temptextunit.text = selectedtempUnit
            edittunit.visibility = View.VISIBLE
            temptextunit.visibility = View.VISIBLE
            stempunit.visibility = View.GONE


            apply.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        exit()
    }

    private fun exit(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("setweather", position)
        intent.putExtra("slideanim", true)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home){
            exit()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setTitle(){
        snamee.text = stationData.getString(StationTableInfo.ColumnTitle)
        edittextsname.setText(stationData.getString(StationTableInfo.ColumnTitle))
        editsname.setOnClickListener {
            snamee.visibility = View.GONE
            edittextsname.visibility = View.VISIBLE
            editsname.visibility = View.GONE
            apply.visibility = View.VISIBLE
        }
    }

    private fun setTempunit(){
        temptextunit.text = stationData.getString(StationTableInfo.ColumnTempUnit)

        when(stationData.getString(StationTableInfo.ColumnTempUnit)){
            "°C" -> sC.isChecked = true
            "°F" -> sF.isChecked = true
            else -> sK.isChecked = true
        }

        edittunit.setOnClickListener {
            edittunit.visibility = View.GONE
            temptextunit.visibility = View.GONE
            apply.visibility = View.VISIBLE
            stempunit.visibility = View.VISIBLE
        }

    }

    private fun setWindunit(){
        windtextunit.text = stationData.getString(StationTableInfo.ColumnWindUnit)
        when(stationData.getString(StationTableInfo.ColumnWindUnit)) {
            "km/h" -> skmh.isChecked = true
            "mph" -> smph.isChecked = true
            else -> sms.isChecked = true
        }

            editwunit.setOnClickListener {
            editwunit.visibility = View.GONE
            windtextunit.visibility = View.GONE
            apply.visibility = View.VISIBLE
            wtempunit.visibility = View.VISIBLE
        }
    }



    private fun saveStation(windunit:String, tempunit:String, title:String){

        val newStation = ContentValues()
        newStation.put(StationTableInfo.ColumnTempUnit, tempunit)
        newStation.put(StationTableInfo.ColumnWindUnit, windunit)
        newStation.put(StationTableInfo.ColumnTitle, title)

       dbQueries.updateStationData(newStation, stationData.getString(BaseColumns._ID))
    }


    private fun deleteWeather(){
        dbQueries.deleteStation(stationData.getString(BaseColumns._ID))
        val intentt = Intent(applicationContext, MainActivity::class.java)
        intentt.putExtra("slideanim", true)
        startActivity(intentt)
        finish()
    }
}
