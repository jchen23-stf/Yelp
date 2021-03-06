package edu.stanford.jchen23.yelpclone

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import edu.stanford.jchen23.yelpclone.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.net.NetworkInfo

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.IOException
import android.widget.Toast





const val RESTAURANT_ID = "RESTAURANT_ID"
private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "bircrYXzBWmK6_ZnZKvq4QIYd79sPs305DXLzDdIeKzX0ODXH_6TEUkUPBT-skk-FSlHpH1xa9c_8wZoTmXlrtlSCyx6uXGhV7jYPcRSo3o1pIbBFy5fo6WZP3WkYXYx"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter : RestaurantsAdapter
    private lateinit var restaurants : MutableList<YelpRestaurant>
    private lateinit var retrofit : Retrofit
    private lateinit var yelpService : YelpService

    private var searchQuery = "Avocado Toast"
    private var searchLocation = "New York"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!isNetworkAvailable == true) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Internet Connection Alert")
                .setMessage("Please Check Your Internet Connection")
                .setPositiveButton(
                    "Close"
                ) { dialogInterface, i -> finish() }.show()
        } else if (isNetworkAvailable == true) {
            Toast.makeText(
                this@MainActivity,
                "Welcome", Toast.LENGTH_LONG
            ).show()
        }

        restaurants = mutableListOf<YelpRestaurant>()
        retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()
        yelpService = retrofit.create(YelpService::class.java)

        updateSearch()
        binding.rvResturants.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantsAdapter(this, restaurants, object : RestaurantsAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                Log.i(TAG, "onItemClick $position")
            }
        })
        binding.rvResturants.adapter = adapter
        binding.rvResturants.addItemDecoration(
            DividerItemDecoration(
                binding.rvResturants.context,
                DividerItemDecoration.VERTICAL
            )
        )

    }

    val isNetworkAvailable: Boolean
        get() {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val capabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return true
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return true
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return true
                        }
                    }
                }
            }
            return false
        }


    private fun updateSearch() {
        yelpService.searchRestaurants("Bearer $API_KEY", searchQuery, searchLocation)
            .enqueue(object : Callback<YelpSearchResult> {
                override fun onResponse(
                    call: Call<YelpSearchResult>,
                    response: Response<YelpSearchResult>
                ) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                        return
                    }
                    restaurants.clear()
                    restaurants.addAll(body.restaurants)
                    Log.i(TAG, "$restaurants")
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFailure $t")
                }
            })
    }

}