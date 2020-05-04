package pl.simplyinc.pogoda.elements

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.GestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import com.github.mikephil.charting.charts.CombinedChart
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import pl.simplyinc.pogoda.R




class OnLeftRightTouchListener(private val ctx: Context, private val mChart: CombinedChart, private val swipeIcon:ImageView) : OnTouchListener {

    private val gestureDetector: GestureDetector
    private var swipetutorial = true
    private var busy = false
    private val max = 24

    init {
        if(SessionPref(ctx).getPref("swipetutorial") == "false")
            swipetutorial = false
        else {
            val aniFade = AnimationUtils.loadAnimation(ctx, R.anim.fadein)
            aniFade.duration = 3000
            swipeIcon.visibility = View.VISIBLE
            swipeIcon.startAnimation(aniFade)
        }
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {

        val dx: Float

        when(event.action){

                MotionEvent.ACTION_UP -> {
                    val x2 = event.x
                    val y2 = event.y
                    dx = x2-y2
                }
                else -> {
                    return true
                }
            }
            if(dx > 1){
                onClickRight()
            }else {
                onClickLeft()
            }
            return true
    }

    private inner class GestureListener : SimpleOnGestureListener() {

    }

    var xstatus = 0f

    private fun onClickLeft() {
        if(!busy) {
            busy = true
            if (swipetutorial) {
                swipetutorial = false
                val aniFade = AnimationUtils.loadAnimation(ctx, R.anim.fadeout)
                aniFade.duration = 1500
                aniFade.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {}
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        swipeIcon.visibility = View.GONE
                    }
                })
                swipeIcon.startAnimation(aniFade)
                SessionPref(ctx).setPref("swipetutorial", "false")
            }

            if (xstatus > 0f)
                xstatus -= 12f
            else busy = false
            mChart.moveViewToAnimated(xstatus, 0f, mChart.axisLeft.axisDependency, 1000)
            Handler().postDelayed({
                busy = false
            }, 1000)
        }
    }


    private fun onClickRight() {
        if(!busy) {
            busy = true
            if (swipetutorial) {
                swipetutorial = false
                val aniFade = AnimationUtils.loadAnimation(ctx, R.anim.fadeout)
                aniFade.duration = 1500
                aniFade.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {}
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        swipeIcon.visibility = View.GONE
                    }
                })
                swipeIcon.startAnimation(aniFade)
                SessionPref(ctx).setPref("swipetutorial", "false")
            }
            if (xstatus < max)
                xstatus += 12f
            else busy = false

            mChart.moveViewToAnimated(xstatus, 0f, mChart.axisLeft.axisDependency, 1000)
            Handler().postDelayed({
                busy = false
            }, 1000)
        }
    }
}
