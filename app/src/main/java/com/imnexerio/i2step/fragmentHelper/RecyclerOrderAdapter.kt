package com.imnexerio.i2step.fragmentHelper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.imnexerio.i2step.R
import com.imnexerio.i2step.SharedPrefManager
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isGone
import com.imnexerio.i2step.DeactivateOrderRequest
import com.imnexerio.i2step.DeactivateOrderResponse
import com.imnexerio.i2step.ModifyOrderRequest
import com.imnexerio.i2step.ModifyOrderResponse
import com.imnexerio.i2step.RetrofitClient
import com.imnexerio.i2step.dialogs.ErrorDialog
import com.imnexerio.i2step.dialogs.LoadingDialog
import com.imnexerio.i2step.dialogs.SuccessDialog
import com.imnexerio.i2step.fragments.OrdersPageFragment
import retrofit2.Call
import retrofit2.Callback

import retrofit2.Response

class RecyclerOrderAdapter (
    var context: Context,
    private var arrOrders: ArrayList<OrderModel>,
    var fragmentManager: FragmentManager,
    var orderPageFragment: OrdersPageFragment
    ) : RecyclerView.Adapter<RecyclerOrderAdapter.ViewHolder>() {


        private var lastPosition = -1

        private var sharedPrefManager= SharedPrefManager(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.order_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = arrOrders[position]
            Log.i("RecyclerOrderAdapter", "onBindViewHolder: $order")
            holder.no_of_bags.text = order.no_bags.toString()
            holder.rate.text = order.rate.toString()
            holder.vehicle_no.text = order.vehicle_no
            holder.initiated_for.text = order.initiated_for
            holder.initiated_date.text = order.initiated_date
            holder.initiated_by.text = order.initiated_by
            holder.order_id.text = order.order_id
            holder.verified_date.text = order.verified_date
            holder.verified_by.text = order.verified_by


            // Set the color of the amount text based on the status
            holder.order_id.setTextColor(
                if (order.status.equals("VERIFIED", ignoreCase = true)) {
                    Color.GREEN
                } else {
                    Color.RED
                }
            )
            setAnimation(holder.itemView, position)


//        setting on click method

            holder.llRow.setOnClickListener {
                val orderDialog = Dialog(context)
                orderDialog.setContentView(R.layout.order_verification_dialog)
//                val position = holder.adapterPosition
                val clickedOrder = arrOrders[position]

                val adapter = ArrayAdapter.createFromResource(
                    context,
                    R.array.orderStatus_array,
                    android.R.layout.simple_spinner_item
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                val order_id = orderDialog.findViewById<TextView>(R.id.orderOrderID)
                val initiated_for = orderDialog.findViewById<TextView>(R.id.orderInitiatedfor)
                val initiated_by = orderDialog.findViewById<TextView>(R.id.orderidInitiatedby)
                val no_bags = orderDialog.findViewById<TextView>(R.id.orderNoBags)
                val rate = orderDialog.findViewById<TextView>(R.id.orderidRate)
                val initiated_date = orderDialog.findViewById<TextView>(R.id.orderidInitiateddate)
                val vehicle_no = orderDialog.findViewById<TextView>(R.id.orderVehicleNo)
                val comments = orderDialog.findViewById<TextView>(R.id.ordercomments)
                val orderStatus = orderDialog.findViewById<Spinner>(R.id.orderOrderStatus)
                val statuss = context.resources.getStringArray(R.array.orderStatus_array)
                val current = statuss.indexOf(clickedOrder.status)

                order_id.isEnabled = false
                initiated_for.isEnabled = false
                initiated_by.isEnabled = false
                no_bags.isEnabled = false
                rate.isEnabled = false
                initiated_date.isEnabled = false
                vehicle_no.isEnabled = false
                comments.isEnabled = false
                orderStatus.adapter = adapter

                val btnAction = orderDialog.findViewById<Button>(R.id.orderidProceedbutton)

                Log.i("RecyclerOrderAdapter", "onBindViewHolder: $clickedOrder")
                order_id.setText(clickedOrder.order_id)
                initiated_for.setText(clickedOrder.initiated_for)
                initiated_by.setText(clickedOrder.initiated_by)
                no_bags.setText(clickedOrder.no_bags.toString())
                rate.setText(clickedOrder.rate.toString())
                initiated_date.setText(clickedOrder.initiated_date)
                vehicle_no.setText(clickedOrder.vehicle_no)
                comments.setText(clickedOrder.comments)

                orderStatus.setSelection(current)
                if (orderStatus.selectedItem.toString() == "INITIATED") {
                    btnAction.setOnClickListener {
                        val loadingDialog = LoadingDialog(context)
                        loadingDialog.show()
                        val newOrderID = order_id.text.toString()
                        val newOrderStatus = orderStatus.selectedItem.toString()

                        val modifyOrder_ = ModifyOrderRequest(
                            newOrderID,
                            newOrderStatus
                        )

                        Log.i("RecyclerOrderAdapter", "onBindViewHolder: $modifyOrder_")
                        val call = RetrofitClient.instance.modifyOrder(modifyOrder_)
                        call.enqueue(object : Callback<ModifyOrderResponse> {
                            override fun onResponse(
                                call: Call<ModifyOrderResponse>,
                                response: Response<ModifyOrderResponse>
                            ) {
                                if (response.isSuccessful) {

                                    orderPageFragment.fetch_refresh_orders()
                                    loadingDialog.dismiss()

                                    Log.i("RecyclerOrderAdapter", "onResponse: ${response.body()}")
                                    val dialog = SuccessDialog("Order Modified Successfully")
                                    dialog.show(fragmentManager, "SuccessDialog")
                                } else {
                                    loadingDialog.dismiss()
                                    val errorBody = response.errorBody()?.string()
                                    val errorMessage = when (response.code()) {
                                        400 -> "Bad Request: $errorBody"
                                        401 -> "Unauthorized: Please check your credentials."
                                        403 -> "Forbidden: You don't have permission to access this resource."
                                        else -> "Error: $errorBody"
                                    }
                                    Log.i("RecyclerOrderAdapter", "onResponse Error: $errorMessage")
                                    val dialog = ErrorDialog(errorMessage)
                                    dialog.show(fragmentManager, "ErrorDialog")
                                }
                            }

                            override fun onFailure(call: Call<ModifyOrderResponse>, t: Throwable) {
                                loadingDialog.dismiss()
                                Log.i("RecyclerOrderAdapter", "onFailure: ${t.message}")
                                val dialog =
                                    ErrorDialog("Failure: There might be an issue with the network ")
                                dialog.show(fragmentManager, "ErrorDialog")
                            }
                        })
                        orderDialog.dismiss()
                    }
                } else {
                    orderStatus.isEnabled = false
                    btnAction.isGone = true
                }
                orderDialog.show()
            }

                holder.llRow.setOnLongClickListener {
                    val userRole = sharedPrefManager.getRole()

                    if (userRole == "A") {
//                        val position = holder.adapterPosition
                        val builder = android.app.AlertDialog.Builder(
                            context
                        )
//                        val contact = arrOrders[position]
                        val orderID = order.order_id
                        val orderID_ = DeactivateOrderRequest(orderID)
                        builder.setTitle("Delete Order")
                        builder.setMessage("Are you sure you want to delete this Order?")
                        builder.setPositiveButton("Yes") { dialog, which ->
                            val loadingDialog = LoadingDialog(context)
                            loadingDialog.show()
                            val call = RetrofitClient.instance.deactivateOrder(orderID_)
                            call.enqueue(object : Callback<DeactivateOrderResponse> {
                                override fun onResponse(
                                    call: Call<DeactivateOrderResponse>,
                                    response: Response<DeactivateOrderResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        orderPageFragment.fetch_refresh_orders()
                                        loadingDialog.dismiss()
                                        // Handle successful response
                                        Log.i(
                                            "RecyclerTransactionAdapter",
                                            "onResponse: ${response.body()}"
                                        )
                                        val successDialog =
                                            SuccessDialog("Order Deleted Successfully")
                                        successDialog.show(fragmentManager, "SuccessDialog")
                                    } else {
                                        loadingDialog.dismiss()
                                        // Handle different HTTP errors based on response code
                                        val errorBody = response.errorBody()?.string()
                                        val errorMessage = when (response.code()) {
                                            400 -> "Bad Request: $errorBody"
                                            401 -> "Unauthorized: Please check your credentials."
                                            403 -> "Forbidden: You don't have permission to access this resource."
                                            else -> "Error: $errorBody"
                                        }
                                        Log.i(
                                            "RecyclerTransactionAdapter",
                                            "onResponse Error: $errorMessage"
                                        )
                                        val errorDialog = ErrorDialog(errorMessage)
                                        errorDialog.show(fragmentManager, "ErrorDialog")
                                    }
                                }

                                override fun onFailure(
                                    call: Call<DeactivateOrderResponse>,
                                    t: Throwable
                                ) {
                                    loadingDialog.dismiss()
                                    // Handle failure, e.g., network error, serialization error
                                    Log.i("RecyclerTransactionAdapter", "onFailure: ${t.message}")
                                    val errorDialog =
                                        ErrorDialog("Failure: There might be an issue with the network ")
                                    errorDialog.show(fragmentManager, "ErrorDialog")
                                }
                            })
                        }
                            .setNegativeButton("No") { dismissDialog, which -> dismissDialog.dismiss() }
                        builder.show()
                        true
                    } else {
                        Toast.makeText(context, "Action Not Permitted", Toast.LENGTH_SHORT).show()
                        Log.i("RecyclerTransactionAdapter", "onLongClick: User is not an admin")
                        false
                    }

                }

            }



        override fun getItemCount(): Int = arrOrders.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var no_of_bags: TextView = itemView.findViewById(R.id.id_noOfBags)
            var rate: TextView = itemView.findViewById(R.id.id_Rate)
            var vehicle_no: TextView = itemView.findViewById(R.id.id_VehicleNo)
            var initiated_for: TextView = itemView.findViewById(R.id.id_InitiatedFor)
            var initiated_date: TextView = itemView.findViewById(R.id.id_InitiatedDate)
            var initiated_by: TextView = itemView.findViewById(R.id.id_InitiatedBy)
            var verified_by: TextView = itemView.findViewById(R.id.id_VerifiedBy)
            var verified_date: TextView = itemView.findViewById(R.id.id_VerifiedDate)
            var order_id: TextView = itemView.findViewById(R.id.id_OrderId)


            var llRow: LinearLayout = itemView.findViewById(R.id.lll_Row)
        }

        private fun setAnimation(viewToAnimate: View, position: Int) {
            if (position > lastPosition) {
                val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
        }
    }

