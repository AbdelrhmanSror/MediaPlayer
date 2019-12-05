package com.example.mediaplayer.customViews

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.example.mediaplayer.Event
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.MinEqualizerBinding
import java.util.function.LongFunction
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random


class EqualizerView : LinearLayout {
    private lateinit var binding: MinEqualizerBinding
    private lateinit var playingSet: AnimatorSet
    private lateinit var stopSet: AnimatorSet
    private var isAnimating = false
    private var foregroundColor = 0
    private var duration = 0

    /*private val bars: Array<ObjectAnimator> by lazy {
        getBar()
    }*/

    constructor(context: Context?) : super(context) {
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setAttrs(context, attrs)
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setAttrs(context, attrs)
        initViews()
    }

    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        val a: TypedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.mini_equalizer,
                0, 0)
        try {
            foregroundColor = a.getResourceId(R.styleable.mini_equalizer_foregroundColor, Color.BLACK)
            duration = a.getInt(R.styleable.mini_equalizer_animDuration, 3000)
        } finally {
            a.recycle()
        }
    }

    private fun initViews() {
        // inflating the layout using data binding
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(inflater, R.layout.min_equalizer, this, true)
        binding.musicBar1.setColor(foregroundColor)
        binding.musicBar2.setColor(foregroundColor)
        binding.musicBar3.setColor(foregroundColor)
        binding.musicBar4.setColor(foregroundColor)
        binding.musicBar5.setColor(foregroundColor)

        setPivots()
    }

    private fun ImageView.setColor(colorRefId: Int) {
        setColorFilter(ContextCompat.getColor(context, colorRefId))

    }

    private fun View.setPivot() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @SuppressLint("ObsoleteSdkInt")
            override fun onGlobalLayout() {
                if (this@setPivot.height > 0) {
                    this@setPivot.pivotY = this@setPivot.height.toFloat()
                    if (Build.VERSION.SDK_INT >= 16) {
                        this@setPivot.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            }
        })
    }

    private fun setPivots() {
        binding.musicBar1.setPivot()
        binding.musicBar2.setPivot()
        binding.musicBar3.setPivot()
        binding.musicBar4.setPivot()
        binding.musicBar5.setPivot()
    }

   /* private fun getBar(): Array<ObjectAnimator> {
        val scaleYbar1 = ObjectAnimator.ofFloat(binding.musicBar1, "scaleY", 0.2f, 0.8f, 0.1f, 0.1f, 0.3f, 0.1f, 0.2f, 0.8f, 0.7f, 0.2f, 0.4f, 0.9f, 0.7f, 0.6f, 0.1f, 0.3f, 0.1f, 0.4f, 0.1f, 0.8f, 0.7f, 0.9f, 0.5f, 0.6f, 0.3f, 0.1f)
        scaleYbar1.repeatCount = ValueAnimator.INFINITE
        val scaleYbar2 = ObjectAnimator.ofFloat(binding.musicBar2, "scaleY", 0.2f, 0.5f, 1.0f, 0.5f, 0.3f, 0.1f, 0.2f, 0.3f, 0.5f, 0.1f, 0.6f, 0.5f, 0.3f, 0.7f, 0.8f, 0.9f, 0.3f, 0.1f, 0.5f, 0.3f, 0.6f, 1.0f, 0.6f, 0.7f, 0.4f, 0.1f)
        scaleYbar2.repeatCount = ValueAnimator.INFINITE
        val scaleYbar3 = ObjectAnimator.ofFloat(binding.musicBar3, "scaleY", 0.6f, 0.5f, 1.0f, 0.6f, 0.5f, 1.0f, 0.6f, 0.5f, 1.0f, 0.5f, 0.6f, 0.7f, 0.2f, 0.3f, 0.1f, 0.5f, 0.4f, 0.6f, 0.7f, 0.1f, 0.4f, 0.3f, 0.1f, 0.4f, 0.3f, 0.7f)
        scaleYbar3.repeatCount = ValueAnimator.INFINITE
        val scaleYbar4 = ObjectAnimator.ofFloat(binding.musicBar4, "scaleY", 0.2f, 0.8f, 0.1f, 0.1f, 0.3f, 0.1f, 0.2f, 0.8f, 0.7f, 0.2f, 0.4f, 0.9f, 0.7f, 0.6f, 0.1f, 0.3f, 0.1f, 0.4f, 0.1f, 0.8f, 0.7f, 0.9f, 0.5f, 0.6f, 0.3f, 0.1f)
        scaleYbar4.repeatCount = ValueAnimator.INFINITE
        val scaleYbar5 = ObjectAnimator.ofFloat(binding.musicBar5, "scaleY", 0.2f, 0.5f, 1.0f, 0.5f, 0.3f, 0.1f, 0.2f, 0.3f, 0.5f, 0.1f, 0.6f, 0.5f, 0.3f, 0.7f, 0.8f, 0.9f, 0.3f, 0.1f, 0.5f, 0.3f, 0.6f, 1.0f, 0.6f, 0.7f, 0.4f, 0.1f)
        scaleYbar5.repeatCount = ValueAnimator.INFINITE
        return arrayOf(scaleYbar1, scaleYbar2, scaleYbar3, scaleYbar4, scaleYbar5)
    }*/

    /**
     * remember to make invalidate
     */
    private fun getBarByteArray(byteArray: ByteArray): Array<ObjectAnimator> {
        val x0: List<Float> = byteArray.asList().subList(0, 202).map {
            Log.v("animtinvalue","${ ( ((abs((it ).toFloat()) / 128)*10).toInt()).toFloat()/10}")

            ( ((abs((it ).toFloat()) / 128)*10).toInt()).toFloat()/10
        }
        val x1: List<Float> = byteArray.asList().subList(202, 404).map {

            ( ((abs((it ).toFloat()) / 128)*10).toInt()).toFloat()/10

        }
        val x2: List<Float> = byteArray.asList().subList(404, 606).map {

            ( ((abs((it ).toFloat()) / 128)*10).toInt()).toFloat()/10

        }
        val x3: List<Float> = byteArray.asList().subList(606, 808).map {

            ( ((abs((it ).toFloat()) / 128)*10).toInt()).toFloat()/10

        }
        val x4: List<Float> = byteArray.asList().subList(808, 1010).map {

            ( ((abs((it ).toFloat()) / 128)*10).toInt()).toFloat()/10

        }
        val scaleYbar1 = ObjectAnimator.ofFloat(binding.musicBar1, "scaleY", *x0.toFloatArray())
        scaleYbar1.repeatCount = ValueAnimator.INFINITE
        val scaleYbar2 = ObjectAnimator.ofFloat(binding.musicBar2, "scaleY", *x1.toFloatArray())
        scaleYbar2.repeatCount = ValueAnimator.INFINITE
        val scaleYbar3 = ObjectAnimator.ofFloat(binding.musicBar3, "scaleY", *x2.toFloatArray())
        scaleYbar3.repeatCount = ValueAnimator.INFINITE
        val scaleYbar4 = ObjectAnimator.ofFloat(binding.musicBar4, "scaleY", *x3.toFloatArray())
        scaleYbar4.repeatCount = ValueAnimator.INFINITE
        val scaleYbar5 = ObjectAnimator.ofFloat(binding.musicBar5, "scaleY", *x4.toFloatArray())
        scaleYbar5.repeatCount = ValueAnimator.INFINITE
        return arrayOf(scaleYbar1, scaleYbar2, scaleYbar3, scaleYbar4, scaleYbar5)
    }

    fun animateBars(byteArray: ByteArray) {
        isAnimating = true
        if (!::playingSet.isInitialized) {
            playingSet = AnimatorSet()
            getBarByteArray(byteArray).apply {
                playingSet.playTogether(this[0], this[1], this[2], this[3], this[4])
            }
            playingSet.duration = duration.toLong()
            playingSet.interpolator = LinearInterpolator()
            playingSet.start()
        } else {
            //if (playingSet.isPaused) {
                getBarByteArray(byteArray).apply {
                    playingSet.childAnimations.clear()
                    playingSet.playTogether(this[0], this[1], this[2], this[3], this[4])
                    playingSet.resume()
                }

           //}
        }
    }


    /* fun animateBars() {
        isAnimating = true
        if (!::playingSet.isInitialized) {
            playingSet = AnimatorSet()
            bars.asList().shuffled().apply {
                playingSet.playTogether(this[0], this[1], this[2], this[3], this[4])
            }
            playingSet.duration = duration.toLong()
            playingSet.interpolator = LinearInterpolator()
            playingSet.start()
        } else {
            if (playingSet.isPaused) {
                bars.asList().shuffled().apply {
                    playingSet.playTogether(this[0], this[1], this[2], this[3], this[4])
                    playingSet.resume()
                }

            }
        }
    }
*/
    fun stopBars() {
        if (::playingSet.isInitialized && playingSet.isRunning && playingSet.isStarted) {
            playingSet.pause()
        }
        if (!::stopSet.isInitialized) { // Animate stopping bars
            val scaleY1 = ObjectAnimator.ofFloat(binding.musicBar1, "scaleY", 0.1f)
            val scaleY2 = ObjectAnimator.ofFloat(binding.musicBar2, "scaleY", 0.1f)
            val scaleY3 = ObjectAnimator.ofFloat(binding.musicBar3, "scaleY", 0.1f)
            val scaleY4 = ObjectAnimator.ofFloat(binding.musicBar4, "scaleY", 0.1f)
            val scaleY5 = ObjectAnimator.ofFloat(binding.musicBar5, "scaleY", 0.1f)
            stopSet = AnimatorSet()
            stopSet.playTogether(scaleY3, scaleY2, scaleY1, scaleY4, scaleY5)
            stopSet.duration = 500
            stopSet.start()
        } else if (!stopSet.isStarted && isAnimating) {
            stopSet.start()
        }
        isAnimating = false
    }

}