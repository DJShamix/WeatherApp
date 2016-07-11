package com.example.geek.testingapp.service;

import com.example.geek.testingapp.data.Channel;

/**
 * Created by Geek on icon_18.06.2016.
 */
public interface WeatherServiceCallback {
    void serviceSuccess(Channel channel);
    void serviceFailure(Exception exception);
}
