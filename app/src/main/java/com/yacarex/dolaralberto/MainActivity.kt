package com.yacarex.dolaralberto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    lateinit var option: Spinner
    lateinit var selectedCurrency: String
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val valueInput = findViewById<TextView>(R.id.input_value)
        val valueOfficialClean = findViewById<TextView>(R.id.official_clean_value)
        val valueSolidarity = findViewById<TextView>(R.id.solidarity_value)
        val valueClaimLater = findViewById<TextView>(R.id.claim_later_value)
        val totalValue = findViewById<TextView>(R.id.total_value)

        getDolarValue()

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().addTestDevice("7790A97E03F01714C1A757BAC702560D").build()
        mAdView.loadAd(adRequest)


        option = findViewById(R.id.currency)
        val options = arrayOf(
            getString(R.string.currency_usa),
            getString(R.string.currency_arg)
        )

        option.adapter = ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            options
        )


        option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCurrency = options[position]
                input_value.setText("")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                option.setSelection(0)
            }

        }



        valueInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val input =
                    if (valueInput.text.toString().isEmpty()) 0.0 else valueInput.text.toString()
                        .toDouble()
                val values: ArrayList<String> = fillValues(input, selectedCurrency)
                valueOfficialClean.text = values[0]
                valueSolidarity.text = values[1]
                valueClaimLater.text = values[2]
                totalValue.text = values[3]
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun getArsTotal(input: Double): Double {

        return getOfficial(input) + getSolidarity(input) + getClaimLater(input)
    }

    private fun getUsdTotal(input: Double): Double {
        val conversion = volley_value.text.toString().replace(",", ".", false).toDouble()
        return (input / 1.65) / conversion
    }

    private fun getOfficial(input: Double): Double {
        val conversion = volley_value.text.toString().replace(",", ".", false).toDouble()
        return input * conversion
    }

    private fun getOfficialInverted(input: Double): Double {
        val conversion = volley_value.text.toString().replace(",", ".", false).toDouble()
        return BigDecimal(
            input / 1.65 / conversion
        ).setScale(
            2,
            RoundingMode.CEILING
        ).toDouble()
    }

    private fun getSolidarity(input: Double): Double {
        return getOfficial(input) * 0.3
    }

    private fun getSolidarityInverted(input: Double): Double {
        return getSolidarity(getUsdTotal(input))
    }

    private fun getClaimLater(input: Double): Double {
        return getOfficial(input) * 0.35
    }

    private fun getClaimLaterInverted(input: Double): Double {
        return getOfficial(getUsdTotal(input)) * 0.35
    }

    private fun fillValues(number: Double, currency: String): ArrayList<String> {
        val results: ArrayList<String>
        if (number <= 0) {

            results = ArrayList(4)
            results.add(0, "AR$ " + getString(R.string.value_empty))
            results.add(1, "AR$ " + getString(R.string.value_empty))
            results.add(2, "AR$ " + getString(R.string.value_empty))
            results.add(
                3,
                (if (currency == getString(R.string.currency_usa)) "AR$ " else "U\$D"
                        ) + " " + getString(R.string.value_empty)
            )
            return results

        } else if ((currency == getString(R.string.currency_usa) && number > 200) ||
            currency == getString(R.string.currency_arg) && getUsdTotal(number) > 200
        ) {

            results = ArrayList(4)
            results.add(0, "AR$ " + getString(R.string.value_empty))
            results.add(1, "AR$ " + getString(R.string.value_empty))
            results.add(2, "AR$ " + getString(R.string.value_empty))
            results.add(3, getString(R.string.value_exceded))
            return results

        } else {

            results = ArrayList(4)

            when (currency) {
                getString(R.string.currency_usa) -> {
                    results.add(
                        0,
                        getString(R.string.currency_arg) + " " + BigDecimal(getOfficial(number)).setScale(
                            2,
                            RoundingMode.CEILING
                        )
                    )
                    results.add(
                        1,
                        getString(R.string.currency_arg) + " " + BigDecimal(getSolidarity(number)).setScale(
                            2,
                            RoundingMode.CEILING
                        )
                    )
                    results.add(
                        2,
                        getString(R.string.currency_arg) + " " + BigDecimal(getClaimLater(number)).setScale(
                            2,
                            RoundingMode.CEILING
                        )
                    )
                    results.add(
                        3,
                        getString(R.string.currency_arg) + " " + BigDecimal(getArsTotal(number)).setScale(
                            2,
                            RoundingMode.CEILING
                        )
                    )
                }

                getString(R.string.currency_arg) -> {
                    results.add(
                        0,
                        getString(R.string.currency_arg) + " " + BigDecimal(
                            getOfficialInverted(
                                number
                            )
                        ).setScale(2, RoundingMode.CEILING).toString()
                    )
                    results.add(
                        1,
                        getString(R.string.currency_arg) + " " + BigDecimal(
                            getSolidarityInverted(
                                number
                            )
                        ).setScale(2, RoundingMode.CEILING).toString()
                    )
                    results.add(
                        2,
                        getString(R.string.currency_arg) + " " + BigDecimal(
                            getClaimLaterInverted(
                                number
                            )
                        ).setScale(2, RoundingMode.CEILING).toString()
                    )
                    results.add(
                        3,
                        getString(R.string.currency_usa) + " " + BigDecimal(getUsdTotal(number)).setScale(
                            2,
                            RoundingMode.CEILING
                        ).toString()
                    )
                }

            }
            return results

        }
    }

    private fun getDolarValue() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.dolarsi.com/api/api.php?type=valoresprincipales"

// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Display the first 500 characters of the response string.
                val sType = object : TypeToken<List<DolarHomeModel>>() {}.type
                val otherList = Gson().fromJson<List<DolarHomeModel>>(response, sType)
                volley_value.text = otherList[0].casa?.venta
            },
            {
                volley_value.text = getString(R.string.value_empty)
            })

// Add the request to the RequestQueue.
        queue.add(stringRequest)

    }

}

