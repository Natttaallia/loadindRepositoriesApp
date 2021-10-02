package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var progressAnim: Int = 0
    private var loadingBgColor: Int = Color.GRAY
    private var text: String = context.getString(R.string.button_name).toUpperCase()
    private var textLoading: String = context.getString(R.string.button_loading).toUpperCase()
    private var currentText: String = text
    private var textColor: Int = Color.WHITE
    private var bgColor: Int = Color.GREEN
    private var widthSize = 0
    private var heightSize = 0
    private val textRect = Rect()

    private val paintLoading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var valueAnimator = getValueAnimator()

    private fun getValueAnimator(): ValueAnimator {
        return ValueAnimator.ofInt(0, widthSize).apply{
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                progressAnim = animatedValue as Int
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    progressAnim = 0
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    progressAnim = 0
                }
            })
        }
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Loading -> {
                currentText = textLoading
                valueAnimator.start()
                this.isEnabled = false
            }
            else -> {
                currentText = text
                valueAnimator.cancel()
                this.isEnabled = true
            }
        }
        invalidate()
    }


    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            bgColor = getColor(R.styleable.LoadingButton_bg_color, Color.GREEN)
            loadingBgColor = getColor(R.styleable.LoadingButton_loading_bg_color, Color.GRAY)
            text = (getString(R.styleable.LoadingButton_text)
                ?: context.getString(R.string.button_name)).toUpperCase()
            textLoading = (getString(R.styleable.LoadingButton_text_loading)
                ?: context.getString(R.string.button_loading)).toUpperCase()
            textColor = getColor(R.styleable.LoadingButton_text_color, Color.WHITE)
        }
        paintLoading.color = loadingBgColor
        paintText.color = textColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(bgColor)
        paintText.getTextBounds(currentText, 0, currentText.length, textRect)
        if (progressAnim > 0) {
            canvas?.drawRect(0f, 0f, progressAnim.toFloat(), heightSize.toFloat(), paintLoading)
            val circleX = width / 2f + textRect.width() / 2f + 30
            val circleY = height / 2f - 30
            val angel = progressAnim * 360 / widthSize
            canvas?.drawArc(
                circleX, circleY, circleX + 60, circleY + 60, 0f,
                angel.toFloat(), true, paintCircle)
        }
        val centerX = widthSize.toFloat() / 2
        val centerY = heightSize.toFloat() / 2 + textRect.height() / 2f - textRect.bottom
        canvas?.drawText(currentText, centerX, centerY, paintText)
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
        //update with new width
        valueAnimator = getValueAnimator()
        setMeasuredDimension(w, h)
    }

    fun setState(state: ButtonState) {
        buttonState = state
    }

}