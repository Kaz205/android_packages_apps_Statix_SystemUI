/*
 * Copyright (C) 2022 StatiXOS
 * SPDX-License-Identifer: Apache-2.0
 */

package com.statix.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.statix.hardware.LineageHardwareManager;
import com.android.systemui.R;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import javax.inject.Inject;

public class GloveModeTile extends QSTileImpl<BooleanState> {

    public static final String TILE_SPEC = "glovemode";

    private LineageHardwareManager mHardwareManager;

    @Inject
    public GloveModeTile(
            QSHost host,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger) {
        super(
                host,
                backgroundLooper,
                mainHandler,
                falsingManager,
                metricsLogger,
                statusBarStateController,
                activityStarter,
                qsLogger);
        mHardwareManager = LineageHardwareManager.getInstance(mContext);
    }

    @Override
    public boolean isAvailable() {
        return mHardwareManager.isSupported(LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY);
    }

    @Override
    public BooleanState newTileState() {
        BooleanState state = new BooleanState();
        state.handlesLongClick = false;
        return state;
    }

    @Override
    public void handleClick(@Nullable View view) {
        boolean enabled =
                mHardwareManager.get(LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY);
        mHardwareManager.set(LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY, !enabled);
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_glove_mode_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (!isAvailable()) {
            return;
        }

        if (state.slash == null) {
            state.slash = new SlashState();
        }

        state.icon = ResourceIcon.get(R.drawable.ic_qs_glove_mode);
        state.value = mHardwareManager.get(LineageHardwareManager.FEATURE_HIGH_TOUCH_SENSITIVITY);
        state.slash.isSlashed = state.value;
        state.label = mContext.getString(R.string.quick_settings_glove_mode_label);
        state.state = state.value ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_CUSTOM;
    }

    @Override
    public void handleSetListening(boolean listening) {}
}
