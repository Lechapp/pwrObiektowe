package pl.pwr.pogoda.adapters

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.search_row.view.*
import pl.pwr.pogoda.R
import pl.pwr.pogoda.widget.NewestWeatherConfigureActivity

class WidgetChooseAdapter(val context:Context, private val title:List<String>): androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    private lateinit var lastchoice: ConstraintLayout
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.search_row, parent, false))
    }

    override fun getItemCount(): Int {
        return if(title.isEmpty()) 1 else title.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val street = holder.itemView.city
        val container = holder.itemView.cale

        if(title.isEmpty()){
            street.text = context.getString(R.string.emptyplaces)
        }else{

            street.text = title[position]
            street.setOnClickListener {
                val act = street.context as NewestWeatherConfigureActivity
                act.choosedPlace = position
                val colorValue = ContextCompat.getColor(context, R.color.colorPrimaryaplha)
                container.setBackgroundColor(colorValue)

                if(::lastchoice.isInitialized && container != lastchoice) {
                    val noneColor = ContextCompat.getColor(context, R.color.none)
                    lastchoice.setBackgroundColor(noneColor)
                }
                lastchoice = container
            }
        }
    }


}

