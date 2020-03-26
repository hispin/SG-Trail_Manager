package com.sensoguard.hunter.interfaces

import com.sensoguard.hunter.classes.Camera

interface OnAdapterListener {
    fun saveNameSensor(detector: Camera)
    fun saveCamera(detector: Camera)
}