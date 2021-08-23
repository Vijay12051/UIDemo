package com.assignment.uidemo

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(), View.OnClickListener {
    val CALLPHONEREQUESTCODE = 89
    var mActivity: Activity? = null
    private var lock: Button? = null
    private  var disable:Button? = null
    private  var enable:Button? = null
    val RESULT_ENABLE = 11
    private var devicePolicyManager: DevicePolicyManager? = null
    private var activityManager: ActivityManager? = null
    private var compName: ComponentName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Wifi Switch
        val wifiSwitch = findViewById<SwitchCompat>(R.id.wifi_switch)
        wifiSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Wi-Fi:ON" else {
                "Wi-Fi:OFF"
            }
            Toast.makeText(
                this@MainActivity, message,
                Toast.LENGTH_SHORT
            ).show()
        }

        //Mobile data Switch
        val mobileDataSwitch = findViewById<SwitchCompat>(R.id.mobile_data_switch)
        mobileDataSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Mobile data:ON" else "Mobile data:OFF"
            Toast.makeText(
                this@MainActivity, message,
                Toast.LENGTH_SHORT
            ).show()
        }

        //Emergency call button
        val emergencyCall = findViewById<AppCompatButton>(R.id.emergency_call)
        emergencyCall.setOnClickListener {
            emergencyCall()
        }

        //Device lock
        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        compName = ComponentName(this, MyAdmin::class.java)

        lock = findViewById<View>(R.id.lock_device) as Button
        enable = findViewById<View>(R.id.enable) as Button
        disable = findViewById<View>(R.id.disable) as Button
        lock!!.setOnClickListener(this)
        enable!!.setOnClickListener(this)
        disable!!.setOnClickListener(this)
    }

    private fun emergencyCall() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            mActivity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    CALLPHONEREQUESTCODE
                )
            }
        } else {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:+91 1234567890")
            startActivity(callIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALLPHONEREQUESTCODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                emergencyCall()
            } else {
                Toast.makeText(mActivity, "Request Rejected.", Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isActive: Boolean = devicePolicyManager!!.isAdminActive(compName!!)
        disable?.visibility = if (isActive) View.VISIBLE else View.GONE
        enable?.visibility = if (isActive) View.GONE else View.VISIBLE
    }

    override fun onClick(view: View) {
        if (view === lock) {
            val active: Boolean = devicePolicyManager!!.isAdminActive(compName!!)
            if (active) {
                devicePolicyManager!!.lockNow()
            } else {
                Toast.makeText(
                    this,
                    "You need to enable the Admin Device Features",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (view === enable) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why we need this permission"
            )
            startActivityForResult(intent, RESULT_ENABLE)
        } else if (view === disable) {
            devicePolicyManager!!.removeActiveAdmin(compName!!)
            disable?.visibility = View.GONE
            enable?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_ENABLE -> if (resultCode == RESULT_OK) {
                Toast.makeText(
                    this@MainActivity,
                    "You have enabled the Admin Device features",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Problem to enable the Admin Device features",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}