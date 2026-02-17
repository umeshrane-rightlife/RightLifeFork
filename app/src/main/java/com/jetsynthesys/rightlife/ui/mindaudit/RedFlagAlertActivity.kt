package com.jetsynthesys.rightlife.ui.mindaudit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityRedFlagAlertBinding
import java.util.Locale


class RedFlagAlertActivity : BaseActivity() {
    private lateinit var binding: ActivityRedFlagAlertBinding
    private lateinit var adapterState: HelpLineAdapter
    private lateinit var adapterCountry: HelpLineAdapter
    private val stateList: ArrayList<Organization> = ArrayList()
    private val countryList: ArrayList<Organization> = ArrayList()
    private var selectedCountryPosition = 0
    private var selectedStatePosition = 0

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private lateinit var helplinesResponse: HelplinesResponse

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            val granted = permissions.entries.all { it.value }

            if (granted) {
                checkIfLocationEnabled()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedFlagAlertBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.tvState.paintFlags = binding.tvState.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvCountry.paintFlags = binding.tvCountry.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.iconBack.setOnClickListener {
            finishThisActivity(RESULT_CANCELED)
        }

        onBackPressedDispatcher.addCallback {
            finishThisActivity(RESULT_CANCELED)
        }

        binding.btnImFine.setOnClickListener {
            finishThisActivity(RESULT_OK)
        }

        val jsonString = loadJSONFromAssets()
        val gson = Gson()
        helplinesResponse = gson.fromJson(jsonString, HelplinesResponse::class.java)
        helplinesResponse.countries[selectedCountryPosition].organizations?.let {
            countryList.addAll(
                it
            )
        }
        helplinesResponse.countries[selectedCountryPosition].states?.get(selectedStatePosition)?.organizations?.let {
            stateList.addAll(
                it
            )
        }
        if (stateList.isEmpty()) {
            binding.rlStateHelpLine.isVisible = false
            binding.rvStateHelpLine.isVisible = false
        } else {
            binding.rlStateHelpLine.isVisible = true
            binding.rvStateHelpLine.isVisible = true
        }
        adapterCountry = HelpLineAdapter(this, countryList)
        adapterState = HelpLineAdapter(this, stateList, true)
        binding.rvCountryHelpLine.layoutManager = LinearLayoutManager(this)
        binding.rvCountryHelpLine.adapter = adapterCountry
        binding.rvStateHelpLine.layoutManager = LinearLayoutManager(this)
        binding.rvStateHelpLine.adapter = adapterState

        val locale = Locale.getDefault()
        val countryName = locale.getDisplayCountry(Locale.ENGLISH)
        val countries = helplinesResponse.countries.map { it.name }

        countries.indexOf(countryName)

        checkLocationPermission()

        binding.tvState.setOnClickListener {
            val states =
                helplinesResponse.countries[selectedCountryPosition].states?.map { it.name }
            if (states != null) {
                CountryListDialog(states, "States") { selected, position ->
                    stateList.clear()
                    selectedStatePosition = position
                    binding.tvState.text = selected
                    helplinesResponse.countries[selectedCountryPosition].states?.get(position)?.organizations
                        ?.let { it1 ->
                            stateList.addAll(
                                it1
                            )
                        }
                    adapterState.notifyDataSetChanged()
                    val visibility = if (stateList.isEmpty()) View.GONE else View.VISIBLE
                    binding.rlStateHelpLine.visibility = visibility
                    binding.rvStateHelpLine.visibility = visibility
                }.show(supportFragmentManager, "stateDialog")
            }
        }

        binding.tvCountry.setOnClickListener {
            val countries = helplinesResponse.countries.map { it.name }
            CountryListDialog(countries, "Countries") { selected, position ->
                countryList.clear()
                selectedCountryPosition = position
                binding.tvCountry.text = selected
                helplinesResponse.countries[position].organizations?.let { it1 ->
                    countryList.addAll(
                        it1
                    )
                }
                adapterCountry.notifyDataSetChanged()

                stateList.clear()
                selectedStatePosition = 0
                val states = helplinesResponse.countries[selectedCountryPosition].states

                if (states.isNullOrEmpty()) {
                    binding.rlStateHelpLine.isVisible = false
                    binding.rvStateHelpLine.isVisible = false
                } else {
                    binding.rlStateHelpLine.isVisible = true
                    binding.rvStateHelpLine.isVisible = true

                    val selectedState = states.getOrNull(selectedStatePosition)
                    binding.tvState.text = selectedState?.name
                    selectedState?.organizations?.let { stateList.addAll(it) }
                }
                adapterState.notifyDataSetChanged()

            }.show(supportFragmentManager, "countryDialog")
        }

    }


    private fun loadJSONFromAssets(): String? {
        return try {
            assets.open("helplines.json").bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun finishThisActivity(result: Int) {
        val returnIntent = Intent()
        setResult(result, returnIntent)
        finish()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkIfLocationEnabled()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkIfLocationEnabled() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (enabled) {
            getLastLocation()
        } else {
            Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLastLocation() {

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {
                getAddressFromLatLng(location.latitude, location.longitude)
            } else {
                requestFreshLocation()
            }
        }
    }

    private fun requestFreshLocation() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).setMaxUpdates(1).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {

                    val location = result.lastLocation

                    if (location != null) {
                        getAddressFromLatLng(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(
                            this@RedFlagAlertActivity,
                            "Unable to get location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double) {

        val geocoder = Geocoder(this, Locale.ENGLISH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    val state = addresses[0].adminArea
                    val country = addresses[0].countryName

                    updateHelpLineData(country,state)
                }
            }

        } else {

            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val state = addresses[0].adminArea
                    val country = addresses[0].countryName

                    updateHelpLineData(country,state)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateHelpLineData(countryName: String?, stateName: String?) {

        // 🔹 Find country position
        val position = helplinesResponse.countries.indexOfFirst {
            it.name.equals(countryName, ignoreCase = true)
        }

        if (position == -1) return

        // ------------------ COUNTRY ------------------
        countryList.clear()
        selectedCountryPosition = position
        binding.tvCountry.text = countryName

        helplinesResponse.countries[position].organizations?.let {
            countryList.addAll(it)
        }

        adapterCountry.notifyDataSetChanged()

        // ------------------ STATE ------------------
        stateList.clear()

        val states = helplinesResponse.countries[selectedCountryPosition].states

        if (states.isNullOrEmpty()) {
            binding.rlStateHelpLine.isVisible = false
            binding.rvStateHelpLine.isVisible = false
            adapterState.notifyDataSetChanged()
            return
        }

        binding.rlStateHelpLine.isVisible = true
        binding.rvStateHelpLine.isVisible = true

        // 🔹 Find state position
        val statePosition = states.indexOfFirst {
            it.name.equals(stateName, ignoreCase = true)
        }

        selectedStatePosition = if (statePosition != -1) statePosition else 0

        val selectedState = states.getOrNull(selectedStatePosition)

        binding.tvState.text = selectedState?.name

        selectedState?.organizations?.let {
            stateList.addAll(it)
        }

        adapterState.notifyDataSetChanged()
    }


}