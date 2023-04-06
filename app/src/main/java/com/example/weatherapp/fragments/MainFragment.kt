package com.example.weatherapp.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.R
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject

const val API_KEY = "c254aa7655f74892b46132829232803"

class MainFragment : Fragment() {

    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val fList = listOf(
        HoursFragment.newInstance(), DaysFragment.newInstance()
    )
    private val tList = listOf("Hours", "Days")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission()
        init()
        requestWeatherData("London")
    }

    private fun init() = with(binding) {
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab: TabLayout.Tab, pos: Int ->
            tab.text = tList[pos]
        }.attach()
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(cityName: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=$cityName&days=3&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
            },
            { error ->
                Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject = mainObject)
        parseCurrentData(mainObject = mainObject, weatherItem = list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")

        val nameCity = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                city = nameCity,
                time = day.getString("date"),
                condition = day.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("text"),
                currentTemp = "",
                maxTemp = day.getJSONObject("day").getString("maxtemp_c"),
                minTemp = day.getJSONObject("day").getString("mintemp_c"),
                imageUrl = day.getJSONObject("day")
                    .getJSONObject("condition").getString("icon"),
                hours = day.getJSONArray("hour").toString()
            )
            list.add(item)
        }

        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            city = mainObject.getJSONObject("location").getString("name"),
            time = mainObject.getJSONObject("current").getString("last_updated"),
            condition = mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
            maxTemp = weatherItem.maxTemp,
            minTemp = weatherItem.minTemp,
            imageUrl = mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            hours = weatherItem.hours
        )
        Log.d("MyLog", "Max Temp: ${item.maxTemp}")
        Log.d("MyLog", "Min Temp: ${item.minTemp}")
        Log.d("MyLog", "Hours: ${item.hours}")
    }
    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}