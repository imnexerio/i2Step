package com.imnexerio.i2step

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imnexerio.i2step.fragments.TransactionPageFragment
import com.imnexerio.i2step.fragments.HomePageFragment
import com.imnexerio.i2step.fragments.OrdersPageFragment


class MainActivity : AppCompatActivity() {
    var bnView: BottomNavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bnView = findViewById(R.id.btView)

        bnView?.setOnItemSelectedListener { menuItem ->
            val id = menuItem.itemId
            if (id == R.id.nav_orders_info) {
                loadFragment(OrdersPageFragment(), false)
            } else if (id == R.id.nav_transaction_info) {
                loadFragment(TransactionPageFragment(), false)
            } else {
                loadFragment(HomePageFragment(), false)
            }
            true
        }

        bnView?.setSelectedItemId(R.id.nav_about_business)
    }

    fun loadFragment(fragment: Fragment?, flag: Boolean) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        if (flag) {
            ft.add(R.id.container, fragment!!)
        } else {
            ft.replace(R.id.container, fragment!!)
        }

        ft.commit()
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("Exit")
            .setMessage("Are you sure?")
            .setIcon(R.drawable.baseline_exit_to_app_24)
            .setPositiveButton("No") { dialog, which ->
                Toast.makeText(
                    this@MainActivity,
                    "Positive",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Yes") { dialog, which ->
                super.onBackPressed()
            }
            .show()
    }
}
