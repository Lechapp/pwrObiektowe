package pl.simplyinc.pogoda.elements

import android.content.Context


class SessionPref(context: Context)
{
    private val preference = context.getSharedPreferences("Session", Context.MODE_PRIVATE)

    fun getPref(name: String) : String {
        return preference.getString(name, "")!!
    }

    fun setPref(name: String, value:String) {
        val edit = preference.edit()
        edit.putString(name, value)
        edit.apply()
    }

    fun deletePref(){
        val edit = preference.edit()
        edit.clear()
        edit.apply()
    }

}