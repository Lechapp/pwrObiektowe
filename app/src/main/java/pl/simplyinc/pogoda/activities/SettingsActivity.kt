package pl.simplyinc.pogoda.activities

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_settings.*
import pl.simplyinc.pogoda.R
import androidx.appcompat.app.AlertDialog
import pl.simplyinc.pogoda.config.DataBaseHelper
import pl.simplyinc.pogoda.config.StationTableInfo


class SettingsActivity : AppCompatActivity() {

    private var position:Int = -1
    private lateinit var cursor:Cursor
    private lateinit var db:SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        position = intent.getIntExtra("position",-1)
        val dbHelper = DataBaseHelper(applicationContext)
        db = dbHelper.writableDatabase
        cursor = db.query(StationTableInfo.TableName, null, null, null, null,null, null)
        cursor.moveToPosition(position)

        settingscity.text = cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnCity))

        setTempunit()

        setWindunit()

        setTitle()

        delete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.deleteTitle))
            val content = "${getString(R.string.deleteContent)} ${cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTitle))}?"
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
        db.close()
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
        snamee.text = cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTitle))
        edittextsname.setText(cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTitle)))
        editsname.setOnClickListener {
            snamee.visibility = View.GONE
            edittextsname.visibility = View.VISIBLE
            editsname.visibility = View.GONE
            apply.visibility = View.VISIBLE
        }
    }

    private fun setTempunit(){
        temptextunit.text = cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTempUnit))

        when(cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnTempUnit))){
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
        windtextunit.text = cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnWindUnit))
        when(cursor.getString(cursor.getColumnIndex(StationTableInfo.ColumnWindUnit))) {
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

        db.update(StationTableInfo.TableName, newStation, BaseColumns._ID + "=?", arrayOf(cursor.getString(cursor.getColumnIndex(BaseColumns._ID))))
    }


    private fun deleteWeather(){
        db.delete(StationTableInfo.TableName, BaseColumns._ID+"=?", arrayOf(cursor.getString(cursor.getColumnIndex(BaseColumns._ID))))
        db.close()
        val intentt = Intent(applicationContext, MainActivity::class.java)
        intentt.putExtra("slideanim", true)
        startActivity(intentt)
        finish()
    }
}
