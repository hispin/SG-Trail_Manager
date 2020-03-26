package com.sensoguard.hunter.classes


class Camera {

    private var longitude: Double?=null
    private var latitude: Double?=null
    private var sysName: String? = null
    private var id: String?=null
    private var isArmed = false
    var isLocallyDefined:Boolean=false

    //TODO make getter and setter
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


    //private var type:Int?=null

    constructor( _uid: String?,_name: String?){
        id=_uid
        sysName = _name
        isArmed = true
    }
    constructor( _id: String?){
        id=_id
        sysName = "id-$id"
        isArmed = true
    }

    fun getId():String{
        return id.toString()
    }

    fun setId(_id:String){
         id=_id
    }

    fun getName(): String? {
        return sysName
    }

    fun setName(_name:String){
        sysName = _name
    }

    fun setArm(state:Boolean){
        isArmed=state
    }

    fun isArmed():Boolean{
        return isArmed
    }

    fun getLatitude():Double?{
        return latitude
    }

    fun getLongtitude(): Double? {
        return longitude
    }

    fun setLatitude(_latitude:Double?){
        latitude=_latitude
    }
    fun setLongtitude(_longitude:Double?){
        longitude=_longitude
    }


}