package com.imnexerio.i2step.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.imnexerio.i2step.InitiateOrderRequest
import com.imnexerio.i2step.InitiateOrderResponse
import com.imnexerio.i2step.OrderResponse
import com.imnexerio.i2step.R
import com.imnexerio.i2step.RetrofitClient
import com.imnexerio.i2step.SharedPrefManager
import com.imnexerio.i2step.dialogs.ErrorDialog
import com.imnexerio.i2step.dialogs.LoadingDialog
import com.imnexerio.i2step.dialogs.SuccessDialog
import com.imnexerio.i2step.fragmentHelper.OrderModel
import com.imnexerio.i2step.fragmentHelper.RecyclerOrderAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OrdersPageFragment : Fragment() {

    private var arrOrders: ArrayList<OrderModel> = ArrayList()
    private lateinit var adapter: RecyclerOrderAdapter
    private var btnOpenDialog: FloatingActionButton? = null

    private lateinit var sharedPrefManager: SharedPrefManager
    private var userRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders_page, container, false)
        sharedPrefManager = SharedPrefManager(requireContext())
        userRole = sharedPrefManager.getRole()
        adapter = RecyclerOrderAdapter(requireContext(), arrOrders,childFragmentManager,this)


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerOrders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        btnOpenDialog = view.findViewById(R.id.btnAddOrder)
        if (userRole == "A" || userRole == "M"){
            btnOpenDialog?.visibility = View.VISIBLE


            btnOpenDialog?.setOnClickListener{
                Log.i("OrdersPageFragment", "onCreateView: Button clicked")
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.add_orders_dialog)

                val initiateOrderadapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.paymentchannel_array,
                    android.R.layout.simple_spinner_item
                )

                val transactionID=dialog.findViewById<EditText>(R.id.transactionID)
                val nosBags = dialog.findViewById<EditText>(R.id.nosBags)
                val edtRate = dialog.findViewById<EditText>(R.id.edtRate)
                val vehicle_no = dialog.findViewById<EditText>(R.id.vehicle_no)
                val InitiatedFor = dialog.findViewById<EditText>(R.id.InitiatedFor)
                val paymentMethod = dialog.findViewById<Spinner>(R.id.edtorderPaymentMode)
                val comments = dialog.findViewById<EditText>(R.id.comments)
                val btnPlaceOrder = dialog.findViewById<Button>(R.id.btnPlaceOrder)


                paymentMethod.adapter = initiateOrderadapter

