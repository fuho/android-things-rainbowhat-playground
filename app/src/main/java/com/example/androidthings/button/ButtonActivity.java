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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;

import android.util.Log;
import android.view.KeyEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;

import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_B;
import static com.example.androidthings.button.Utils.hexStringToByteArray;


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
    public static final String UART_DEVICE_NAME = "UART6";

    private UartDevice mDevice;
    private Gpio mLedRGpio, mLedGGpio;
    private ButtonInputDriver mButtonAInputDriver, mButtonBInputDriver;
    private AlphanumericDisplay mAlphanumericDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");
        initUart();
        initLeds();
        initButtons();
        initAlphanumericDisplay();

        logUartDevices();
    }

    private void printTable() {
        if (mDevice == null) {
            Log.w(TAG, "Can't print to UART, it is null");
            return;
        }
        final StringBuilder builder = new StringBuilder();
        builder.append("0123456789abcdef");
        for(int y = 0; y < 16; y++){
            builder.append(Integer.toHexString(y));
            for(int x = 0; x < 16; x++){
                builder.append("x");
            }
            builder.append("\n");
        }
/*
        for row in range(16):
        s.write('{0:02d} '.format(row))
        for col in range(16):
        val = row * 16 + col
        s.write(chr(val))
        s.write("\n")
*/

        // final byte[] buffer = hexStringToByteArray(builder.toString());
        final byte[] buffer = builder.toString().getBytes();
        try {
            mDevice.write(buffer, buffer.length);
        } catch (IOException e) {
            Log.w(TAG, "Error printing sample",e);
        }

    }

    private void initUart() {
        Log.i(TAG, "Registering UART device");
        try {
            PeripheralManagerService managerService = new PeripheralManagerService();
            mDevice = managerService.openUartDevice(UART_DEVICE_NAME);
            mDevice.setBaudrate(9600);
            mDevice.setDataSize(8);
            mDevice.setParity(UartDevice.PARITY_NONE);
            mDevice.setStopBits(1);
        } catch (IOException e) {
            Log.w(TAG, String.format("Unable to access UART device: %s", UART_DEVICE_NAME), e);
        }
    }

    private void initAlphanumericDisplay() {
        try {
            mAlphanumericDisplay = RainbowHat.openDisplay();
            mAlphanumericDisplay.setEnabled(true);
            mAlphanumericDisplay.clear();
        } catch (IOException e) {
            Log.e(TAG, "Error configuring Alphanumeric Display", e);
        }
    }

    private void initButtons() {
        Log.i(TAG, "Registering button driver");
        try {
            mButtonAInputDriver = RainbowHat.createButtonAInputDriver(KeyEvent.KEYCODE_A);
            mButtonAInputDriver.register();
            mButtonBInputDriver = RainbowHat.createButtonBInputDriver(KEYCODE_B);
            mButtonBInputDriver.register();
        } catch (IOException e) {
            Log.e(TAG, "Error registering buttons", e);
        }
    }

    private void initLeds() {
        Log.i(TAG, "Configuring LED pins");
        try {
            mLedRGpio = RainbowHat.openLedRed();
            mLedRGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGGpio = RainbowHat.openLedGreen();
            mLedGGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error configuring LEDs", e);
        }
    }

    private void logUartDevices() {
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> deviceList = manager.getUartDeviceList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No UART port available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        displayText("");
        switch (keyCode) {
            case KEYCODE_A:
                displayText("LedA");
                return setLedValue(mLedRGpio, true);
            case KEYCODE_B:
                displayText("LedB");
                printTable();
                return setLedValue(mLedGGpio, true);
            default:
                return false;
        }
    }

    private void displayText(final String text) {
        try {
            mAlphanumericDisplay.display(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
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
            Log.e(TAG, "Error setting led");
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
        }

        if (mLedGGpio != null) {
            try {
                mLedGGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Green LED GPIO", e);
            } finally {
                mLedGGpio = null;
            }
        }

        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing UART device", e);
            } finally {
                mDevice = null;
            }
        }
    }
}
