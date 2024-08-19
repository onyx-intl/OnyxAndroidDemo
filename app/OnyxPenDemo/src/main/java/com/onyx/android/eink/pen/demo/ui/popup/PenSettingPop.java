package com.onyx.android.eink.pen.demo.ui.popup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.PenBundle;
import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.action.StrokeColorChangeAction;
import com.onyx.android.eink.pen.demo.action.StrokeStyleChangeAction;
import com.onyx.android.eink.pen.demo.action.StrokeWidthChangeAction;
import com.onyx.android.eink.pen.demo.data.ShapeTexture;
import com.onyx.android.eink.pen.demo.data.ShapeType;
import com.onyx.android.eink.pen.demo.data.StrokeColor;
import com.onyx.android.eink.pen.demo.databinding.LayoutPenSettingPopBinding;
import com.onyx.android.eink.pen.demo.databinding.LayoutPenSettingPopBrushItemBinding;
import com.onyx.android.eink.pen.demo.util.PenInfoUtils;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.ResManager;
import com.onyx.android.sdk.utils.ViewUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PenSettingPop extends BasePopup {
    private LayoutPenSettingPopBinding binding;
    private List<Float> strokeWidthValues;

    public PenSettingPop(Context context) {
        super(context);
        initPopupWindow();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        onShow();
    }

    private void onShow() {
        updateStrokeWidth(getCurrentStrokeWidth(), true);
    }

    private void initPopupWindow() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_pen_setting_pop, null, false);
        setContentView(binding.getRoot());

        setWidth(ResManager.getDimens(R.dimen.pen_popup_size));
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setStroke(2, Color.BLACK);
        setBackgroundDrawable(drawable);

        initBrushList();
        initColorList();
        initTextureList();
        initListener();
        initSeekBar();
    }

    private void initListener() {
        binding.minus.setOnClickListener(v -> {
            float currentStrokeWidth = getCurrentStrokeWidth();
            float width = getClickStrokeWidth(false, currentStrokeWidth);
            updateStrokeWidth(width);
        });
        binding.plus.setOnClickListener(v -> {
            float currentStrokeWidth = getCurrentStrokeWidth();
            float width = getClickStrokeWidth(true, currentStrokeWidth);
            updateStrokeWidth(width);
        });
    }

    private void initSeekBar() {
        initStrokeWidthValues();
        binding.widthSeekBar.setMax(strokeWidthValues.size() - 1);
        binding.widthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateStrokeWidth(strokeWidthValues.get(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initBrushList() {
        BrushSettingAdapter brushSettingAdapter = new BrushSettingAdapter(Arrays.asList(ShapeType.values())
                , ShapeType.find(getCurrentShapeType()));
        binding.brushList.setLayoutManager(new GridLayoutManager(context, brushSettingAdapter.options.size()));
        binding.brushList.setAdapter(brushSettingAdapter);
    }

    private void initColorList() {
        ColorSettingAdapter colorSettingAdapter = new ColorSettingAdapter(Arrays.asList(StrokeColor.values())
                , StrokeColor.find(getCurrentStrokeColor()));
        binding.colorList.setLayoutManager(new GridLayoutManager(context, colorSettingAdapter.options.size()));
        binding.colorList.setAdapter(colorSettingAdapter);
    }

    private void initTextureList() {
        List<ShapeTexture> textures = ShapeTexture.getShapeTextures(getCurrentShapeType());
        boolean showTexture = CollectionUtils.isNonBlank(textures);
        ViewUtils.setViewVisibleOrGone(binding.textureContainer, showTexture);
        if (showTexture) {
            TextureSettingAdapter adapter = new TextureSettingAdapter(textures, ShapeTexture.find(getCurrentTexture()));
            binding.textureList.setLayoutManager(new GridLayoutManager(context, adapter.options.size()));
            binding.textureList.setAdapter(adapter);
        }
    }

    private void initStrokeWidthValues() {
        strokeWidthValues = PenInfoUtils.getPenWidthRange(getCurrentShapeType());
    }

    private void updateStrokeWidth(final float width) {
        updateStrokeWidth(width, false);
    }

    private void updateStrokeWidth(final float lineWidth, boolean justInitView) {
        String lineWidthValue = Math.floor(lineWidth) == lineWidth ?
                String.format(Locale.getDefault(), "%.0f", lineWidth) : String.valueOf(lineWidth);
        binding.width.setText(lineWidthValue);
        binding.widthSeekBar.setProgress(strokeWidthValues.indexOf(lineWidth));
        if (!justInitView) {
            updateStrokeWidthImpl(lineWidth);
        }
    }

    private void updateStrokeWidthImpl(final float lineWidth) {
        new StrokeWidthChangeAction(getCurrentShapeType(), lineWidth).execute();
    }

    private float getClickStrokeWidth(boolean plusClick, float currentStrokeWidth) {
        int currentStrokeStyle = getCurrentShapeType();
        float gap = PenInfoUtils.getStrokeWidthGap(currentStrokeStyle, plusClick, currentStrokeWidth);
        if (plusClick) {
            return Math.min(currentStrokeWidth + gap, PenInfoUtils.getMaxStrokeWidth(currentStrokeStyle));
        } else {
            return Math.max(currentStrokeWidth - gap, PenInfoUtils.getMinStrokeWidth(currentStrokeStyle));
        }
    }

    private float getCurrentStrokeWidth() {
        return getPenBundle().getCurrentStrokeWidth();
    }

    private int getCurrentShapeType() {
        return getPenBundle().getCurrentShapeType();
    }

    private int getCurrentStrokeColor() {
        return getPenBundle().getCurrentStrokeColor();
    }

    private int getCurrentTexture() {
        return getPenBundle().getCurrentTexture();
    }

    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

    public class BrushSettingAdapter extends RecyclerView.Adapter<BrushSettingAdapter.ViewHolder> {
        private List<ShapeType> options;
        private ShapeType selectedShapeType;

        public BrushSettingAdapter(List<ShapeType> options, ShapeType selectedShapeType) {
            this.options = options;
            this.selectedShapeType = selectedShapeType;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pen_setting_pop_brush_item
                    , parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShapeType shapeType = options.get(position);
            holder.bindTo(shapeType, shapeType == selectedShapeType);
            holder.itemView.setOnClickListener(v -> {
                onBrushSettingImpl(shapeType);
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        private void onBrushSettingImpl(ShapeType shapeType) {
            selectedShapeType = shapeType;
            notifyDataSetChanged();
            new StrokeStyleChangeAction(selectedShapeType.getValue(), getPenBundle().getCurrentTexture())
                    .build()
                    .subscribe(strokeStyleChangeAction -> {
                        initSeekBar();
                        initTextureList();
                        updateStrokeWidth(getPenBundle().getPenLineWidth(shapeType.getValue()));
                    });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final LayoutPenSettingPopBrushItemBinding binding;

            public ViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindTo(ShapeType shapeType, boolean selected) {
                binding.title.setText(shapeType.getTextResId());
                binding.icon.setImageResource(shapeType.getIconResId());
                binding.radio.setChecked(selected);
                binding.executePendingBindings();
            }
        }

    }

    public class ColorSettingAdapter extends RecyclerView.Adapter<ColorSettingAdapter.ViewHolder> {
        private List<StrokeColor> options;
        private StrokeColor selectedStrokeColor;

        public ColorSettingAdapter(List<StrokeColor> options, StrokeColor selectedStrokeColor) {
            this.options = options;
            this.selectedStrokeColor = selectedStrokeColor;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pen_setting_pop_brush_item
                    , parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StrokeColor strokeColor = options.get(position);
            holder.bindTo(strokeColor, strokeColor == selectedStrokeColor);
            holder.itemView.setOnClickListener(v -> {
                selectedStrokeColor = strokeColor;
                new StrokeColorChangeAction(selectedStrokeColor.getValue()).execute();
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final LayoutPenSettingPopBrushItemBinding binding;

            public ViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindTo(StrokeColor strokeStyle, boolean selected) {
                binding.title.setText(strokeStyle.getTextResId());
                binding.radio.setChecked(selected);
                binding.executePendingBindings();
            }
        }

    }

    public class TextureSettingAdapter extends RecyclerView.Adapter<TextureSettingAdapter.ViewHolder> {
        private List<ShapeTexture> options;
        private ShapeTexture selectedShapeTexture;

        public TextureSettingAdapter(List<ShapeTexture> options, ShapeTexture selectedShapeTexture) {
            this.options = options;
            this.selectedShapeTexture = selectedShapeTexture;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pen_setting_pop_brush_item
                    , parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShapeTexture texture = options.get(position);
            holder.bindTo(texture, texture == selectedShapeTexture);
            holder.itemView.setOnClickListener(v -> {
                selectedShapeTexture = texture;
                new StrokeStyleChangeAction(getPenBundle().getCurrentShapeType(),
                        selectedShapeTexture.getTexture()).execute();
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final LayoutPenSettingPopBrushItemBinding binding;

            public ViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public void bindTo(ShapeTexture texture, boolean selected) {
                binding.title.setText(texture.getTextureTextResId());
                binding.radio.setChecked(selected);
                binding.executePendingBindings();
            }
        }

    }

}