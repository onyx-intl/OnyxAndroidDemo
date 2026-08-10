package com.onyx.android.eink.pen.demo.ui

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.PenCommands
import com.onyx.android.eink.pen.demo.PenManager
import com.onyx.android.eink.pen.demo.R
import com.onyx.android.eink.pen.demo.databinding.ActivityPenDemoBinding
import com.onyx.android.eink.pen.demo.erase.EraseLifecycleCallbacks
import com.onyx.android.eink.pen.demo.erase.input.DrawEraseInputHandler
import com.onyx.android.eink.pen.demo.erase.input.DrawEraseInputHandler.ShapeCommitCallback
import com.onyx.android.eink.pen.demo.erase.ui.EraseSettingPop
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper
import com.onyx.android.eink.pen.demo.event.ActivityFocusChangedEvent
import com.onyx.android.eink.pen.demo.event.ApplyFastModeEvent
import com.onyx.android.eink.pen.demo.event.DemoFloatMenuStateChangeEvent
import com.onyx.android.eink.pen.demo.event.FloatButtonChangedEvent
import com.onyx.android.eink.pen.demo.event.FloatButtonMenuStateChangedEvent
import com.onyx.android.eink.pen.demo.event.NotificationPanelChangeEvent
import com.onyx.android.eink.pen.demo.event.PenEvent
import com.onyx.android.eink.pen.demo.event.PopupWindowChangeEvent
import com.onyx.android.eink.pen.demo.event.StatusBarChangeEvent
import com.onyx.android.eink.pen.demo.receiver.GlobalDeviceReceiver
import com.onyx.android.eink.pen.demo.ui.popup.PenSettingPop
import com.onyx.android.eink.pen.demo.ui.view.FloatingMenuDragHandler
import com.onyx.android.eink.pen.demo.util.ShapeUtils
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.utils.BroadcastHelper
import com.onyx.android.sdk.utils.EventBusUtils
import com.onyx.android.sdk.utils.SystemPropertiesUtil
import com.onyx.android.sdk.utils.ViewUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PenDemoActivity : AppCompatActivity(), EraseLifecycleCallbacks {
    private lateinit var binding: ActivityPenDemoBinding

    private val deviceReceiver = GlobalDeviceReceiver()
    private var surfaceAttachJob: Job? = null

    private var rawInputCallback: RawInputCallback? = null
    private var dragHandler: FloatingMenuDragHandler? = null

    private var statusBarShowing = false
    private var NotificationPanelShowing = false
    private var popupShowing = false
    private var floatButtonActivated = false
    private var demoFloatMenuActivated = false
    private var hasFocus = true

    private val brushButtonClickListener =
        View.OnClickListener { view -> onBrushButtonClickImpl(view) }
    private val eraseButtonClickListener =
        View.OnClickListener { view -> onEraseButtonClickImpl(view) }
    private val clearButtonClickListener =
        View.OnClickListener { view -> onClearButtonClickImpl(view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pen_demo)

        EpdController.enablePost(binding.root, 1)
        deviceReceiver.enable(this, true)
        setNeedReceiveFloatButtonTouchStatus(true)
        EventBusUtils.ensureRegister(this.penManager.getEventBus(), this)
        initView()
        initListener()
    }

    private fun setNeedReceiveFloatButtonTouchStatus(enable: Boolean) {
        val intent: Intent = Intent(ONYX_ACTION_REQUIRE_FLOAT_BUTTON_STATUS)
        intent.putExtra(ARGS_STATUS, enable)
        BroadcastHelper.sendBroadcast(this, intent)
    }

    private val floatMenuLayout: LinearLayout
        get() = binding.floatMenuContainer.root

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        EventBusUtils.safelyPostEvent(
            this.penBundle.getEventBus(), ActivityFocusChangedEvent(hasFocus)
        )
    }

    override fun onPause() {
        runOnPenThread { it.releaseRawSession() }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        hasFocus = hasWindowFocus()
        requestAttachNoteView()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyImpl()
    }

    private fun destroyImpl() {
        surfaceAttachJob?.cancel()
        this.penManager.destroy()
        this.penBundle.resetToolToBrushOnSessionEnd()
        deviceReceiver.enable(this, false)
        setNeedReceiveFloatButtonTouchStatus(false)
        EventBusUtils.ensureUnregister(this.penManager.getEventBus(), this)
    }

    private fun initView() {
        initSurfaceView()
        ViewUtils.setViewVisibleOrGone(binding.penUpContainer, !SystemPropertiesUtil.isTablet())
    }

    private fun initSurfaceView() {
        val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                scheduleRenderToSurfaceView()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                scheduleRenderToSurfaceView()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                runOnPenThread { it.releaseRawSession() }
            }
        }
        val surfaceHolder = this.surfaceView.holder
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT)
        surfaceHolder.addCallback(surfaceCallback)
    }

    private fun runOnPenThread(action: (PenManager) -> Unit) {
        this.penManager.launchPen {
            action(this)
        }
    }

    private fun requestAttachNoteView() {
        val surfaceView = this.surfaceView
        if (surfaceView.width > 0 && surfaceView.height > 0) {
            scheduleRenderToSurfaceView()
        }
    }

    private fun applyCurrentPenStateToScreen() {
        hasFocus = hasWindowFocus()
        runOnPenThread { pm ->
            pm.applyCurrentPenState()
            pm.renderToScreen()
        }
    }

    private fun initListener() {
        this.surfaceView.setOnTouchListener { v, event -> true }
        binding.brushButton.setOnClickListener(brushButtonClickListener)
        binding.eraseButton.setOnClickListener(eraseButtonClickListener)
        binding.clearButton.setOnClickListener(clearButtonClickListener)
        binding.brushCheck.setOnCheckedChangeListener { _, isChecked ->
            if (binding.floatMenuContainer.floatBrushCheck.isChecked != isChecked) {
                binding.floatMenuContainer.floatBrushCheck.isChecked = isChecked
            } else {
                onBrushCheckImpl(isChecked)
            }
        }
        binding.eraseCheck.setOnCheckedChangeListener { _, isChecked ->
            if (binding.floatMenuContainer.floatEraseCheck.isChecked != isChecked) {
                binding.floatMenuContainer.floatEraseCheck.isChecked = isChecked
            } else {
                onEraseCheckImpl(isChecked)
            }
        }
        binding.penUpCheck.setOnCheckedChangeListener { _, isChecked ->
            if (binding.floatMenuContainer.floatPenUpCheck.isChecked != isChecked) {
                binding.floatMenuContainer.floatPenUpCheck.isChecked = isChecked
            } else {
                onPenUpCheckImpl(isChecked)
            }
        }
        initFloatMenuListener()
    }

    private fun initFloatMenuListener() {
        binding.floatMenuContainer.floatBrushButton.setOnClickListener(brushButtonClickListener)
        binding.floatMenuContainer.floatEraseButton.setOnClickListener(eraseButtonClickListener)
        binding.floatMenuContainer.floatClearButton.setOnClickListener(clearButtonClickListener)
        binding.floatMenuContainer.floatBrushCheck.setOnCheckedChangeListener { _, isChecked ->
            if (binding.brushCheck.isChecked != isChecked) {
                binding.brushCheck.isChecked = isChecked
            } else {
                onBrushCheckImpl(isChecked)
            }
        }
        binding.floatMenuContainer.floatEraseCheck.setOnCheckedChangeListener { _, isChecked ->
            if (binding.eraseCheck.isChecked != isChecked) {
                binding.eraseCheck.isChecked = isChecked
            } else {
                onEraseCheckImpl(isChecked)
            }
        }
        binding.floatMenuContainer.floatPenUpCheck.setOnCheckedChangeListener { _, isChecked ->
            if (binding.penUpCheck.isChecked != isChecked) {
                binding.penUpCheck.isChecked = isChecked
            } else {
                onPenUpCheckImpl(isChecked)
            }
        }
    }

    private fun onPenUpCheckImpl(isChecked: Boolean) {
        this.penBundle.setEnablePenUpRefresh(isChecked)
        refreshScreen()
    }

    private fun onEraseCheckImpl(isChecked: Boolean) {
        this.penBundle.setErasing(isChecked)
        if (isChecked) {
            this.penBundle.setCurrentEraseType(this.penBundle.getLastEraseType())
        }
        applyToolSwitch()
        if (isChecked) {
            binding.brushCheck.isChecked = false
            binding.floatMenuContainer.floatBrushCheck.isChecked = false
        }
    }

    private fun onBrushCheckImpl(isChecked: Boolean) {
        this.penBundle.setErasing(!isChecked)
        applyToolSwitch()
        if (isChecked) {
            binding.eraseCheck.isChecked = false
            binding.floatMenuContainer.floatEraseCheck.isChecked = false
        }
    }

    private fun onBrushButtonClickImpl(view: View) {
        binding.brushCheck.isChecked = true
        binding.floatMenuContainer.floatBrushCheck.isChecked = true
        val penSettingPop = PenSettingPop(view.context)
        showPenSettingPop(view, penSettingPop)
    }

    private fun onEraseButtonClickImpl(view: View) {
        binding.eraseCheck.isChecked = true
        binding.floatMenuContainer.floatEraseCheck.isChecked = true
        val eraseSettingPop = EraseSettingPop(view.context).setPenBundle(this.penBundle)
            .setOnChanged { this.onEraseSettingChanged() }
        showEraseSettingPop(view, eraseSettingPop)
    }

    private fun onClearButtonClickImpl(view: View?) {
        PenCommands.clearNote()
    }

    private fun onEraseSettingChanged() {
        if (this.penBundle.isEraseTool()) {
            applyToolSwitch()
        } else {
            refreshScreen()
        }
    }

    private fun applyToolSwitch() {
        penManager.launchPen(onSuccess = { resumeAfterToolSwitch() }) {
            applyToolSwitchWithRefresh()
        }
    }

    private fun resumeAfterToolSwitch() {
        val resumeRender = !this.penBundle.isEraseTool() || EraserTrackHelper.useSfTrack(
            this.penBundle, this.penBundle.getCurrentEraseType()
        )
        resumeRawDrawingAllowEraseRender(
            resumeRender, true, PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS
        )
    }

    private fun showPenSettingPop(view: View, penSettingPop: PenSettingPop) {
        if (view.id == R.id.brush_button) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = location[0] + view.width
            penSettingPop.showAsDropDown(view, x, 0, Gravity.NO_GRAVITY)
        } else if (view.id == R.id.float_brush_button) {
            penSettingPop.showAsDropDown(view, 0, 0, Gravity.NO_GRAVITY)
        }
    }

    private fun showEraseSettingPop(view: View, eraseSettingPop: EraseSettingPop) {
        if (view.id == R.id.erase_button) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = location[0] + view.width
            eraseSettingPop.showAsDropDown(view, x, 0, Gravity.NO_GRAVITY)
        } else if (view.id == R.id.float_erase_button) {
            eraseSettingPop.showAsDropDown(view, 0, 0, Gravity.NO_GRAVITY)
        }
    }

    private fun scheduleRenderToSurfaceView() {
        surfaceAttachJob?.cancel()
        surfaceAttachJob = lifecycleScope.launch {
            delay(SURFACE_CHANGED_THROTTLE_MS)
            renderToSurfaceView()
        }
    }

    private fun renderToSurfaceView() {
        Log.e(TAG, "renderToSurfaceView")
        lifecycleScope.launch {
            hasFocus = hasWindowFocus()
            PenCommands.attachNoteView(
                surfaceView, floatMenuLayout, getRawInputCallback()
            ).onSuccess { limitRect ->
                dragHandler = FloatingMenuDragHandler(floatMenuLayout).setLimitRect(limitRect)
                floatMenuLayout.setOnTouchListener(dragHandler)
            }
        }
    }

    private fun addShape(touchPointList: TouchPointList?) {
        val currentShapeType = this.penBundle.getCurrentShapeType()
        val shape = ShapeUtils.createShape(this.penBundle, currentShapeType, touchPointList)
        PenCommands.addShape(shape)
    }

    private fun pauseRawDrawingRender() {
        PenCommands.pauseRawDrawingRender()
    }

    private fun pauseRawDrawing() {
        PenCommands.pauseRawDrawing()
    }

    private fun canResumePenOverlay(): Boolean {
        return hasFocus && !statusBarShowing && !NotificationPanelShowing && !popupShowing && !floatButtonActivated && !demoFloatMenuActivated
    }

    private fun resumeRawDrawing(
        resumeRender: Boolean,
        resumeInput: Boolean,
        delayResumePenTime: Int,
    ) { // SF erase track needs raw render while erase tool stays selected.
        val allowEraseRender = this.penBundle.isEraseTool() && EraserTrackHelper.useSfTrack(
            this.penBundle, this.penBundle.getCurrentEraseType()
        )
        val render =
            resumeRender && (!this.penBundle.isErasing() || allowEraseRender) && canResumePenOverlay()
        val input = resumeInput && canResumePenOverlay()
        if (!render && !input) {
            return
        }
        resumeRawDrawingImpl(render, input, delayResumePenTime)
    }

    private fun resumeRawDrawingImpl(
        resumeRender: Boolean,
        resumeInput: Boolean,
        delayResumePenTime: Int,
    ) {
        PenCommands.resumeRawDrawing(
            resumeRender, resumeInput, delayResumePenTime
        )
    }

    override fun resumePenAfterErase() {
        applyCurrentPenStateToScreen()
    }

    private fun resumeRawDrawingAllowEraseRender(
        resumeRender: Boolean,
        resumeInput: Boolean,
        delayResumePenTime: Int,
    ) {
        val render = resumeRender && canResumePenOverlay()
        val input = resumeInput && canResumePenOverlay()
        if (!render && !input) {
            return
        }
        resumeRawDrawingImpl(render, input, delayResumePenTime)
    }

    override fun refreshScreen() {
        PenCommands.refreshScreen()
    }

    private fun applyApplicationFastMode(enable: Boolean) {
        if (enable) {
            EpdController.applyTransientUpdate(UpdateMode.ANIMATION_X)
        } else {
            EpdController.clearTransientUpdate(true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationPanelChangeEvent(event: NotificationPanelChangeEvent) {
        NotificationPanelShowing = event.show
        refreshScreen()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStatusBarChangeEvent(event: StatusBarChangeEvent) {
        statusBarShowing = event.show
        refreshScreen()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onActivityFocusChangedEvent(event: ActivityFocusChangedEvent) {
        hasFocus = event.hasFocus
        if (hasFocus && this.penManager.needsRawSessionRestart()) {
            requestAttachNoteView()
            return
        }
        refreshScreen()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFloatButtonChangedEvent(event: FloatButtonChangedEvent) {
        floatButtonActivated = event.active
        refreshScreen()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFloatButtonMenuStateChangedEvent(event: FloatButtonMenuStateChangedEvent) {
        floatButtonActivated = event.active
        refreshScreen()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDemoFloatMenuStateChangeEvent(event: DemoFloatMenuStateChangeEvent) {
        demoFloatMenuActivated = event.active
        refreshScreen()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApplyFastModeEvent(event: ApplyFastModeEvent) {
        applyApplicationFastMode(event.enable)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPenEvent(event: PenEvent) {
        if (!event.isResumeDrawingRender() && !event.isResumeRawInputReader()) {
            pauseRawDrawingRender()
            return
        }
        resumeRawDrawing(
            event.isResumeDrawingRender(),
            event.isResumeRawInputReader(),
            event.getDelayResumePenTimeMs()
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPopupWindowChangeEvent(event: PopupWindowChangeEvent) {
        popupShowing = event.show
        if (event.show) {
            pauseRawDrawing()
        } else {
            resumeRawDrawing(true, true, PenEvent.POPUP_RESUME_PEN_TIME_MS)
        }
    }

    private val surfaceView: SurfaceView
        get() = binding.surfaceView

    private val penBundle: PenBundle
        get() = PenBundle.getInstance()

    val penManager: PenManager
        get() = this.penBundle.getPenManager()

    private fun getRawInputCallback(): RawInputCallback {
        val existing = rawInputCallback
        if (existing != null) {
            return existing
        }
        return DrawEraseInputHandler(
            this.penBundle, this.penManager, this, object : ShapeCommitCallback {
                override fun onCommitShape(touchPointList: TouchPointList?) {
                    if (floatButtonActivated || demoFloatMenuActivated) {
                        Log.d(TAG, "FloatButton or demoFloatMenu activated, return")
                        return
                    }
                    Log.d(TAG, "onRawDrawingTouchPointListReceived")
                    addShape(touchPointList)
                }
            }, null
        ).also { rawInputCallback = it }
    }

    companion object {
        private val TAG: String = PenDemoActivity::class.java.simpleName
        private const val ONYX_ACTION_REQUIRE_FLOAT_BUTTON_STATUS =
            "onyx.action.REQUIRE_FLOAT_BUTTON_STATUS"
        private const val ARGS_STATUS = "args_status"
        private const val SURFACE_CHANGED_THROTTLE_MS = 300L
    }
}
