/*
 * Miasma Minecraft Mod
 * Copyright © 2021 Dashkal <dashkal@darksky.ca>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package name.dashkal.minecraft.miasma.lib.client.render;

import org.apache.commons.lang3.StringUtils;

/**
 * Class representing a color, in both RGB and HSV.
 */
public class Color {
    public final float red;
    public final float green;
    public final float blue;

    public final float hue;
    public final float saturation;
    public final float value;

    public final float alpha;

    private Color(float red, float green, float blue, float hue, float saturation, float value, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
        this.alpha = alpha;
    }

    public static Color fromRGB(float red, float green, float blue, float alpha) {
        // https://www.rapidtables.com/convert/color/rgb-to-hsv.html
        float _red = clampFloat(red);
        float _green = clampFloat(green);
        float _blue = clampFloat(blue);

        float max = Math.max(Math.max(_red, _green), _blue);
        float min = Math.min(Math.min(_red, _green), _blue);
        float delta = max - min;

        float _hue;
        if (delta == 0.0f) {
            _hue = 0.0f;
        } else if (_red >= _green && _red >= _blue) {
            _hue = 60.0f * (((_green - _blue) / delta) % 6.0f);
        } else if (_green >= _red && _green >= _blue) {
            _hue = 60.0f * (((_blue - _red) / delta) + 2.0f);
        } else {
            _hue = 60.0f * (((_red - _green) / delta) + 4.0f);
        }

        float _saturation;
        if (max == 0.0f) {
            _saturation = 0;
        } else {
            _saturation = delta / max;
        }

        float _value = max;

        alpha = clampFloat(alpha);

        return new Color(_red, _green, _blue, _hue, _saturation, _value, alpha);
    }

    public static Color fromHSV(float hue, float saturation, float value, float alpha) {
        // https://www.rapidtables.com/convert/color/hsv-to-rgb.html
        float _hue = Math.max(0.0f, Math.min(hue, 360.0f));
        float _saturation = clampFloat(saturation);
        float _value = clampFloat(value);
        float _alpha = clampFloat(alpha);

        if (saturation == 0.0f) {
            return new Color(_value, _value, _value, _hue, _saturation, _value, _alpha);
        } else {
            float c = _value * _saturation;
            float x = c * (1 - Math.abs(((_hue / 60.0f) % 2.0f) - 1));
            float m = _value - c;

            float _red;
            float _green;
            float _blue;

            if (_hue < 60.0f) {
                _red = c;
                _green = x;
                _blue = 0;
            } else if (_hue < 120.0f) {
                _red = x;
                _green = c;
                _blue = 0;
            } else if (_hue < 180.0f) {
                _red = 0;
                _green = c;
                _blue = x;
            } else if (_hue < 240.0f) {
                _red = 0;
                _green = x;
                _blue = c;
            } else if (_hue < 300.0f) {
                _red = x;
                _green = 0;
                _blue = c;
            } else {
                _red = c;
                _green = 0;
                _blue = x;
            }

            return new Color(_red + m, _green + m, _blue + m, _hue, _saturation, _value, _alpha);
        }
    }

    public static Color fromPackedRGB(long color) {
        return fromPackedRGBA(0xFF000000 | color);
    }

    public static Color fromPackedRGBA(long color) {
        return fromRGB(
                argbToColorChannel(color, 16),
                argbToColorChannel(color, 8),
                argbToColorChannel(color, 0),
                argbToColorChannel(color, 24)
        );
    }

    public int getColorRGB() {
        return (getColorARGB() & 0xffffff);
    }

    public int getColorARGB() {
        return (int) (colorChannelToARGB(alpha, 24)
                | colorChannelToARGB(red, 16)
                | colorChannelToARGB(green, 8)
                | colorChannelToARGB(blue, 0));
    }

    public Color withRed(float red) {
        return fromRGB(red, green, blue, alpha);
    }

    public Color withGreen(float green) {
        return fromRGB(red, green, blue, alpha);
    }

    public Color withBlue(float blue) {
        return fromRGB(red, green, blue, alpha);
    }

    public Color withHue(float hue) {
        return fromHSV(hue, saturation, value, alpha);
    }

    public Color withSaturation(float saturation) {
        return fromHSV(hue, saturation, value, alpha);
    }

    public Color withValue(float value) {
        return fromHSV(hue, saturation, value, alpha);
    }

    public Color withAlpha(float alpha) {
        return new Color(red, green, blue, hue, saturation, value, alpha);
    }

    public Color multiply(Color color) {
        return fromRGB(this.red * color.red, this.green * color.green, this.blue * color.blue, color.alpha);
    }

    @Override
    public String toString() {
        return String.format(
                "MiasmaColor(r:%.2f, g:%.2f, b:%.2f, h:%s, s:%.2f, v:%.2f, a:%.2f, argb:%s)",
                red, green, blue, StringUtils.leftPad(String.format("%.1f", hue), 5), saturation, value, alpha,
                "0x" + getColorARGBString()
        );
    }

    private static float clampFloat(float f) {
        return Math.max(0.0f, Math.min(f, 1.0f));
    }

    private static float argbToColorChannel(long colorARGB, int shift) {
        return (float) ((colorARGB >> shift) & 0xFF) / 255.0F;
    }

    private static long colorChannelToARGB(float colorChannel, int shift) {
        return (long) (colorChannel * 255.0F) << shift;
    }

    private String getColorARGBString() {
        long l = getColorARGB();
        if (l < 1) { l += Integer.MAX_VALUE; }
        return StringUtils.leftPad(Long.toString(l, 16), 8, '0');
    }
}
