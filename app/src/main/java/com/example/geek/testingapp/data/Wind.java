package com.example.geek.testingapp.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Wind implements JSONPopulator {
    private int chill;
    private int direction;
    private double speed;

    public int getChill() {
        return chill;
    }

    public int getDirection() {
        return direction;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public void populate(JSONObject data) {
        chill = data.optInt("chill");
        direction = data.optInt("direction");
        speed = data.optDouble("speed");
    }

    @Override
    public JSONObject toJSON() {
        JSONObject data = new JSONObject();

        try {
            data.put("chill", chill);
            data.put("direction", direction);
            data.put("speed", speed);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
