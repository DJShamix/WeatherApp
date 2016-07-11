/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Yoel Nunez <dev@nunez.guru>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.example.geek.testingapp.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Units implements JSONPopulator {
    private String distance;
    private String pressure;
    private String speed;
    private String temperature;

    public String getTemperature() {
        return temperature;
    }

    public String getDistance() {
        return distance;
    }

    public String getPressure() {
        return pressure;
    }

    public String getSpeed() {
        return speed;
    }

    @Override
    public void populate(JSONObject data) {
        distance = data.optString("distance");
        pressure = data.optString("pressure");
        speed = data.optString("speed");
        temperature = data.optString("temperature");
    }

    @Override
    public JSONObject toJSON() {
        JSONObject data = new JSONObject();

        try {
            data.put("distance", distance);
            data.put("pressure", pressure);
            data.put("speed", speed);
            data.put("temperature", temperature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
