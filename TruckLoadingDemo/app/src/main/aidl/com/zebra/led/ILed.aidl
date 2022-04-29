// Copyright (c) 2020-2021 Zebra Technologies Corporation and/or its affiliates. All rights reserved.

package com.zebra.led;

/**
 * Interface for apps to control device LEDs. Use this interface when you wish to control LEDs
 * beyond what is available in Android's Notification API; for example, to light an LED without
 * showing an on-screen notification, or to specify which LED when there is more than one (such as
 * on the WS50).
 */
interface ILed
{
    /** LED id for a single app-controlled LED (available on Sharp RZ-H270, red and green
        colors only) */
    const int LED_USER = 1;
    /** LED id for the left LED on devices with multiple LEDs (available on Zebra WS50) */
    const int LED_LEFT = 2;
    /** LED id for the right LED on devices with multiple LEDs (available on Zebra WS50) */
    const int LED_RIGHT = 3;

    /**
     * Sets an LED to a solid color. LED and color availability varies by product.
     *
     * @param ledID the LED to control, one of the LED_ constants
     * @param color the color to set, in standard Android ARGB format. The opacity (A) is
     *              ignored. Use Color.TRANSPARENT (0) to turn the LED off.
     * @throws IllegalArgumentException the LED is not available to control on this product, or the
     *                                  the given color is not supported for the LED
     */
    void setLed(int ledId, int color);

    /**
     * Sets an LED to a blinking color. LEDs, color availability, and blinking support varies by
     * product.
     *
     * @param ledID the LED to control, one of the LED_ constants
     * @param color the color to set, in standard Android ARGB format. The opacity (A) is
     *              ignored.
     * @throws IllegalArgumentException the LED is not available to control on this product, or the
     *                                  the given color is not supported for the LED
     * @throws UnsupportedOperationException blinking is not supported for this product or LED
     */
    void setLedBlinking(int ledId, int color);
}
