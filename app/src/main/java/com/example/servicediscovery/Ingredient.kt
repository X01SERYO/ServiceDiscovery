package com.example.servicediscovery

class Ingredient(s: String, deviceAddress: String) {

    var mac: String? = null
    var name: String? = null
    private var longitude = 0.0
    private var latitude = 0.0
    private var date: String? = null
    private var indice: String? = null
    private var distance = 0.0
    private var index = 1

    fun Ingredient() {}

    fun Ingredient(name: String?, mac: String?) {
        this.name = name
        this.mac = mac ///// S G K /////
    }

    fun Ingredient(name: String?, mac: String?, indice: String?) {
        this.name = name
        this.mac = mac ///// S G K /////
        this.indice = indice
    }

    fun Ingredient(name: String?, mac: String?, index: Int) {
        this.name = name
        this.mac = mac ///// S G K /////
        this.index = index
    }

    override fun toString(): String {
        return if (name != null) {
            name.toString()
        } else null.toString()
    }


    fun getLongitude(): Double {
        return longitude
    }

    fun setLongitude(longitude: Double) {
        this.longitude = longitude
    }

    fun getLatitude(): Double {
        return latitude
    }

    fun setLatitude(latitude: Double) {
        this.latitude = latitude
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String?) {
        this.date = date
    }

    fun getIndice(): String? {
        return indice
    }

    fun setIndice(indice: String?) {
        this.indice = indice
    }

    fun getDistance(): Double {
        return distance
    }

    fun setDistance(distance: Double) {
        this.distance = distance
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }

    @JvmName("getMac1")
    fun getMac(): String? {
        return mac
    }

    @JvmName("setMac1")
    fun setMac(mac: String?) {
        this.mac = mac
    }

    @JvmName("getName1")
    fun getName(): String? {
        return name
    }

    @JvmName("setName1")
    fun setName(name: String?) {
        this.name = name
    }
}