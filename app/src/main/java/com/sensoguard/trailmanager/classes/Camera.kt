package com.sensoguard.trailmanager.classes


//a camera, stored locally as JSON (the field names are part of the stored data)
class Camera {

    private var longitude: Double? = null
    private var latitude: Double? = null
    private var sysName: String? = null
    private var id: String? = null
    private var isArmed = false

    var phoneNum: String? = null
    var cameraModel: String? = null
    var cameraModelPosition: Int = 0
    var emailAddress: String? = null
    var password: String? = null
    var emailServer: String? = null
    var emailPort: String? = null
    var isUseSSL: Boolean = false
    var lastVisitDate: String? = null
    var lastVisitPicturePath: String? = null
    var isSorted: Boolean = false

    constructor(id: String?) {
        this.id = id
        sysName = "id-$id"
        isArmed = true
    }

    constructor(id: Int) {
        this.id = id.toString()
        sysName = "id-${this.id}"
        isArmed = true
    }

    fun getId(): String {
        return id.toString()
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getName(): String? {
        return sysName
    }

    fun setName(name: String) {
        sysName = name
    }

    fun setArm(state: Boolean) {
        isArmed = state
    }

    fun isArmed(): Boolean {
        return isArmed
    }

    fun getLatitude(): Double? {
        return latitude
    }

    fun getLongitude(): Double? {
        return longitude
    }

    fun setLatitude(latitude: Double?) {
        this.latitude = latitude
    }

    fun setLongitude(longitude: Double?) {
        this.longitude = longitude
    }
}
