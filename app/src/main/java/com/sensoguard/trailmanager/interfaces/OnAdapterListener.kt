package com.sensoguard.trailmanager.interfaces

import com.sensoguard.trailmanager.classes.Camera

interface OnAdapterListener {
    fun saveNameSensor(detector: Camera)
    fun saveCamera(detector: Camera)
}