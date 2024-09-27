package com.imnexerio.i2step.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.imnexerio.i2step.MainActivity
import com.imnexerio.i2step.LoginRequest
import com.imnexerio.i2step.LoginResponse
import com.imnexerio.i2step.R
import com.imnexerio.i2step.RetrofitClient
import com.imnexerio.i2step.SharedPrefManager
import com.imnexerio.i2step.dialogs.ErrorDialog
import com.imnexerio.i2step.dialogs.LoadingDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class loginFragment : AppCompatActivity() {
    private var usernameField: EditText? = null
    private var passwordField: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        usernameField = findViewById(R.id.loginId)
        passwordField = findViewById(R.id.loginPassword)

        val btnLogin = findViewById<Button>(R.id.loginButton)

        btnLogin.setOnClickListener {
            val username = usernameField?.text.toString()
            val password = passwordField?.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                val errorDialog = ErrorDialog("Username or password cannot be empty")
                errorDialog.show(supportFragmentManager, "errorDialog")
            } else {
                login(username, password)
            }
        }
    }

    private fun login(username: String, password: String) {
        val request = LoginRequest(username, password)
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        val call = RetrofitClient.instance.loginUser(request)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loadingDialog.dismiss()
                if (response.isSuccessful) {
                    response.body()?.let {
                        RetrofitClient.setAuthToken(it.access_token)
                        val sharedPrefManager = SharedPrefManager(this@loginFragment)
                        sharedPrefManager.saveRole(it.role)
                        sharedPrefManager.saveDisplayName(it.name)
                        Log.i("loginFragment", "Access token: ${it.access_token}")
                        Log.i("loginFragment", "Username: ${it.username}")
                        Log.i("loginFragment", "Role: ${it.role}")
                        Log.i("loginFragment", "Name: ${it.name}")
                    }
                    val iHome = Intent(this@loginFragment, MainActivity::class.java)
                    startActivity(iHome)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        401 -> "Unauthorized access. Please check your credentials."
                        403 -> "Forbidden access. You don't have permission to access this resource."
                        else -> "An error occurred: $errorBody"
                    }
                    val errorDialog = ErrorDialog(errorMessage)
                    errorDialog.show(supportFragmentManager, "errorDialog")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.i("loginFragment", "onFailure: ${t.message}")
                val errorDialog = ErrorDialog("Failure: There might be an issue with the network")
                errorDialog.show(supportFragmentManager, "errorDialog")
            }
        })
    }
}