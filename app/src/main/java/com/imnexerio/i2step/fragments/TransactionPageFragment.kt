package com.imnexerio.i2step.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.imnexerio.i2step.InitiateTransactionRequest
import com.imnexerio.i2step.R
import com.imnexerio.i2step.RetrofitClient
import com.imnexerio.i2step.SharedPrefManager
import com.imnexerio.i2step.TransactionResponse
import com.imnexerio.i2step.fragmentHelper.TransactionModel
import com.imnexerio.i2step.fragmentHelper.RecyclerTransactionAdapter
import retrofit2.Call
import com.imnexerio.i2step.InitiatedTransactionResponse
import com.imnexerio.i2step.dialogs.ErrorDialog
import com.imnexerio.i2step.dialogs.LoadingDialog
import com.imnexerio.i2step.dialogs.SuccessDialog
import retrofit2.Callback
import retrofit2.Response


class TransactionPageFragment : Fragment() {
    private var arrContact: ArrayList<TransactionModel> = ArrayList()
    private lateinit var adapter: RecyclerTransactionAdapter
    private var btnOpenDialog: FloatingActionButton? = null

    private lateinit var sharedPrefManager: SharedPrefManager
    private var userRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions_page, container, false)
        sharedPrefManager = SharedPrefManager(requireContext())
        userRole = sharedPrefManager.getRole()
//        adapter = RecyclerTransactionAdapter(requireContext(), arrContact,childFragmentManager)
        adapter = RecyclerTransactionAdapter(requireContext(), arrContact, childFragmentManager, this)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerContact)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        btnOpenDialog = view.findViewById(R.id.btnOpenDialog)
        if (userRole == "A" || userRole == "M"){
            btnOpenDialog?.visibility = View.VISIBLE


            btnOpenDialog?.setOnClickListener{
                Log.i("TransactionPageFragment", "onCreateView: Button clicked")
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.add_transection_dialog)

                val adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.paymentchannel_array,
                    android.R.layout.simple_spinner_item
                )
