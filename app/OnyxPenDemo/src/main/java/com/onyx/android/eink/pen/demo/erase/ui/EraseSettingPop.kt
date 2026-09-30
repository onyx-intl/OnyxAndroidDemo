package com.onyx.android.eink.pen.demo.erase.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.LayoutEraseSettingPopBinding
import com.onyx.android.eink.pen.demo.databinding.LayoutPenSettingPopBrushItemBinding
import com.onyx.android.eink.pen.demo.erase.data.EraseType
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.eink.pen.demo.erase.util.EraseUnits
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper
import com.onyx.android.eink.pen.demo.ui.popup.BasePopup
import com.onyx.android.sdk.utils.ResManager
import com.onyx.android.sdk.utils.ViewUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EraseSettingPop(context: Context?) : BasePopup(context) {
    private lateinit var binding: LayoutEraseSettingPopBinding
    private lateinit var eraseWidthPercents: MutableList<Int>
    private var penBundle: PenBundle? = null
    private var onChanged: (() -> Unit)? = null
    private var currentEraseType = EraseTypes.ERASER_STROKE

    init {
        initPopupWindow()
    }

    fun setPenBundle(penBundle: PenBundle): EraseSettingPop {
        this.penBundle = penBundle
        return this
    }

    fun setOnChanged(onChanged: (() -> Unit)?): EraseSettingPop {
        this.onChanged = onChanged
        return this
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        onShow()
    }

    private fun onShow() {
        if (binding.eraseTypeList.getAdapter() == null) {
            initEraseTypeList()
        }
        currentEraseType = requirePenBundle().getCurrentEraseType()
        val percent = EraseUnits.widthToPercentage(
            requirePenBundle().getEraseWidth(EraseTypes.ERASER_MOVE), EraseTypes.ERASER_MOVE
        )
        updateEraseWidthUi(percent)
        updateEraseTypeUi(currentEraseType)
    }

    private fun initPopupWindow() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.layout_erase_setting_pop, null, false
        )
        contentView = binding.getRoot()
        width = ResManager.getDimens(R.dimen.pen_popup_size)
        height = WindowManager.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        val drawable = GradientDrawable()
        drawable.setColor(Color.WHITE)
        drawable.setStroke(2, Color.BLACK)
        setBackgroundDrawable(drawable)

        eraseWidthPercents = EraseUnits.getEraseWidthPercentRange(EraseTypes.ERASER_MOVE)
        initSeekBar()
        initListener()
        initTrackCheck()
    }

    private fun initTrackCheck() {
        binding.trackCheck.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (buttonView?.isPressed == true) {
                onTrackToggled(currentEraseType, isChecked)
            }
        })
    }

    private fun syncTrackCheck(eraseType: Int) {
        setCheckedSilently(requirePenBundle().isDisplayEraseTrack(eraseType))
        binding.trackCheck.text = buildTrackCheckLabel(eraseType)
    }

    private fun setCheckedSilently(checked: Boolean) {
        binding.trackCheck.setOnCheckedChangeListener(null)
        binding.trackCheck.isChecked = checked
        initTrackCheck()
    }

    private fun buildTrackCheckLabel(eraseType: Int): CharSequence? {
        @StringRes
        val labelRes = trackLabelRes(eraseType)
        if (EraseTypes.isMoveOrStrokeErase(eraseType) && !EraserTrackHelper.supportsMoveStrokeSfTrack()) {
            return (ResManager.getString(labelRes) + ResManager.getString(R.string.erase_track_app_layer_hint))
        }
        return ResManager.getString(labelRes)
    }

    @StringRes
    private fun trackLabelRes(eraseType: Int): Int {
        when (eraseType) {
            EraseTypes.ERASER_MOVE -> return R.string.erase_track_move
            EraseTypes.ERASER_AREA -> return R.string.erase_track_area
            else -> return R.string.erase_track_stroke
        }
    }

    private fun onTrackToggled(eraseType: Int, enabled: Boolean) {
        requirePenBundle().setDisplayEraseTrack(eraseType, enabled)
        notifyChanged()
    }

    private fun updateEraseTypeUi(eraseType: Int) {
        currentEraseType = eraseType
        updateWidthVisibility(eraseType)
        syncTrackCheck(eraseType)
    }

    private fun initEraseTypeList() {
        val selected: EraseType =
            EraseType.fromValue(requirePenBundle().getCurrentEraseType())
        val options = ArrayList(EraseType.entries)
        val adapter = EraseTypeAdapter(options, selected)
        binding.eraseTypeList.setLayoutManager(GridLayoutManager(context, adapter.options.size))
        binding.eraseTypeList.setAdapter(adapter)
    }

    private fun initSeekBar() {
        binding.widthSeekBar.max = eraseWidthPercents.size - 1
        binding.widthSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || eraseWidthPercents.isEmpty()) {
                    return
                }
                val percent = eraseWidthPercents.getOrNull(progress) ?: return
                updateEraseWidth(percent)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun initListener() {
        binding.minus.setOnClickListener(View.OnClickListener { v: View? ->
            updateEraseWidth(
                getClickEraseWidthPercent(false)
            )
        })
        binding.plus.setOnClickListener(View.OnClickListener { v: View? ->
            updateEraseWidth(
                getClickEraseWidthPercent(true)
            )
        })
    }

    private fun updateWidthVisibility(eraseType: Int) {
        ViewUtils.setViewVisibleOrGone(
            binding.widthContainer, eraseType == EraseTypes.ERASER_MOVE
        )
    }

    private fun updateEraseWidth(percent: Int) {
        val width = EraseUnits.widthFromPercentage(percent)
        requirePenBundle().setEraseWidth(EraseTypes.ERASER_MOVE, width)
        updateEraseWidthUi(percent)
        notifyChanged()
    }

    private fun updateEraseWidthUi(percent: Int) {
        val width = EraseUnits.widthFromPercentage(percent)
        binding.eraserWidth.text = EraseUnits.formatWidthMm(width, EraseTypes.ERASER_MOVE)
        updateSeekBarProgress(percent)
    }

    private fun updateSeekBarProgress(percent: Int) {
        if (eraseWidthPercents.isEmpty()) {
            return
        }
        var index = eraseWidthPercents.indexOf(percent)
        if (index < 0) {
            index = findNearestPercentIndex(percent)
        }
        binding.widthSeekBar.progress = index
    }

    private fun findNearestPercentIndex(percent: Int): Int {
        var nearestIndex = 0
        var minDiff = Int.MAX_VALUE
        for (i in eraseWidthPercents.indices) {
            val diff = abs(eraseWidthPercents[i] - percent)
            if (diff < minDiff) {
                minDiff = diff
                nearestIndex = i
            }
        }
        return nearestIndex
    }

    private fun getClickEraseWidthPercent(plus: Boolean): Int {
        val currentPercent = EraseUnits.widthToPercentage(
            requirePenBundle().getEraseWidth(EraseTypes.ERASER_MOVE), EraseTypes.ERASER_MOVE
        )
        val nextPercent = if (plus) currentPercent + EraseUnits.getEraseWidthIncrement()
        else currentPercent - EraseUnits.getEraseWidthIncrement()
        val min = EraseUnits.getMinEraseWidthPercent(EraseTypes.ERASER_MOVE)
        val max = EraseUnits.getMaxEraseWidthPercent(EraseTypes.ERASER_MOVE)
        return max(min, min(max, nextPercent))
    }

    private fun requirePenBundle(): PenBundle {
        return checkNotNull(penBundle) { "EraseSettingPop requires setPenBundle() before use" }
    }

    private fun notifyChanged() {
        onChanged?.invoke()
    }

    private inner class EraseTypeAdapter(
        val options: MutableList<EraseType>,
        private var selectedEraseType: EraseType?
    ) : RecyclerView.Adapter<EraseTypeAdapter.ViewHolder?>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_pen_setting_pop_brush_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val eraseType = options.get(position)
            holder.bindTo(eraseType, eraseType == selectedEraseType)
            holder.itemView.setOnClickListener(View.OnClickListener { v: View? ->
                onEraseTypeSelected(
                    eraseType
                )
            })
        }

        override fun getItemCount(): Int {
            return options.size
        }

        fun onEraseTypeSelected(eraseType: EraseType) {
            selectedEraseType = eraseType
            notifyDataSetChanged()
            val eraseTypeValue = eraseType.getValue()
            requirePenBundle().selectEraseType(eraseTypeValue)
            updateEraseTypeUi(eraseTypeValue)
            notifyChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val itemBinding: LayoutPenSettingPopBrushItemBinding =
                checkNotNull(DataBindingUtil.bind(itemView))

            fun bindTo(eraseType: EraseType, selected: Boolean) {
                itemBinding.title.setText(eraseType.getNameResId())
                itemBinding.icon.visibility = View.GONE
                itemBinding.radio.isChecked = selected
                itemBinding.executePendingBindings()
            }
        }
    }
}
