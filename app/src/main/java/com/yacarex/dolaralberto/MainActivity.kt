package com.yacarex.dolaralberto

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_official_layout.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.ceil
import android.view.inputmethod.InputMethodManager as InputMethodManager1

class MainActivity : AppCompatActivity() {

    lateinit var option: Spinner
    lateinit var selectedCurrency: String
    lateinit var resultCurrency: String
    private lateinit var mAdView: AdView

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edit_button.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val editConversionDialog = inflater.inflate(R.layout.edit_official_layout, null)
            val insertOfficial = editConversionDialog.findViewById<EditText>(R.id.insert_official)

            with(builder) {

                setTitle("Que valor de conversión usarás?")

                setPositiveButton("OK") { _, _ ->
                    volley_value.text = insertOfficial.text.toString()
                }

                setNegativeButton("Cancelar") { _, _ ->

                }
                setNeutralButton("Restaurar") { _, _ ->
                    getDolarValue()
                }
                setView(editConversionDialog)
                show()

            }
        }

        getDolarValue()

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest =
            AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // View control on mode selection in RadioGroup

        mode_select.check(R.id.mode_direct)
        mode_select.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {

                R.id.mode_direct -> {

                    mode_text.text = getString(R.string.mode_text_direct_buy)
                    iva_field.visibility = View.GONE
                    iva.visibility = View.GONE
                    warn_cannot_buy.visibility = View.GONE
                    total_value.visibility = View.VISIBLE
                    no_you_cant.visibility = View.GONE

                    when (selectedCurrency) {

                        "U\$D" -> text_total.text = getString(R.string.total_direct_usd_ars)
                        "AR$" -> text_total.text = getString(R.string.total_direct_ars_usd)

                    }

                    input_value.text = input_value.text

                }

                R.id.mode_credit -> {

                    mode_text.text = getString(R.string.mode_text_credit_card)
                    iva_field.visibility = View.GONE
                    iva.visibility = View.GONE
                    text_total.text = getString(R.string.total_credit)
                    warn_cannot_buy.visibility = View.VISIBLE
                    total_value.visibility = View.VISIBLE
                    no_you_cant.visibility = View.GONE

                    input_value.text = input_value.text

                }

                R.id.mode_digital_services -> {

                    mode_text.text = getString(R.string.mode_text_digital_services)
                    iva_field.visibility = View.VISIBLE
                    iva.visibility = View.VISIBLE
                    text_total.text = getString(R.string.total_digital)
                    warn_cannot_buy.visibility = View.VISIBLE
                    total_value.visibility = View.VISIBLE
                    no_you_cant.visibility = View.GONE

                    input_value.text = input_value.text

                }
            }
        }

        // Input Method Spinner

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

                if (mode_direct.isChecked) when (selectedCurrency) {

                    "U\$D" -> text_total.text = getString(R.string.total_direct_usd_ars)
                    "AR$" -> text_total.text = getString(R.string.total_direct_ars_usd)

                }

                input_value.setText("")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                option.setSelection(0)
            }

        }

        // When input EditText changes

        input_value.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val input =
                    when {
                        input_value.text.toString().isEmpty() -> {
                            0.0
                        }
                        input_value.text.toString().startsWith(".") -> {
                            val decimalValue = "0" + input_value.text.toString()
                            decimalValue.toDouble()
                        }
                        else -> {
                            input_value.text.toString()
                                .toDouble()
                        }
                    }

                val values: ArrayList<String> = fillValues(input)
                official_clean_value.text = values[0]
                solidarity_value.text = values[1]
                claim_later_value.text = values[2]
                total_value.text = values[3]
                iva_value.text = values[4]

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        //When Official input changes

    }

    fun hideSoftKeyboard(view: View) {
        currentFocus?.let {
            val inputMethodManager =
                ContextCompat.getSystemService(this, InputMethodManager1::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun getCurrency(): String {
        return if (mode_direct.isChecked && selectedCurrency == "AR$") {
                getString(R.string.currency_usa)
            } else{
            getString(R.string.currency_arg)
            }
    }

    private fun giveItRounded(toRound: Double): String {
        return BigDecimal(toRound).setScale(2, RoundingMode.CEILING).toString()
    }

    private fun getArsDirect(input: Double): Double {
        return getDirectOfficial(input) + getSolidarity(input) + getClaimLater(input)
    }


    private fun getArsCredit(input: Double): Double {
        return if (selectedCurrency == "AR$") {
            input + getSolidarity(input) + getClaimLater(input)
        } else {
            getDirectOfficial(input) + getSolidarity(input) + getClaimLater(input)
        }
    }

    private fun getArsDigital(input: Double): Double {
        return if (selectedCurrency == "AR$") {
            input + getSolidarity(input) + getClaimLater(input) + getIva(input)
        } else {
            getDirectOfficial(input) + getSolidarity(input) + getClaimLater(input) + getIva(input)
        }
    }

    private fun getUsdTotal(input: Double): Double {
        return if (mode_select.checkedRadioButtonId == R.id.mode_digital_services) {
            BigDecimal(input / (1 + 0.35 + 0.08 + 0.21) / getConversion())
                .setScale(2, RoundingMode.CEILING).toDouble()
        } else {
            BigDecimal(input / 1.65 / getConversion())
                .setScale(2, RoundingMode.CEILING).toDouble()
        }
    }

    private fun getConversion(): Double {
        return volley_value.text.toString().replace(",", ".", false).toDouble()
    }

    private fun getDirectOfficial(input: Double): Double {
        return if (selectedCurrency == "U\$D") input * getConversion() else input
    }


    private fun getDirectOfficialInverted(input: Double): Double {
        return getUsdTotal(input) * getConversion()
    }

    private fun getSolidarity(input: Double): Double {
        return if (mode_digital_services.isChecked) getDirectOfficial(input) * 0.08 else getDirectOfficial(
            input
        ) * 0.3
    }

    private fun getSolidarityInverted(input: Double): Double {
        return getSolidarity(getUsdTotal(input) * getConversion())
    }

    private fun getClaimLater(input: Double): Double {
        return getDirectOfficial(input) * 0.35
    }

    private fun getClaimLaterInverted(input: Double): Double {
        return getUsdTotal(input) * getConversion() * 0.35
    }

    private fun getIva(input: Double): Double {
        return getDirectOfficial(input) * 0.21
    }

    private fun getIvaInverted(input: Double): Double {
        return getIva(getUsdTotal(input))
    }

    private fun setLimitReduction(usdValue: Double) {
        when (usdValue) {
            0.0 -> {
                val message = getString(R.string.text_blessing)
                warn_cannot_buy.text = message
            }
            in 0.0..200.0 -> {

                val message = getString(R.string.text_warning) + (200 - usdValue)
                warn_cannot_buy.text = message

            }
            else -> {
                val restoDecimal = BigDecimal(usdValue % 200).setScale(
                    2,
                    RoundingMode.CEILING
                ).toDouble()

                val df = DecimalFormat("###.#")
                val meses = df.format(ceil(usdValue / 200))
                val message = "Podrás comprar U\$D${
                    BigDecimal(200 - restoDecimal).setScale(
                        2,
                        RoundingMode.CEILING
                    )
                } recién en ${if (restoDecimal == 0.00) meses + 1 else meses} meses"
                warn_cannot_buy.text = message

            }
        }
    }

    private fun fillEmpty(number: Double): ArrayList<String>{

        val results: ArrayList<String> = ArrayList(4)

        results.add(0, "AR$" + getString(R.string.value_empty))
        results.add(1, "AR$" + getString(R.string.value_empty))
        results.add(2, "AR$" + getString(R.string.value_empty))
        results.add(3, getCurrency() + getString(R.string.value_empty))
        results.add(4, "AR$" + getString(R.string.value_empty))
        setLimitReduction(0.0)

        return results
    }

    private fun fillDirect(number: Double):ArrayList<String> {

        val results: ArrayList<String> = ArrayList(4)

        if ((selectedCurrency == "U\$D" && number > 200) ||
            (selectedCurrency == "AR$" && getUsdTotal(number) > 200)
        ) {

            results.add(0, "AR$" + getString(R.string.value_empty))
            results.add(1, "AR$" + getString(R.string.value_empty))
            results.add(2, "AR$" + getString(R.string.value_empty))
            results.add(3, getString(R.string.value_exceded))
            results.add(4, "AR$" + getString(R.string.value_empty))
            total_value.visibility = View.GONE
            no_you_cant.visibility = View.VISIBLE

        } else {

            when (selectedCurrency) {
                getString(R.string.currency_usa) -> {
                    results.add(0, "AR$" + giveItRounded(getDirectOfficial(number)))
                    results.add(1, "AR$" + giveItRounded(getSolidarity(number)))
                    results.add(2, "AR$" + giveItRounded(getClaimLater(number)))
                    results.add(3, getCurrency() + giveItRounded(getArsDirect(number)))
                    results.add(4, "AR$" + giveItRounded(getIva(number)))
                    total_value.visibility = View.VISIBLE
                    no_you_cant.visibility = View.GONE
                }

                getString(R.string.currency_arg) -> {
                    results.add(
                        0,
                        "AR$" + giveItRounded(getDirectOfficialInverted(number))
                    )
                    results.add(1, "AR$" + giveItRounded(getSolidarityInverted(number)))
                    results.add(2, "AR$" + giveItRounded(getClaimLaterInverted(number)))
                    results.add(3, getCurrency() + giveItRounded(getUsdTotal(number)))
                    results.add(4, "AR$" + giveItRounded(getIvaInverted(number)))
                    total_value.visibility = View.VISIBLE
                    no_you_cant.visibility = View.GONE
                }
            }
        }

        return results
    }

    private fun fillCreditCard (number: Double): ArrayList<String>{

        val results: ArrayList<String> = ArrayList(4)

        results.add(0, "AR$" + giveItRounded(getDirectOfficial(number)))
        results.add(1, "AR$" + giveItRounded(getSolidarity(number)))
        results.add(2, "AR$" + giveItRounded(getClaimLater(number)))
        results.add(3, "AR$" + giveItRounded(getArsCredit(number)))
        results.add(4, "AR$" + giveItRounded(getIva(number)))
        setLimitReduction(
            if (selectedCurrency == "AR$") BigDecimal(number / getConversion()).setScale(
                2,
                RoundingMode.CEILING
            ).toDouble() else number
        )

        return results
    }

    private fun fillDigitalServices(number: Double): ArrayList<String>{

        val results: ArrayList<String> = ArrayList(4)

        results.add(0, "AR$" + giveItRounded(getDirectOfficial(number)))
        results.add(1, "AR$" + giveItRounded(getSolidarity(number)))
        results.add(2, "AR$" + giveItRounded(getClaimLater(number)))
        results.add(3, "AR$" + giveItRounded(getArsDigital(number)))
        results.add(4, "AR$" + giveItRounded(getIva(number)))
        setLimitReduction(
            if (selectedCurrency == "AR$") BigDecimal(number / getConversion()).setScale(
                2,
                RoundingMode.CEILING
            ).toDouble() else number
        )

        return results
    }

    private fun fillValues(number: Double): ArrayList<String> {

        var results: ArrayList<String> = ArrayList()

        when {
            number <= 0 -> results = fillEmpty(number)
            mode_direct.isChecked -> results = fillDirect(number)
            mode_credit.isChecked -> results = fillCreditCard(number)
            mode_digital_services.isChecked -> results = fillDigitalServices(number)
        }

        return results
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

