package com.onyx.android.eink.pen.demo;

import android.graphics.Color;
import android.graphics.Rect;

import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.data.ShapeType;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.util.EraseUnits;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.eink.pen.demo.util.PenInfoUtils;
import com.onyx.android.sdk.data.note.PenTexture;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PenBundle {
    private static PenBundle instance;

    private PenManager penManager;
    private EventBus eventBus;

    public Map<Integer, Float> penLineWidthMap = new HashMap<>();
    private int currentShapeType = ShapeFactory.SHAPE_BRUSH_SCRIBBLE;
    private int currentStrokeColor = Color.BLACK;
    private int currentTexture = PenTexture.CHARCOAL_SHAPE_V1;
    private float currentStrokeWidth;

    private boolean isErasing = false;
    private int currentEraseType = EraseTypes.ERASER_STROKE;
    private int lastEraseType = EraseTypes.ERASER_STROKE;
    private final Map<Integer, Float> eraseWidthMap = new HashMap<>();
    private final Map<Integer, Boolean> displayEraseTrackMap = new HashMap<>();

    private boolean displayEraseTrack = false;
    private boolean enablePenUpRefresh = false;
    private int penUpRefreshTimeMs = 500;

    private List<Rect> excludeRectList;

    private PenBundle() {
        initDefaultPenLineWidth();
        initDefaultEraseSettings();
        setCurrentStrokeWidth(getPenLineWidth(currentShapeType));
    }

    private void initDefaultPenLineWidth() {
        for (ShapeType style : ShapeType.values()) {
            int shapeType = style.getValue();
            penLineWidthMap.put(shapeType, PenInfoUtils.getShapeDefaultStrokeWidth(shapeType));
        }
    }

    private void initDefaultEraseSettings() {
        int[] eraseTypes = new int[]{
                EraseTypes.ERASER_STROKE,
                EraseTypes.ERASER_MOVE,
                EraseTypes.ERASER_AREA
        };
        for (int eraseType : eraseTypes) {
            eraseWidthMap.put(eraseType, EraseUnits.getDefaultEraseWidth(eraseType));
            displayEraseTrackMap.put(eraseType, EraserTrackHelper.defaultTrackEnabled(eraseType));
        }
        displayEraseTrack = isDisplayEraseTrack(EraseTypes.ERASER_STROKE);
    }

    public static PenBundle getInstance() {
        if (instance == null) {
            instance = new PenBundle();
        }
        return instance;
    }

    public PenManager getPenManager() {
        if (penManager == null) {
            penManager = new PenManager(getEventBus());
        }
        return penManager;
    }

    public EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }
        return eventBus;
    }

    public int getCurrentShapeType() {
        return currentShapeType;
    }

    public void setCurrentShapeType(int currentShapeType) {
        this.currentShapeType = currentShapeType;
    }

    public float getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }

    public void setCurrentStrokeWidth(float currentStrokeWidth) {
        this.currentStrokeWidth = currentStrokeWidth;
    }

    public int getCurrentStrokeColor() {
        return currentStrokeColor;
    }

    public void setCurrentStrokeColor(int currentStrokeColor) {
        this.currentStrokeColor = currentStrokeColor;
    }

    public int getCurrentTexture() {
        return currentTexture;
    }

    public void setCurrentTexture(int currentTexture) {
        this.currentTexture = currentTexture;
    }

    public void savePenLineWidth(int shapeType, float lineWidth) {
        penLineWidthMap.put(shapeType, lineWidth);
    }

    public float getPenLineWidth(int shapeType) {
        Float lineWidth = penLineWidthMap.get(shapeType);
        if (lineWidth != null) {
            return lineWidth;
        }
        return PenInfoUtils.getShapeDefaultStrokeWidth(shapeType);
    }

    public boolean isErasing() {
        return isErasing;
    }

    public void setErasing(boolean erasing) {
        isErasing = erasing;
    }

    public void resetToolToBrushOnSessionEnd() {
        isErasing = false;
    }

    public boolean isEraseTool() {
        return isErasing();
    }

    public int getCurrentEraseType() {
        return currentEraseType;
    }

    public void setCurrentEraseType(int currentEraseType) {
        this.currentEraseType = currentEraseType;
        this.lastEraseType = currentEraseType;
        displayEraseTrack = isDisplayEraseTrack(currentEraseType);
    }

    public int getLastEraseType() {
        return lastEraseType;
    }

    public void selectEraseType(int eraseType) {
        setCurrentEraseType(eraseType);
    }

    public float getEraseWidth(int eraseType) {
        Float width = eraseWidthMap.get(eraseType);
        if (width != null) {
            return width;
        }
        float defaultWidth = EraseUnits.getDefaultEraseWidth(eraseType);
        eraseWidthMap.put(eraseType, defaultWidth);
        return defaultWidth;
    }

    public void setEraseWidth(int eraseType, float eraseWidth) {
        eraseWidthMap.put(eraseType, EraseUnits.clampEraseWidth(eraseWidth, eraseType));
    }

    public float getCurrentEraseWidth() {
        return getEraseWidth(currentEraseType);
    }

    public void setCurrentEraseWidth(float currentEraseWidth) {
        setEraseWidth(currentEraseType, currentEraseWidth);
    }

    public boolean isDisplayEraseTrack() {
        return isDisplayEraseTrack(currentEraseType);
    }

    public boolean isDisplayEraseTrack(int eraseType) {
        Boolean displayTrack = displayEraseTrackMap.get(eraseType);
        if (displayTrack != null) {
            return displayTrack;
        }
        boolean defaultValue = EraserTrackHelper.defaultTrackEnabled(eraseType);
        displayEraseTrackMap.put(eraseType, defaultValue);
        return defaultValue;
    }

    public void setDisplayEraseTrack(boolean displayEraseTrack) {
        this.displayEraseTrack = displayEraseTrack;
        setDisplayEraseTrack(currentEraseType, displayEraseTrack);
    }

    public void setDisplayEraseTrack(int eraseType, boolean displayEraseTrack) {
        displayEraseTrackMap.put(eraseType, displayEraseTrack);
        if (eraseType == currentEraseType) {
            this.displayEraseTrack = displayEraseTrack;
        }
    }

    public boolean isEnablePenUpRefresh() {
        return enablePenUpRefresh;
    }

    public void setEnablePenUpRefresh(boolean enablePenUpRefresh) {
        this.enablePenUpRefresh = enablePenUpRefresh;
    }

    public int getPenUpRefreshTimeMs() {
        return penUpRefreshTimeMs;
    }

    public void setPenUpRefreshTimeMs(int penUpRefreshTimeMs) {
        this.penUpRefreshTimeMs = penUpRefreshTimeMs;
    }

    public List<Rect> getExcludeRectList() {
        return excludeRectList;
    }

    public void setExcludeRectList(List<Rect> excludeRectList) {
        this.excludeRectList = excludeRectList;
    }
}
