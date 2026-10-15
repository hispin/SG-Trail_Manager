package com.sensoguard.trailmanager.interfaces

import com.sensoguard.trailmanager.classes.Camera

//callback from an adapter of cameras to the fragment
interface OnAdapterListener {
    //the user renamed the camera
    fun saveNameSensor(camera: Camera)
}
