package com.hrithikvish.apitask.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.hrithikvish.apitask.util.Constants
import com.hrithikvish.apitask.util.Validator
import com.hrithikvish.apitask.databinding.ActivityMainBinding
import com.hrithikvish.apitask.model.CustomerData
import com.hrithikvish.apitask.db.CustomerDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val validator = Validator()
    lateinit var database: CustomerDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = CustomerDatabase.getInstance(applicationContext)

        binding.loginButton.setOnClickListener {
            val phone: String = binding.phoneET.text.toString()
            if (validator.validatePhone(phone)) {

                lifecycleScope.launch {

                    val customerData = getCustomerDataFromApi(phone)

                    if (customerData != null) {
                        customerData.Data?.let {
                            database.customerDao().insert(it.get(0))
                        }
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        intent.putExtra("customerid", customerData.Data?.get(0)?.customerid)
                        startActivity(intent)
                        finish()
                    }

                }

            } else {
                Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private suspend fun getCustomerDataFromApi(phone: String): CustomerData? {
        return suspendCancellableCoroutine { continuation ->
            val queue = Volley.newRequestQueue(this)
            val url = Constants.BaseApiUrl + phone

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    if (response.optString("Message").equals("Success")) {
                        val data = Gson().fromJson(response.toString(), CustomerData::class.java)
                        continuation.resume(data)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Please Enter Valid Advisor Mobile Number",
                            Toast.LENGTH_SHORT
                        ).show()
                        continuation.resume(null)
                    }

                    if (response.optInt("Status") != 200) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error Code: " + response.optInt("Status"),
                            Toast.LENGTH_SHORT
                        ).show()
                        continuation.resume(null)
                    }
                },
                { error ->
                    Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_SHORT)
                        .show()
                    continuation.resume(null)
                }
            )

            queue.add(jsonObjectRequest)

            continuation.invokeOnCancellation {
                queue.cancelAll(null)
            }
        }
    }
}