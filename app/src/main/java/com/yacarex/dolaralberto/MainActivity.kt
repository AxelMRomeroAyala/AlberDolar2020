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
        val valueClaimLeater = findViewById<TextView>(R.id.solidarity_value)
        val totalValue = findViewById<TextView>(R.id.total_value)

        valueInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                valueSolidarity.text = fillValues(valueInput.text.toString().toDouble()).get(0)
                valueClaimLeater.text = fillValues(valueInput.text.toString().toDouble()).get(1)
                totalValue.text = fillValues(valueInput.text.toString().toDouble()).get(2)
            }

            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
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
        if (number <= 0) {

            results = ArrayList(3)
            results.set(0, "AR$ ${R.string.value_empty}")
            results.set(1, "AR$ ${R.string.value_empty}")
            results.set(2, "AR$ ${R.string.value_empty}")

        } else if (number > 200) {

            results = ArrayList(3)
            results.set(0, "AR$ ${R.string.value_empty}")
            results.set(1, "AR$ ${R.string.value_empty}")
            results.set(2, R.string.value_exceded.toString())

        } else {

            results = ArrayList(3)
            results.set(0, "AR$ ${getSolidarity(number)}")
            results.set(1, "AR$ ${getClaimLeater(number)}")
            results.set(2, "AR$ ${getTotal(number)}")

        }

        return results
    }
}

