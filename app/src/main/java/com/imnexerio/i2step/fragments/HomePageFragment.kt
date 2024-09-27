package com.imnexerio.i2step.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.imnexerio.i2step.dialogs.ExitConfirmationDialog
import com.imnexerio.i2step.R
import com.imnexerio.i2step.SharedPrefManager

class HomePageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val phoneNumber = view.findViewById<TextView>(R.id.phone_number)
        val address = view.findViewById<TextView>(R.id.address)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        val welcomeText = view.findViewById<TextView>(R.id.welcome_text)
        val sharedPrefManager = SharedPrefManager(requireContext())
        val displayName = sharedPrefManager.getDisplayName()

        if (displayName != null) {
            welcomeText.text = "Welcome, $displayName"
        } else {
            welcomeText.text = "Welcome"
        }

        phoneNumber.setOnClickListener {
            val phone = phoneNumber.text.toString()
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            startActivity(intent)
        }

        address.setOnClickListener {
            val mapUrl = "https://maps.app.goo.gl/vzDmwkpDumuDa6fQ7?g_st=iw"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            mapIntent.setPackage("com.google.android.apps.maps")

            // Check if there's an app that can handle the Intent
            if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // If Google Maps app is not installed, open the URL in a web browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                startActivity(browserIntent)
            }
        }

        logoutButton.setOnClickListener {
            ExitConfirmationDialog.show(
                activity,
                "Logout",
                "Are you sure you want to logout?"
            ) { _, _ -> requireActivity().finish() }
        }

        return view
    }
}