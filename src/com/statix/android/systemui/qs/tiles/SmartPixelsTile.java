/*
 * Copyright (C) 2018 CarbonROM
 * Copyright (C) 2018 Adin Kwok (adinkwok)
 * Copyright (C) 2022 StatiXOS
 * SPDX-License-Identifer: Apache-2.0
 */

package com.statix.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.view.View;

import com.android.internal.logging.MetricsLogger;
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
import com.android.systemui.statusbar.policy.BatteryController;

import javax.inject.Inject;

public class SmartPixelsTile extends QSTileImpl<BooleanState>
        implements BatteryController.BatteryStateChangeCallback {

    public static final String TILE_SPEC = "smartpixels";

    private static final ComponentName SMART_PIXELS_SETTING_COMPONENT =
            new ComponentName(
                    "com.android.settings",
                    "com.statix.android.settings.StatixSettings$SmartPixelsActivity");

    private static final Intent SMART_PIXELS_SETTINGS =
            new Intent().setComponent(SMART_PIXELS_SETTING_COMPONENT);

    private final BatteryController mBatteryController;

    private boolean mSmartPixelsEnable;
    private boolean mSmartPixelsOnPowerSave;
    private boolean mLowPowerMode;
    private boolean mListening;

    @Inject
    public SmartPixelsTile(
            QSHost host,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger,
            BatteryController batteryController) {
        super(
                host,
                backgroundLooper,
                mainHandler,
                falsingManager,
                metricsLogger,
                statusBarStateController,
                activityStarter,
                qsLogger);
        mBatteryController = batteryController;
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (listening) {
            mBatteryController.addCallback(this);
        } else {
            mBatteryController.removeCallback(this);
        }
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources()
                .getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
    }

    @Override
    public void handleClick(View v) {
        mSmartPixelsEnable =
                (Settings.System.getIntForUser(
                                mContext.getContentResolver(),
                                Settings.System.SMART_PIXELS_ENABLE,
                                0,
                                UserHandle.USER_CURRENT)
                        == 1);
        mSmartPixelsOnPowerSave =
                (Settings.System.getIntForUser(
                                mContext.getContentResolver(),
                                Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                                0,
                                UserHandle.USER_CURRENT)
                        == 1);
        if (mLowPowerMode && mSmartPixelsOnPowerSave) {
            Settings.System.putIntForUser(
                    mContext.getContentResolver(),
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                    0,
                    UserHandle.USER_CURRENT);
            Settings.System.putIntForUser(
                    mContext.getContentResolver(),
                    Settings.System.SMART_PIXELS_ENABLE,
                    0,
                    UserHandle.USER_CURRENT);
        } else if (!mSmartPixelsEnable) {
            Settings.System.putIntForUser(
                    mContext.getContentResolver(),
                    Settings.System.SMART_PIXELS_ENABLE,
                    1,
                    UserHandle.USER_CURRENT);
        } else {
            Settings.System.putIntForUser(
                    mContext.getContentResolver(),
                    Settings.System.SMART_PIXELS_ENABLE,
                    0,
                    UserHandle.USER_CURRENT);
        }
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return SMART_PIXELS_SETTINGS;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        mSmartPixelsEnable =
                (Settings.System.getIntForUser(
                                mContext.getContentResolver(),
                                Settings.System.SMART_PIXELS_ENABLE,
                                0,
                                UserHandle.USER_CURRENT)
                        == 1);
        mSmartPixelsOnPowerSave =
                (Settings.System.getIntForUser(
                                mContext.getContentResolver(),
                                Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                                0,
                                UserHandle.USER_CURRENT)
                        == 1);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_smart_pixels);
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        if (mLowPowerMode && mSmartPixelsOnPowerSave) {
            state.label = mContext.getString(R.string.quick_settings_smart_pixels_on_power_save);
            state.value = true;
        } else if (mSmartPixelsEnable) {
            state.label = mContext.getString(R.string.quick_settings_smart_pixels);
            state.value = true;
        } else {
            state.label = mContext.getString(R.string.quick_settings_smart_pixels);
            state.value = false;
        }
        state.slash.isSlashed = !state.value;
        state.state = state.value ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_smart_pixels);
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean plugged, boolean charging) {
        // yurt
    }

    @Override
    public void onPowerSaveChanged(boolean active) {
        mLowPowerMode = active;
        refreshState();
    }
}
