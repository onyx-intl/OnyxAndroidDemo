package com.onyx.android.eink.pen.demo.erase.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.databinding.LayoutEraseSettingPopBinding;
import com.onyx.android.eink.pen.demo.databinding.LayoutPenSettingPopBrushItemBinding;
import com.onyx.android.eink.pen.demo.erase.data.EraseType;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.util.EraseUnits;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.eink.pen.demo.ui.popup.BasePopup;
import com.onyx.android.sdk.utils.ResManager;
import com.onyx.android.sdk.utils.ViewUtils;

import java.util.Arrays;
import java.util.List;

public class EraseSettingPop extends BasePopup {

    private LayoutEraseSettingPopBinding binding;
    private List<Integer> eraseWidthPercents;
    private PenBundle penBundle;
    private Runnable onChanged;
    private int currentEraseType = EraseTypes.ERASER_STROKE;

    public EraseSettingPop(Context context) {
        super(context);
        initPopupWindow();
    }

    public EraseSettingPop setPenBundle(PenBundle penBundle) {
        this.penBundle = penBundle;
        return this;
    }

    public EraseSettingPop setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        onShow();
    }

    private void onShow() {
        if (binding.eraseTypeList.getAdapter() == null) {
            initEraseTypeList();
        }
        currentEraseType = requirePenBundle().getCurrentEraseType();
        int percent = EraseUnits.widthToPercentage(
                requirePenBundle().getEraseWidth(EraseTypes.ERASER_MOVE), EraseTypes.ERASER_MOVE);
        updateEraseWidthUi(percent);
        updateEraseTypeUi(currentEraseType);
    }

    private void initPopupWindow() {
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.layout_erase_setting_pop, null, false);
        setContentView(binding.getRoot());
        setWidth(ResManager.getDimens(R.dimen.pen_popup_size));
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setStroke(2, Color.BLACK);
        setBackgroundDrawable(drawable);

        eraseWidthPercents = EraseUnits.getEraseWidthPercentRange(EraseTypes.ERASER_MOVE);
        initSeekBar();
        initListener();
        initTrackCheck();
    }

    private void initTrackCheck() {
        binding.trackCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            onTrackToggled(currentEraseType, isChecked);
        });
    }

    private void syncTrackCheck(int eraseType) {
        setCheckedSilently(requirePenBundle().isDisplayEraseTrack(eraseType));
        binding.trackCheck.setText(buildTrackCheckLabel(eraseType));
    }

    private void setCheckedSilently(boolean checked) {
        binding.trackCheck.setOnCheckedChangeListener(null);
        binding.trackCheck.setChecked(checked);
        initTrackCheck();
    }

    private CharSequence buildTrackCheckLabel(int eraseType) {
        @StringRes int labelRes = trackLabelRes(eraseType);
        if (EraseTypes.isMoveOrStrokeErase(eraseType)
                && !EraserTrackHelper.supportsMoveStrokeSfTrack()) {
            return ResManager.getString(labelRes)
                    + ResManager.getString(R.string.erase_track_app_layer_hint);
        }
        return ResManager.getString(labelRes);
    }

    @StringRes
    private int trackLabelRes(int eraseType) {
        switch (eraseType) {
            case EraseTypes.ERASER_MOVE:
                return R.string.erase_track_move;
            case EraseTypes.ERASER_AREA:
                return R.string.erase_track_area;
            default:
                return R.string.erase_track_stroke;
        }
    }

    private void onTrackToggled(int eraseType, boolean enabled) {
        requirePenBundle().setDisplayEraseTrack(eraseType, enabled);
        notifyChanged();
    }

    private void updateEraseTypeUi(int eraseType) {
        currentEraseType = eraseType;
        updateWidthVisibility(eraseType);
        syncTrackCheck(eraseType);
    }

    private void initEraseTypeList() {
        EraseType selected = EraseType.fromValue(requirePenBundle().getCurrentEraseType());
        EraseTypeAdapter adapter = new EraseTypeAdapter(Arrays.asList(EraseType.values()), selected);
        binding.eraseTypeList.setLayoutManager(new GridLayoutManager(context, adapter.options.size()));
        binding.eraseTypeList.setAdapter(adapter);
    }

    private void initSeekBar() {
        binding.widthSeekBar.setMax(eraseWidthPercents.size() - 1);
        binding.widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || eraseWidthPercents == null || eraseWidthPercents.isEmpty()) {
                    return;
                }
                updateEraseWidth(eraseWidthPercents.get(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initListener() {
        binding.minus.setOnClickListener(v ->
                updateEraseWidth(getClickEraseWidthPercent(false)));
        binding.plus.setOnClickListener(v ->
                updateEraseWidth(getClickEraseWidthPercent(true)));
    }

    private void updateWidthVisibility(int eraseType) {
        ViewUtils.setViewVisibleOrGone(binding.widthContainer, eraseType == EraseTypes.ERASER_MOVE);
    }

    private void updateEraseWidth(int percent) {
        float width = EraseUnits.widthFromPercentage(percent);
        requirePenBundle().setEraseWidth(EraseTypes.ERASER_MOVE, width);
        updateEraseWidthUi(percent);
        notifyChanged();
    }

    private void updateEraseWidthUi(int percent) {
        float width = EraseUnits.widthFromPercentage(percent);
        binding.eraserWidth.setText(EraseUnits.formatWidthMm(width, EraseTypes.ERASER_MOVE));
        updateSeekBarProgress(percent);
    }

    private void updateSeekBarProgress(int percent) {
        if (eraseWidthPercents == null || eraseWidthPercents.isEmpty()) {
            return;
        }
        int index = eraseWidthPercents.indexOf(percent);
        if (index < 0) {
            index = findNearestPercentIndex(percent);
        }
        binding.widthSeekBar.setProgress(index);
    }

    private int findNearestPercentIndex(int percent) {
        int nearestIndex = 0;
        int minDiff = Integer.MAX_VALUE;
        for (int i = 0; i < eraseWidthPercents.size(); i++) {
            int diff = Math.abs(eraseWidthPercents.get(i) - percent);
            if (diff < minDiff) {
                minDiff = diff;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    private int getClickEraseWidthPercent(boolean plus) {
        int currentPercent = EraseUnits.widthToPercentage(
                requirePenBundle().getEraseWidth(EraseTypes.ERASER_MOVE), EraseTypes.ERASER_MOVE);
        int nextPercent = plus
                ? currentPercent + EraseUnits.getEraseWidthIncrement()
                : currentPercent - EraseUnits.getEraseWidthIncrement();
        int min = EraseUnits.getMinEraseWidthPercent(EraseTypes.ERASER_MOVE);
        int max = EraseUnits.getMaxEraseWidthPercent(EraseTypes.ERASER_MOVE);
        return Math.max(min, Math.min(max, nextPercent));
    }

    @NonNull
    private PenBundle requirePenBundle() {
        if (penBundle == null) {
            throw new IllegalStateException("EraseSettingPop requires setPenBundle() before use");
        }
        return penBundle;
    }

    private void notifyChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }

    private class EraseTypeAdapter extends RecyclerView.Adapter<EraseTypeAdapter.ViewHolder> {
        private final List<EraseType> options;
        private EraseType selectedEraseType;

        EraseTypeAdapter(List<EraseType> options, EraseType selectedEraseType) {
            this.options = options;
            this.selectedEraseType = selectedEraseType;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_pen_setting_pop_brush_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EraseType eraseType = options.get(position);
            holder.bindTo(eraseType, eraseType == selectedEraseType);
            holder.itemView.setOnClickListener(v -> onEraseTypeSelected(eraseType));
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        private void onEraseTypeSelected(EraseType eraseType) {
            selectedEraseType = eraseType;
            notifyDataSetChanged();
            int eraseTypeValue = eraseType.getValue();
            requirePenBundle().selectEraseType(eraseTypeValue);
            updateEraseTypeUi(eraseTypeValue);
            notifyChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final LayoutPenSettingPopBrushItemBinding itemBinding;

            ViewHolder(View itemView) {
                super(itemView);
                itemBinding = DataBindingUtil.bind(itemView);
            }

            void bindTo(EraseType eraseType, boolean selected) {
                itemBinding.title.setText(eraseType.getNameResId());
                itemBinding.icon.setVisibility(View.GONE);
                itemBinding.radio.setChecked(selected);
                itemBinding.executePendingBindings();
            }
        }
    }
}