//
                btnPlaceOrder.setOnClickListener {
                    val newtransaction_id = transactionID.text.toString()
                    val newnosBagsStr = nosBags.text.toString()
                    val newRateStr = edtRate.text.toString()
                    val newVehicle_no = vehicle_no.text.toString()
                    val newInitiatedFor = InitiatedFor.text.toString()
                    val newPaymentMethod = paymentMethod.selectedItem.toString()
                    val newComments = comments.text.toString()
                    // Check if any field is empty
                    if (newnosBagsStr.isEmpty() || newRateStr.isEmpty() || newVehicle_no.isEmpty() || newInitiatedFor.isEmpty() || newComments.isEmpty() || newtransaction_id.isEmpty()) {
                        val addOrderDialog = ErrorDialog("All fields must be filled.")
                        addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                        return@setOnClickListener
                    }


                    // Parse the number of bags and rate as integers and floats respectively
                    val newnosBags = try {
                        newnosBagsStr.toInt()
                    } catch (e: NumberFormatException) {
                        val addOrderDialog = ErrorDialog("Invalid number of bags format.")
                        addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                        return@setOnClickListener
                    }

                    val newRate = try {
                        newRateStr.toFloat()
                    } catch (e: NumberFormatException) {
                        val addOrderDialog = ErrorDialog("Invalid rate format.")
                        addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                        return@setOnClickListener
                    }

                    val newContact = InitiateOrderRequest(
                        newtransaction_id,
                        newnosBags,
                        newRate,
                        newVehicle_no,
                        newInitiatedFor,
                        newPaymentMethod,
                        newComments
                    )
                    val loadingDialog = LoadingDialog(requireContext())
                    loadingDialog.show()
                    
                    val call = RetrofitClient.instance.initiateOrder(newContact)
                call.enqueue(object : Callback<InitiateOrderResponse> {
                    override fun onResponse(
                        call: Call<InitiateOrderResponse>,
                        response: Response<InitiateOrderResponse>
                    ) {
                        if (response.isSuccessful) {
                            loadingDialog.dismiss()
                            // Handle successful response
                            fetch_refresh_orders()

                            Log.i("OrdersPageFragment", "onResponse: ${response.body()}")
                            val addOrderDialog = SuccessDialog("Order Initiated Successfully")
                            addOrderDialog.show(requireActivity().supportFragmentManager, "SuccessDialog")
                        } else {
                            loadingDialog.dismiss()
                            // Handle different HTTP errors based on response code
                            val errorBody = response.errorBody()?.string()
                            val errorMessage = when (response.code()) {
                                400 -> "Bad Request: $errorBody"
                                401 -> "Unauthorized: Please check your credentials."
                                else -> "Error: $errorBody"
                            }
                            Log.i("OrdersPageFragment", "onResponse Error: $errorMessage")
                            val addOrderDialog = ErrorDialog(errorMessage)
                            addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                        }
                    }
                
                    override fun onFailure(call: Call<InitiateOrderResponse>, t: Throwable) {
                        loadingDialog.dismiss()
                        // Handle failure, e.g., network error, serialization error
                        Log.e("OrdersPageFragment", "onFailure: ${t.message}")
                        val addOrderDialog = ErrorDialog("Failure: There might be an issue with the network ")
                        addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                    }
                })

                    dialog.dismiss()

                }
                dialog.show()
            }
        }else{
            btnOpenDialog?.visibility = View.GONE
        }


        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()
        val call = RetrofitClient.instance.getOrders()
        call.enqueue(object : Callback<List<OrderResponse>> {
            override fun onResponse(call: Call<List<OrderResponse>>, response: Response<List<OrderResponse>>) {
                if (response.isSuccessful) {
                    // Clear existing data to avoid duplicates
                    arrOrders.clear()
                    loadingDialog.dismiss()
                    // Add new orders
                    response.body()?.forEach { order ->
                        arrOrders.add(OrderModel(
                            order_id = order.order_id,
                            no_bags = order.no_bags,
                            rate = order.rate,
                            vehicle_no = order.vehicle_no ?: "NA",
                            initiated_for = order.initiated_for,
                            status = order.status,
                            initiated_date = order.initiated_date,
                            initiated_by = order.initiated_by,
                            verified_by = order.verified_by ?: "NA",
                            verified_date = order.verified_date ?: "NA",
                            comments = order.comments ?: "NA"
                        ))
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    loadingDialog.dismiss()
                    val errorBody = response.errorBody()?.string()
                    Log.i("OrdersPageFragment", "Error Response: $errorBody")
                    // Handle specific error based on HTTP status code
                    val errorMessage = when (response.code()) {
                        401 -> {
                            // Unauthorized or token expired
                            Log.i("OrdersPageFragment", "Unauthorized: Token may have expired.")
                            "Unauthorized: Token may have expired. $errorBody"
                        }
                        else -> {
                            // General error handling
                            Log.i("OrdersPageFragment", "An error occurred: HTTP ${response.code()}")
                            "An error occurred: HTTP ${response.code()} $errorBody"
                        }
                    }
                    val addOrderDialog = ErrorDialog(errorMessage)
                    addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
                }
            }

            override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                // Log failure message
                loadingDialog.dismiss()
                Log.e("OrdersPageFragment", "Failure: ${t.message}")
                // Show ErrorDialog with the failure message
                val addOrderDialog = ErrorDialog("Failure: There might be an issue with the network or the response format.")
                addOrderDialog.show(requireActivity().supportFragmentManager, "ErrorDialog")
            }
        })
    }

    fun fetch_refresh_orders(){
        arrOrders.clear()
        fetchOrders()
    }
}