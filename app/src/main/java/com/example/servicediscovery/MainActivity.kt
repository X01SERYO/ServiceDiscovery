package com.example.servicediscovery

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.database.DataSetObserver
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var texto :TextView
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var wifiP2pChannel: WifiP2pManager.Channel
    var longitude = 0.0
    var latitude = 0.0
    var indice = 0.0
    private lateinit var ingredients: ArrayList<Ingredient>
    private lateinit var record: Map<*, *>
    private var lv: ListView? = null
    private var distance = 0.0
    private var serviceInfo: WifiP2pDnsSdServiceInfo? = null
    //var adapter: MapDevicesAdapter? = null
    private lateinit var mProgressDialog: ProgressDialog
    var wifiManager: WifiManager? = null

    //MODULO SERVICE
    private val TAG = "MainActivity"
    //private val intent: Intent? = null
    private val token = "MainActivity"
    private val button: Button? = null
    private val datei: Date? = null

    //Variables Calculo Reference ideal Acelerometer and Proximity
    private val RANGE_AP_A = 0
    private val RANGE_AP_B = 720
    private val REFERENCE_IDEAL_AP_C = 0
    private val REFERENCE_IDEAL_AP_D = 10

    //Variables calculo Reference ideal Battery
    private val RANGE_BATTERY_A = 0
    private val RANGE_BATTERY_B = 100
    private val REFERENCE_IDEAL_BATTERY_C = 100
    private val REFERENCE_IDEAL_BATTERY_D = 100

    //Variables criteria values
    private val CRITERIA_ACELEROMETER_VALUE = 0.5
    private val CRITERIA_PROXIMITY_VALUE = 0.17
    private val CRITERIA_BATTERY_VALUE = 0.33
    private val weightNormalized: List<Double> = ArrayList()

    //Estado de la bateria
    private var bm: BatteryManager? = null
    private val percentageBattery = 0

    //Obtain data Sharedpreference
    val PREFERENCES_DATE = "saveDate"
    val ACELEROMETER_ITERATION_DATE = "acelerometerIterationKey"
    val PROXIMITY_ITERATION_DATE = "proximityIterationKey"
    private var sharedpreferences: SharedPreferences? = null
    private val sharedPreferencesRelativeIndex: SharedPreferences? = null
    private val editorRelativeIndex: Editor? = null

    //Nombre extern file
    private val filenameIndexRelative = "IndexRelativeFile"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        texto = findViewById(R.id.texto)

        //Mofificacion modulo service
        //Mofificacion modulo service
        sharedpreferences = getSharedPreferences(PREFERENCES_DATE,MODE_PRIVATE)
        bm = getSystemService(BATTERY_SERVICE) as BatteryManager

        //Fin modificacion
        //turnGPSOn()
        record = HashMap<Any?, Any?>()

        wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        ingredients = ArrayList<Ingredient>()

        wifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        wifiP2pChannel = wifiP2pManager.initialize(applicationContext, mainLooper, null)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Buscando dispositivos por favor espere")
        mProgressDialog.setOnCancelListener(object : DialogInterface.OnCancelListener {
            override fun onCancel(dialogInterface: DialogInterface) {
                onBackPressed()
            }
        })
        mProgressDialog.show()

        val handler = Handler()
        handler.postDelayed(Runnable {
            //val sRelativeIndex = String.format("%.2f", calcularIndicedeActividad())
            (record as HashMap<Any?, Any?>).put("longitude", longitude.toString())
            (record as HashMap<Any?, Any?>).put("latitude", latitude.toString())
            //(record as HashMap<Any?, Any?>).put("date", Codificator.dateToString(Date()))
            //(record as HashMap<Any?, Any?>).put("indice", sRelativeIndex)
            Log.d("MapDevices", "Record coordinates in the hashmap")
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence.tcp",
                record as MutableMap<String, String>?
            )
            Log.d("MapDevices", "${record}")
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@Runnable
            }
            wifiP2pManager.addLocalService(
                wifiP2pChannel,
                serviceInfo,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        // Success!
                    }

                    override fun onFailure(code: Int) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                })
            mProgressDialog.dismiss()
            wifiP2pManager.setDnsSdResponseListeners(wifiP2pChannel,
                { s, s1, wifiP2pDevice -> },
                { s, map, wifiP2pDevice ->
                    val ingredient = Ingredient(
                        wifiP2pDevice.deviceName.split("°").toTypedArray()[0],
                        wifiP2pDevice.deviceAddress
                    )
                    ingredient.setLatitude(java.lang.Double.valueOf(map["latitude"]))
                    ingredient.setLongitude(java.lang.Double.valueOf(map["longitude"]))
                    ingredient.setIndice(map["indice"])
                    //ingredient.setDistance(calclateDistance(ingredient.getLongitude(),ingredient.getLatitude()) as Double)
                    distance = ingredient.getDistance()
                    ingredient.setDate(map["date"])
                    Log.d("MapDevices", "${wifiP2pDevice.deviceAddress}")
                    if (!isObjectInArray(wifiP2pDevice.deviceAddress)) {
                        ingredients.add(ingredient)
                        val arrayList: ArrayList<Ingredient>
                        arrayList = ListSort(ingredients)
                        //adapter.clear()
                        for (i in arrayList.indices) {
                            //adapter.add(arrayList[i])
                        }
                        //lv.setSelection(adapter.getCount() - 1)
                        //Log.d("Add device", java.lang.String.valueOf(adapter.getCount()))
                        Log.d("Add device", ingredient.name.toString() + " device")
                        /*when (ingredients.size) {
                            1 -> {
                                otherDevice.setVisibility(View.VISIBLE)
                                otherDevice.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            2 -> {
                                otherDevice1.setVisibility(View.VISIBLE)
                                otherDevice1.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice1.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            3 -> {
                                otherDevice2.setVisibility(View.VISIBLE)
                                otherDevice2.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice2.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            4 -> {
                                otherDevice3.setVisibility(View.VISIBLE)
                                otherDevice3.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice3.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            5 -> {
                                otherDevice4.setVisibility(View.VISIBLE)
                                otherDevice4.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice4.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            6 -> {
                                otherDevice5.setVisibility(View.VISIBLE)
                                otherDevice5.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice5.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            7 -> {
                                otherDevice6.setVisibility(View.VISIBLE)
                                otherDevice6.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice6.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            8 -> {
                                otherDevice7.setVisibility(View.VISIBLE)
                                otherDevice7.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice7.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            9 -> {
                                otherDevice8.setVisibility(View.VISIBLE)
                                otherDevice8.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice8.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                            10 -> {
                                otherDevice9.setVisibility(View.VISIBLE)
                                otherDevice9.setY(calculateY(java.lang.Double.valueOf(map["latitude"])))
                                otherDevice9.setX(calculateX(java.lang.Double.valueOf(map["longitude"])))
                            }
                        }*/
                    }
                })
            val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
            wifiP2pManager.addServiceRequest(
                wifiP2pChannel,
                serviceRequest,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        // Success!
                    }

                    override fun onFailure(code: Int) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                })
            wifiP2pManager.discoverServices(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // Success!
                }

                override fun onFailure(code: Int) {
                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                }
            })
            Log.d("MapDevices", "Start services")
        }, 10000)
        Toast.makeText(applicationContext, "MapDevices", Toast.LENGTH_SHORT).show()
    }


    fun onCickStart(view : View){
        //startRegistration()
    }

    fun onCickDiscover(view : View){
        //discoverService()
    }
    private fun isObjectInArray(deviceAddress: String): Boolean {
        val result = false
        for (ingredient in ingredients) {
            if (ingredient.mac.equals(deviceAddress)) {
                return true
            }
        }
        return result
    }
    private fun ListSort(lista: ArrayList<Ingredient>): ArrayList<Ingredient> {
        val copy: ArrayList<Ingredient>
        copy = lista
        var name: String? = ""
        var mac: String? = ""
        var distance = 0.0
        var indice: String? = ""
        var date: String? = ""
        for (i in copy.indices) for (j in i + 1 until copy.size) if (java.lang.Double.valueOf(copy[i].getIndice()) < java.lang.Double.valueOf(
                copy[j].getIndice()
            )
        ) {
            name = copy[j].getName()
            mac = copy[j].getMac()
            distance = copy[j].getDistance()
            indice = copy[j].getIndice()
            date = copy[j].getDate()

            //copy.get(j).equals(copy.get(i));
            copy[j].setName(copy[i].getName())
            copy[j].setMac(copy[i].getMac())
            copy[j].setDistance(copy[i].getDistance())
            copy[j].setIndice(copy[i].getIndice())
            copy[j].setDate(copy[i].getDate())

            //copy.get(i).equals(temp);
            copy[i].setName(name)
            copy[i].setMac(mac)
            copy[i].setDistance(distance)
            copy[i].setIndice(indice)
            copy[i].setDate(date)
        }
        return copy
    }

}