package com.yacarex.dolaralberto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val valueInput = findViewById<TextView>(R.id.input_value)
        val valueSolidarity = findViewById<TextView>(R.id.solidarity_value)
        val valueClaimLeater = findViewById<TextView>(R.id.claim_leater_value)
        val totalValue = findViewById<TextView>(R.id.total_value)

        valueInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val input = if(valueInput.text.toString().isEmpty()) 0.0 else valueInput.text.toString().toDouble()
                val values: ArrayList<String> = fillValues(input)
                valueSolidarity.text = values.get(0)
                valueClaimLeater.text = values.get(1)
                totalValue.text = values.get(2)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun getTotal(input: Double): BigDecimal? {
        var usdChange = input * 80
        var total = BigDecimal(usdChange + getSolidarity(input) + getClaimLeater(input)).setScale(
            2,
            RoundingMode.HALF_EVEN
        )

        return total
    }

    private fun getSolidarity(input: Double): Double {
        var usdChange = input * 80
        var solidarity = usdChange * 0.3
        return solidarity
    }

    private fun getClaimLeater(input: Double): Double {
        var usdChange = input * 80
        var claimLeater = usdChange * 0.35
        return claimLeater
    }

    private fun fillValues(number: Double): ArrayList<String> {
        val results: ArrayList<String>
        if ((number <= 0) || number == null) {

            results = ArrayList(3)
            results.add(0, "AR$ " + getString(R.string.value_empty))
            results.add(1, "AR$ " + getString(R.string.value_empty))
            results.add(2, "AR$ " + getString(R.string.value_empty))
            return results

        } else if (number > 200) {

            results = ArrayList(3)
            results.add(0, "AR$ " + getString(R.string.value_empty))
            results.add(1, "AR$ " + getString(R.string.value_empty))
            results.add(2, getString(R.string.value_exceded))
            return results

        } else {

            results = ArrayList(3)
            results.add(0, "AR$ ${getSolidarity(number)}")
            results.add(1, "AR$ ${getClaimLeater(number)}")
            results.add(2, "AR$ ${getTotal(number)}")
            return results

        }
    }
}

