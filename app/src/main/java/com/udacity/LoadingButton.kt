package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import kotlin.math.min
import kotlin.properties.Delegates


private const val RADIUS_OFFSET = 30
private const val TEXT_PADDING_LEFT = 20
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator()

    private val sweepAngleAnimator = ValueAnimator()

    private var sweepAnim = ValueAnimator()

    private var widthAnim = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new)
        {
            ButtonState.Loading-> {
                setupAnimation()
                invalidate()
            }
            ButtonState.Completed-> {
                cancelAnimation()
                invalidate()

            }

            ButtonState.Clicked-> {
                invalidate()
            }
        }


    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var radius = 0.0f
    private var circleColor = 0
    private var animatedRecWidth = 0
    private var animatedSweepAngle = 0F

    private var leftArcRec = 0F
    private var topArcRec  = 0F
    private var rightArcRec  =  0F
    private var bottomArcRec  = 0F
    private var arcRectF: RectF? = null

    init {
        isClickable = true
        circleColor = resources.getColor(R.color.circleColor)
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    context.getString(when (buttonState) {
                        ButtonState.Loading ->  R.string.loading
                        ButtonState.Completed -> R.string.completed
                        else-> R.string.download
                    })
                )
                info.addAction(customClick)
            }
        })


    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.BLACK
        canvas.drawRect(0.0f,0.0f,animatedRecWidth.toFloat(),heightSize.toFloat(), paint)

        paint.color = context.getColor(R.color.colorPrimary)
        canvas.drawRect(animatedRecWidth.toFloat(),0.0f,(widthSize).toFloat(),heightSize.toFloat(), paint)

        paint.color = Color.WHITE
        var text = resources.getString(R.string.download)
        if (buttonState == ButtonState.Loading) {
            text = resources.getString(R.string.button_loading)
        }
        canvas.drawText(text , (widthSize / 2).toFloat(), (heightSize/2).toFloat(), paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = circleColor
            arcRectF?.let { canvas.drawArc(it, 0F, animatedSweepAngle, true, paint) }

        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

    }

    private fun setupAnimation() {
        widthAnim = valueAnimator.apply {
            setIntValues(0, widthSize)
            setDuration(4000)

        }
        widthAnim.addUpdateListener {
            val value = it.animatedValue as Int
            animatedRecWidth = value
            invalidate()

        }
        widthAnim.start()

        sweepAnim = sweepAngleAnimator.apply {
            setFloatValues(0F,360F)
            setDuration(4000)
        }

        sweepAnim.addUpdateListener {
            val value = it.animatedValue as Float
            animatedSweepAngle = value
            invalidate()
        }

        sweepAnim.start()
    }

    private fun cancelAnimation() {
        sweepAnim.removeAllUpdateListeners()
        sweepAnim.cancel()
        widthAnim.removeAllUpdateListeners()
        widthAnim.cancel()
        animatedRecWidth = 0
        animatedSweepAngle = 0F
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = ((min(w, h)/ 2.0) - RADIUS_OFFSET).toFloat()
        val text  = resources.getString(R.string.button_loading)
        val textWidth = paint.measureText(text)

        leftArcRec = ((widthSize + textWidth)/ 2) +  radius
        //leftArcRec = ((widthSize/2) + textWidth) - radius
        topArcRec  = (heightSize / 2) - radius
        rightArcRec  =  ((widthSize + textWidth)/ 2) + radius + radius
        bottomArcRec  = (heightSize / 2) + radius

        arcRectF= RectF(leftArcRec , topArcRec , rightArcRec , bottomArcRec )
    }

    override fun performClick(): Boolean {

        super.performClick()
        return true
    }

    fun changeButtonState(state:ButtonState)
    {
        buttonState = state
    }

}