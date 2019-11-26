package com.example.mediaplayer.customViews

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import com.example.mediaplayer.R

class CustomDropDownMenu :ImageButton {
    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.CustomDropDownMenu, 0, 0)
            val menu = typedArray.getResourceId(R.styleable.CustomDropDownMenu_dropDownMenu, 0)
            val drawable=typedArray.getResourceId(R.styleable.CustomDropDownMenu_drawable,0)
            typedArray.recycle()
        this.setImageResource(drawable)
            this.setOnClickListener {
                //creating a popup menu
                val popup = PopupMenu(context, this)
                //inflating menu from xml resource
                popup.inflate(menu)
                // popup.setOnMenuItemClickListener {}
                //displaying the popup
                popup.show()
            }
        }
    }
}