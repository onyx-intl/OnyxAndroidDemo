package com.onyx.android.eink.pen.demo.ui.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenCommands
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.data.ShapeTexture
import com.onyx.android.eink.pen.demo.data.ShapeType
import com.onyx.android.eink.pen.demo.data.StrokeColor
import com.onyx.android.eink.pen.demo.databinding.LayoutPenSettingPopBinding
import com.onyx.android.eink.pen.demo.databinding.LayoutPenSettingPopBrushItemBinding
import com.onyx.android.eink.pen.demo.util.PenInfoUtils
import com.onyx.android.sdk.utils.CollectionUtils
import com.onyx.android.sdk.utils.ResManager
import com.onyx.android.sdk.utils.ViewUtils
import java.util.Arrays
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class PenSettingPop(context: Context?) : BasePopup(context) {
    private lateinit var binding: LayoutPenSettingPopBinding
    private lateinit var strokeWidthValues: MutableList<Float?>

    init {
        initPopupWindow()
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        onShow()
    }

    private fun onShow() {
        updateStrokeWidth(this.currentStrokeWidth, true)
    }

    private fun initPopupWindow() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.layout_pen_setting_pop, null, false
        )
        contentView = binding.root

        width = ResManager.getDimens(R.dimen.pen_popup_size)
        height = WindowManager.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        val drawable = GradientDrawable()
        drawable.setColor(Color.WHITE)
        drawable.setStroke(2, Color.BLACK)
        setBackgroundDrawable(drawable)

        initBrushList()
        initColorList()
        initTextureList()
        initListener()
        initSeekBar()
    }

    private fun initListener() {
        binding.minus.setOnClickListener {
            val currentStrokeWidth = this.currentStrokeWidth
            val width = getClickStrokeWidth(false, currentStrokeWidth)
            updateStrokeWidth(width)
        }
        binding.plus.setOnClickListener {
            val currentStrokeWidth = this.currentStrokeWidth
            val width = getClickStrokeWidth(true, currentStrokeWidth)
            updateStrokeWidth(width)
        }
    }

    private fun initSeekBar() {
        initStrokeWidthValues()
        binding.widthSeekBar.max = strokeWidthValues.size - 1
        binding.widthSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val width = strokeWidthValues[progress]
                        ?: throw NullPointerException("strokeWidthValues[$progress]")
                    updateStrokeWidth(width)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun initBrushList() {
        val brushSettingAdapter = BrushSettingAdapter(
            mutableListOf(*ShapeType.entries.toTypedArray()),
            ShapeType.find(this.currentShapeType)
        )
        binding.brushList.setLayoutManager(
            GridLayoutManager(
                context, brushSettingAdapter.options.size
            )
        )
        binding.brushList.setAdapter(brushSettingAdapter)
    }

    private fun initColorList() {
        val colorSettingAdapter = ColorSettingAdapter(
            mutableListOf(*StrokeColor.entries.toTypedArray()),
            StrokeColor.find(this.currentStrokeColor)
        )
        binding.colorList.setLayoutManager(
            GridLayoutManager(
                context, colorSettingAdapter.options.size
            )
        )
        binding.colorList.setAdapter(colorSettingAdapter)
    }

    private fun initTextureList() {
        val textures: MutableList<ShapeTexture> = ShapeTexture.getShapeTextures(
            this.currentShapeType
        )
        val showTexture = CollectionUtils.isNonBlank(textures)
        ViewUtils.setViewVisibleOrGone(binding.textureContainer, showTexture)
        if (showTexture) {
            val adapter = TextureSettingAdapter(
                textures, ShapeTexture.find(
                    this.currentTexture
                )
            )
            binding.textureList.setLayoutManager(GridLayoutManager(context, adapter.options.size))
            binding.textureList.setAdapter(adapter)
        }
    }

    private fun initStrokeWidthValues() {
        strokeWidthValues = PenInfoUtils.getPenWidthRange(this.currentShapeType)
    }

    private fun updateStrokeWidth(width: Float) {
        updateStrokeWidth(width, false)
    }

    private fun updateStrokeWidth(lineWidth: Float, justInitView: Boolean) {
        val lineWidthValue = if (floor(lineWidth.toDouble()) == lineWidth.toDouble()) String.format(
            Locale.getDefault(), "%.0f", lineWidth
        ) else lineWidth.toString()
        binding.width.text = lineWidthValue
        binding.widthSeekBar.progress = strokeWidthValues.indexOf(lineWidth)
        if (!justInitView) {
            updateStrokeWidthImpl(lineWidth)
        }
    }

    private fun updateStrokeWidthImpl(lineWidth: Float) {
        PenCommands.changeStrokeWidth(this.currentShapeType, lineWidth)
    }

    private fun getClickStrokeWidth(plusClick: Boolean, currentStrokeWidth: Float): Float {
        val currentStrokeStyle = this.currentShapeType
        val gap = PenInfoUtils.getStrokeWidthGap(currentStrokeStyle, plusClick, currentStrokeWidth)
        if (plusClick) {
            return min(currentStrokeWidth + gap, PenInfoUtils.getMaxStrokeWidth(currentStrokeStyle))
        } else {
            return max(currentStrokeWidth - gap, PenInfoUtils.getMinStrokeWidth(currentStrokeStyle))
        }
    }

    private val currentStrokeWidth: Float
        get() = this.penBundle.getCurrentStrokeWidth()

    private val currentShapeType: Int
        get() = this.penBundle.getCurrentShapeType()

    private val currentStrokeColor: Int
        get() = this.penBundle.getCurrentStrokeColor()

    private val currentTexture: Int
        get() = this.penBundle.getCurrentTexture()

    private val penBundle: PenBundle
        get() = PenBundle.getInstance()

    inner class BrushSettingAdapter(
        val options: MutableList<ShapeType>,
        private var selectedShapeType: ShapeType
    ) : RecyclerView.Adapter<BrushSettingAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_pen_setting_pop_brush_item, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val shapeType = options[position]
            holder.bindTo(shapeType, shapeType == selectedShapeType)
            holder.itemView.setOnClickListener {
                onBrushSettingImpl(shapeType)
            }
        }

        override fun getItemCount(): Int {
            return options.size
        }

        private fun onBrushSettingImpl(shapeType: ShapeType) {
            selectedShapeType = shapeType
            notifyDataSetChanged()
            PenCommands.changeStrokeStyle(
                selectedShapeType.getValue(), penBundle.getCurrentTexture()
            ) {
                initSeekBar()
                initTextureList()
                updateStrokeWidth(penBundle.getPenLineWidth(shapeType.getValue()))
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val binding: LayoutPenSettingPopBrushItemBinding =
                requireNotNull(DataBindingUtil.bind(itemView))

            fun bindTo(shapeType: ShapeType, selected: Boolean) {
                binding.title.setText(shapeType.getTextResId())
                binding.icon.setImageResource(shapeType.getIconResId())
                binding.radio.isChecked = selected
                binding.executePendingBindings()
            }
        }
    }

    inner class ColorSettingAdapter(
        val options: MutableList<StrokeColor>,
        private var selectedStrokeColor: StrokeColor?
    ) : RecyclerView.Adapter<ColorSettingAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_pen_setting_pop_brush_item, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val strokeColor = options[position]
            holder.bindTo(strokeColor, strokeColor == selectedStrokeColor)
            holder.itemView.setOnClickListener {
                selectedStrokeColor = strokeColor
                PenCommands.changeStrokeColor(strokeColor.getValue())
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return options.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val binding: LayoutPenSettingPopBrushItemBinding =
                requireNotNull(DataBindingUtil.bind(itemView))

            fun bindTo(strokeStyle: StrokeColor, selected: Boolean) {
                binding.title.setText(strokeStyle.getTextResId())
                binding.radio.isChecked = selected
                binding.executePendingBindings()
            }
        }
    }

    inner class TextureSettingAdapter(
        val options: MutableList<ShapeTexture>,
        private var selectedShapeTexture: ShapeTexture
    ) : RecyclerView.Adapter<TextureSettingAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_pen_setting_pop_brush_item, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val texture = options[position]
            holder.bindTo(texture, texture == selectedShapeTexture)
            holder.itemView.setOnClickListener {
                selectedShapeTexture = texture
                PenCommands.changeStrokeStyle(
                    penBundle.getCurrentShapeType(), selectedShapeTexture.getTexture()
                )
            }
        }

        override fun getItemCount(): Int {
            return options.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val binding: LayoutPenSettingPopBrushItemBinding =
                requireNotNull(DataBindingUtil.bind(itemView))

            fun bindTo(texture: ShapeTexture, selected: Boolean) {
                binding.title.setText(texture.getTextureTextResId())
                binding.radio.isChecked = selected
                binding.executePendingBindings()
            }
        }
    }
}