//
                val edtTransactionID = dialog.findViewById<EditText>(R.id.edtTransactionID)
                val edtAmount = dialog.findViewById<EditText>(R.id.edtNewAmount)
                val edtInitiatedFor = dialog.findViewById<EditText>(R.id.edtNewInitiatedFor)
                val edtPaymentMode = dialog.findViewById<Spinner>(R.id.edtNewPaymentMode)
                val edtComments = dialog.findViewById<EditText>(R.id.edtComments)
                val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)


                edtPaymentMode.adapter = adapter
                btnAdd.setOnClickListener {
                    val newTransactionID = edtTransactionID.text.toString()
                    val newAmountStr = edtAmount.text.toString()
                    val newInitiatedFor = edtInitiatedFor.text.toString()
                    val newCommets = edtComments.text.toString()
                    val newPaymentMode = edtPaymentMode.selectedItem.toString()

                    // Check if any field is empty
                    if (newTransactionID.isEmpty() || newAmountStr.isEmpty() || newInitiatedFor.isEmpty() || newCommets.isEmpty()) {
                        val errorDialog = ErrorDialog("All fields must be filled.")
                        errorDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                        return@setOnClickListener
                    }


                    // Parse the amount as a float
                    val newAmount = try {
                        newAmountStr.toFloat()
                    } catch (e: NumberFormatException) {
                        val errorDialog = ErrorDialog("Invalid amount format.")
                        errorDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                        return@setOnClickListener
                    }

                    val newContact = InitiateTransactionRequest(
                        newTransactionID,
                        newPaymentMode,
                        newAmount,
                        newInitiatedFor,
                        newCommets
                    )
                    val loadingDialog = LoadingDialog(requireContext())
                    loadingDialog.show()

                    val call = RetrofitClient.instance.initiateTransaction(newContact)
                    call.enqueue(object : Callback<InitiatedTransactionResponse> {
                        override fun onResponse(call: Call<InitiatedTransactionResponse>, response: Response<InitiatedTransactionResponse>) {
                            if (response.isSuccessful) {
//                                arrContact.clear()
//                                fetchTransactions()
                                loadingDialog.dismiss()
                                fetch_refresh_transactions()
                                Log.i("TransactionPageFragment", "Transaction Successful: ${response.body()}")
                                val successDialog = SuccessDialog("Transaction Successful")
                                successDialog.show(requireActivity().supportFragmentManager, "SuccessDialog")
                                // Handle successful response
                                Log.i("TransactionPageFragment", "Transaction Successful: ${response.body()}")
                            }
                            else {
                                loadingDialog.dismiss()
                                // Handle different HTTP errors based on response code
                                val errorBody = response.errorBody()?.string()
                                val errorMessage = when (response.code()) {
                                    400 -> "Bad Request: $errorBody"
                                    401 -> "Unauthorized: Token may have expired or is invalid. $errorBody"
                                    403 -> "Forbidden: Access denied. $errorBody"
                                    404 -> "Not Found: The requested resource was not found. $errorBody"
                                    500 -> "Internal Server Error: Please try again later. $errorBody"
                                    else -> "Error: HTTP ${response.code()} $errorBody"
                                }
                                val errorDialog = ErrorDialog(errorMessage)
                                errorDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                            }
                        }

                        override fun onFailure(call: Call<InitiatedTransactionResponse>, t: Throwable) {
                            loadingDialog.dismiss()
                            Log.i("TransactionPageFragment", "onFailure: ${t.message}")
                            // Distinguish between different types of failures
                            val errorDialog = ErrorDialog("Failure: There might be an issue with the network or the response format.")
                            errorDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")

                        }
                    })

                    dialog.dismiss()

        }
            dialog.show()
            }
        }else{
            btnOpenDialog?.visibility = View.GONE
        }


        fetchTransactions()

        return view
    }

    private fun fetchTransactions() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()
        val call = RetrofitClient.instance.getTransactions()
        call.enqueue(object : Callback<List<TransactionResponse>> {
            override fun onResponse(call: Call<List<TransactionResponse>>, response: Response<List<TransactionResponse>>) {
                if (response.isSuccessful) {
                    // Clear existing data to avoid duplicates
                    arrContact.clear()
                    loadingDialog.dismiss()
                    // Add new transactions
                    response.body()?.forEach { transaction ->
                        arrContact.add(TransactionModel(
                            transaction_id = transaction.transaction_id,
                            paymentMethod = transaction.payment_method,
                            amount = transaction.amount,
                            status = transaction.status,
                            initiatedDate = transaction.initiated_date,
                            verifiedDate = transaction.verified_date ?: "NA",
                            initiatedById = transaction.initiated_by,
                            verifiedById = transaction.verified_by ?: "NA",
                            initiatedFor = transaction.initiated_for,
                            totalAmount = transaction.total_amount ?: 0.0f,
                            comments = transaction.comments ?: "NA"


                        ))
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    // Log error response
                    loadingDialog.dismiss()
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        401 -> {
                            // Unauthorized or token expired
                            "Unauthorized: Token may have expired. $errorBody"
                        }
                        else -> {
                            // General error handling
                            "An error occurred: HTTP ${response.code()} $errorBody"
                        }
                    }
                    val errorDialog = ErrorDialog(errorMessage)
                    errorDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                }
            }


            override fun onFailure(call: Call<List<TransactionResponse>>, t: Throwable) {
                loadingDialog.dismiss()
                // Log failure message
                Log.i("TransactionPageFragment", "Failure: ${t.message}")
                // Handle different types of failures (e.g., network issue, serialization error)
                val errorDialog = ErrorDialog("Failure: There might be an issue with the network or the response format.")
                errorDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
}
        })
    }

    fun fetch_refresh_transactions() {
        arrContact.clear()
        fetchTransactions()
    }
}