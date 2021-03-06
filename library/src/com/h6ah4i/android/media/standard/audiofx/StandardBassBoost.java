/*
 *    Copyright (C) 2014 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package com.h6ah4i.android.media.standard.audiofx;

import android.util.Log;

import com.h6ah4i.android.media.audiofx.IAudioEffect;
import com.h6ah4i.android.media.audiofx.IBassBoost;
import com.h6ah4i.android.media.utils.AudioEffectSettingsConverter;

public class StandardBassBoost extends android.media.audiofx.BassBoost implements IBassBoost {
    private static final String TAG = "StandardBassBoost";

    private Object mOnParameterChangeListenerLock = new Object();
    private IBassBoost.OnParameterChangeListener mUserOnParameterChangeListener;

    private android.media.audiofx.BassBoost.OnParameterChangeListener mOnParameterChangeListener = new android.media.audiofx.BassBoost.OnParameterChangeListener() {
        @Override
        public void onParameterChange(
                android.media.audiofx.BassBoost effect, int status, int param, short value) {
            StandardBassBoost.this.onParameterChange(effect, status, param, value);
        }
    };

    public StandardBassBoost(int priority, int audioSession) throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(priority, audioSession);
        super.setParameterListener(mOnParameterChangeListener);
        initializeForCompat();
    }

    @Override
    public void release() {
        mUserOnParameterChangeListener = null;
        super.release();
    }

    @Override
    public void setPropertiesCompat(IBassBoost.Settings settings)
            throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        verifySettings(settings);
        super.setProperties(AudioEffectSettingsConverter.convert(settings));
    }

    @Override
    public IBassBoost.Settings getPropertiesCompat() throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        return AudioEffectSettingsConverter.convert(super.getProperties());
    }

    @Override
    public void setProperties(android.media.audiofx.BassBoost.Settings settings)
            throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        throwUseIBassBoostVersionMethod();
    };

    @Override
    public android.media.audiofx.BassBoost.Settings getProperties() throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        throwUseIBassBoostVersionMethod();
        return null;
    }

    private static void throwUseIBassBoostVersionMethod() {
        throw new IllegalStateException(
                "This method is not supported, please use IBassBoost version");
    }

    @Override
    public void setParameterListener(IBassBoost.OnParameterChangeListener listener) {
        synchronized (mOnParameterChangeListenerLock) {
            mUserOnParameterChangeListener = listener;
        }
    }
    
    @Override
    public void setControlStatusListener(IAudioEffect.OnControlStatusChangeListener listener)
            throws IllegalStateException {
        super.setControlStatusListener(StandardAudioEffect.wrap(this, listener));
    }
    
    @Override
    public void setEnableStatusListener(IAudioEffect.OnEnableStatusChangeListener listener)
            throws IllegalStateException {
        super.setEnableStatusListener(StandardAudioEffect.wrap(this, listener));
    }

    @Override
    public void setParameterListener(
            android.media.audiofx.BassBoost.OnParameterChangeListener listener) {
        throwUseIBassBoostVersionMethod();
    }

    /* package */void onParameterChange(
            android.media.audiofx.BassBoost effect,
            int status, int param, short value) {
        IBassBoost.OnParameterChangeListener listener = null;

        synchronized (mOnParameterChangeListenerLock) {
            listener = mUserOnParameterChangeListener;
        }

        if (listener != null) {
            listener.onParameterChange((StandardBassBoost) effect, status, param, value);
        }
    }

    // === Fix unwanted behaviors ===
    private static final short STRENGTH_MIN = 0;
    private static final short STRENGTH_MAX = 1000;
    private static final short DEFAULT_STRENGTH = 0;

    void initializeForCompat() {
        try {
            super.setStrength(DEFAULT_STRENGTH);
        } catch (IllegalStateException e) {
            Log.e(TAG, "initializeForCompat()", e);
        }
    }

    @Override
    public void setStrength(short strength) throws IllegalStateException, IllegalArgumentException,
            UnsupportedOperationException {
        verifyStrengthParameterRange(strength);
        super.setStrength(strength);
    }

    private static void verifyStrengthParameterRange(short strength) {
        if (!(strength >= STRENGTH_MIN && strength <= STRENGTH_MAX))
            throw new IllegalArgumentException("bad parameter value: strength = " + strength);
    }

    private static void verifySettings(IBassBoost.Settings settings) {
        if (settings == null)
            throw new IllegalArgumentException("The parameter 'settings' is null");
        verifyStrengthParameterRange(settings.strength);
    }
    // ==============================
}
