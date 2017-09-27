/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.button;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;

import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_B;


/**
 * Example of using Button driver for toggling a LED.
 * <p>
 * This activity initialize an InputDriver to emit key events when the button GPIO pin state change
 * and flip the state of the LED GPIO pin.
 * <p>
 * You need to connect an LED and a push button switch to pins specified in {@link BoardDefaults}
 * according to the schematic provided in the sample README.
 */
public class ButtonActivity extends Activity {
    private static final String TAG = ButtonActivity.class.getSimpleName();

    private Gpio mLedRGpio, mLedGGpio;
    private ButtonInputDriver mButtonAInputDriver, mButtonBInputDriver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");

        PeripheralManagerService pioService = new PeripheralManagerService();
        try {
            Log.i(TAG, "Configuring GPIO pins");
            mLedRGpio = RainbowHat.openLedRed();
            mLedRGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGGpio = RainbowHat.openLedGreen();
            mLedGGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            Log.i(TAG, "Registering button driver");
            mButtonAInputDriver = RainbowHat.createButtonAInputDriver(KeyEvent.KEYCODE_A);
            mButtonAInputDriver.register();
            mButtonBInputDriver = RainbowHat.createButtonBInputDriver(KEYCODE_B);
            mButtonBInputDriver.register();
        } catch (IOException e) {
            Log.e(TAG, "Error configuring GPIO pins", e);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KEYCODE_A:
                return setLedValue(mLedRGpio, true);
            case KEYCODE_B:
                return setLedValue(mLedGGpio, true);
            default:
                return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KEYCODE_A:
                return setLedValue(mLedRGpio, false);
            case KEYCODE_B:
                return setLedValue(mLedGGpio, false);
            default:
                return false;
        }
    }

    private boolean setLedValue(final Gpio led, final boolean setOn) {
        try {
            led.setValue(setOn);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error setting led");
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mButtonAInputDriver != null) {
            mButtonAInputDriver.unregister();
            try {
                mButtonAInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally {
                mButtonAInputDriver = null;
            }
        }

        if (mLedRGpio != null) {
            try {
                mLedRGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Red LED GPIO", e);
            } finally {
                mLedRGpio = null;
            }
            mLedRGpio = null;
        }

        if (mLedGGpio != null) {
            try {
                mLedGGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Green LED GPIO", e);
            } finally {
                mLedGGpio = null;
            }
            mLedGGpio = null;
        }
    }
}
