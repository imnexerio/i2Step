package com.imnexerio.i2step.fragmentHelper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.imnexerio.i2step.DeactivateTransactionRequest
import com.imnexerio.i2step.DeactivateTransactionResponse
import com.imnexerio.i2step.ModifyTransactionRequest
import com.imnexerio.i2step.ModifyTransactionResponse
import com.imnexerio.i2step.R
import com.imnexerio.i2step.RetrofitClient
import com.imnexerio.i2step.SharedPrefManager
import com.imnexerio.i2step.dialogs.ErrorDialog
import com.imnexerio.i2step.dialogs.LoadingDialog
import com.imnexerio.i2step.dialogs.SuccessDialog
import com.imnexerio.i2step.fragments.TransactionPageFragment
import retrofit2.Call
import retrofit2.Callback

import retrofit2.Response


class RecyclerTransactionAdapter(
    var context: Context,
    private var arrContacts: ArrayList<TransactionModel>,
    var fragmentManager: FragmentManager,
    var transactionPageFragment: TransactionPageFragment
) : RecyclerView.Adapter<RecyclerTransactionAdapter.ViewHolder>() {
    // ...


    private var lastPosition = -1

    private var sharedPrefManager= SharedPrefManager(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transection_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = arrContacts[position]
        Log.i("RecyclerTransactionAdapter", "onBindViewHolder: $contact")
        holder.idAmount.text = contact.amount.toString()
        holder.idtotalAmount.text = contact.totalAmount.toString()
        holder.idTransactionId.text = contact.transaction_id
        holder.idInitiatedFor.text = contact.initiatedFor
        holder.idInitiatedBy.text = contact.initiatedById
        holder.idInitiatedDate.text = contact.initiatedDate
        holder.idVerifiedDate.text = contact.verifiedDate ?: "N/A"
        holder.idPaymentMode.text = contact.paymentMethod
        holder.idVerifiedBy.text = contact.verifiedById ?: "N/A"

        // Set the color of the amount text based on the status
        holder.idAmount.setTextColor(
            if (contact.status.equals("VERIFIED", ignoreCase = true)) {
                Color.GREEN
            } else {
                Color.RED
            }
        )
        setAnimation(holder.itemView, position)


//        setting on click method

        holder.llRow.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.transaction_verification_dialog)
//            val clickposition = holder.adapterPosition
//            val contact = arrContacts[clickposition]

            val adapter = ArrayAdapter.createFromResource(
                context,
                R.array.paymentStatus_array,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            val Initiatedfor = dialog.findViewById<EditText>(R.id.transidInitiatedfor)
            val InitiatedBy = dialog.findViewById<EditText>(R.id.transidInitiatedby)
            val Amount = dialog.findViewById<EditText>(R.id.transidAmount)
            val TransactionID = dialog.findViewById<EditText>(R.id.transTransactionID)
            val InitiatedDate = dialog.findViewById<EditText>(R.id.transidInitiateddate)
            val PaymentMode = dialog.findViewById<EditText>(R.id.transidPaymentmethod)
            val transidComments = dialog.findViewById<EditText>(R.id.edt_Comments)
            val paymentStatus = dialog.findViewById<Spinner>(R.id.transidPaymentStatus)
            val statuses = context.resources.getStringArray(R.array.paymentStatus_array)
            val currentStatusIndex = statuses.indexOf(contact.status)

            paymentStatus.adapter = adapter
            Initiatedfor.isEnabled = false
            InitiatedBy.isEnabled = false
            Amount.isEnabled = false
            InitiatedDate.isEnabled = false
            PaymentMode.isEnabled = false
            TransactionID.isEnabled = false
            transidComments.isEnabled = false
            val btnAction = dialog.findViewById<Button>(R.id.transidProceedbutton)


            Log.i("RecyclerTransactionAdapter", "onBindViewHolder: $contact")
            Initiatedfor.setText(contact.initiatedFor)
            InitiatedBy.setText(contact.initiatedById)
            Amount.setText(contact.amount.toString())
            InitiatedDate.setText(contact.initiatedDate)
            PaymentMode.setText(contact.paymentMethod)
            TransactionID.setText(contact.transaction_id)
            transidComments.setText(contact.comments)


//            paymentStatus.setText(contact.status)
            paymentStatus.setSelection(currentStatusIndex)
            if(paymentStatus.selectedItem.toString()=="INITIATED"){


                btnAction.setOnClickListener{
                    val loadingDialog = LoadingDialog(context)
                    loadingDialog.show()
                    val newTransactionId = TransactionID.text.toString()
                    val newPaymentStatus = paymentStatus.selectedItem.toString()

                    val modifyTransaction_ = ModifyTransactionRequest(
                        newTransactionId,
                        newPaymentStatus
                    )

                    Log.i("RecyclerTransactionAdapter", "onBindViewHolder: $modifyTransaction_")
                    val call = RetrofitClient.instance.modifyTransaction(modifyTransaction_)
                    call.enqueue(object : Callback<ModifyTransactionResponse> {
                        override fun onResponse(
                            call: Call<ModifyTransactionResponse>,
                            response: Response<ModifyTransactionResponse>
                        ) {
                            if (response.isSuccessful) {

                                transactionPageFragment.fetch_refresh_transactions()
                                loadingDialog.dismiss()
//                                transactionPageFragment.clearContacts()
//                                transactionPageFragment.refreshTransactions()
                                // Handle successful response
                                Log.i("RecyclerTransactionAdapter", "onResponse: ${response.body()}")
                                val successdialog = SuccessDialog("Transaction Modified Successfully")
                                successdialog.show(fragmentManager, "SuccessDialog")
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
                                Log.i("RecyclerTransactionAdapter", "onResponse Error: $errorMessage")
                                val errordialog = ErrorDialog(errorMessage)
                                errordialog.show(fragmentManager, "ErrorDialog")
                            }
                        }

                        override fun onFailure(call: Call<ModifyTransactionResponse>, t: Throwable) {
                            loadingDialog.dismiss()
                            // Handle failure, e.g., network error, serialization error
                            Log.i("RecyclerTransactionAdapter", "onFailure: ${t.message}")
                            val errordialog = ErrorDialog("Failure: There might be an issue with the network ")
                            errordialog.show(fragmentManager, "ErrorDialog")
                        }
                    })
                    dialog.dismiss()
                }

            }else {
                paymentStatus.isEnabled = false
                btnAction.isGone = true
            }

                dialog.show()
            }
        holder.llRow.setOnLongClickListener{
            val userRole = sharedPrefManager.getRole()

            if (userRole=="A"){
                val longClickedPosition = holder.adapterPosition
                val builder = android.app.AlertDialog.Builder(
                    context
                )
                val longClickedPos = arrContacts[longClickedPosition]
                val transactionID=longClickedPos.transaction_id
                val transactionID_= DeactivateTransactionRequest(transactionID)
                builder.setTitle("Delete Transaction")
                builder.setMessage("Are you sure you want to delete this transaction?")
                builder.setPositiveButton("Yes") { dialog, which ->
                    val loadingDialog = LoadingDialog(context)
                    loadingDialog.show()
                    val call = RetrofitClient.instance.deactivateTransaction(transactionID_)
                    call.enqueue(object : Callback<DeactivateTransactionResponse> {
                        override fun onResponse(
                            call: Call<DeactivateTransactionResponse>,
                            response: Response<DeactivateTransactionResponse>
                        ) {
                            if (response.isSuccessful) {
                                transactionPageFragment.fetch_refresh_transactions()
                                loadingDialog.dismiss()
//                                transactionPageFragment.clearContacts()
//                                transactionPageFragment.refreshTransactions()
                                // Handle successful response
                                Log.i("RecyclerTransactionAdapter", "onResponse: ${response.body()}")
                                val successdialog = SuccessDialog("Transaction Deactivated Successfully")
                                successdialog.show(fragmentManager, "SuccessDialog")
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
                                Log.i("RecyclerTransactionAdapter", "onResponse Error: $errorMessage")
                                val errordialog = ErrorDialog(errorMessage)
                                errordialog.show(fragmentManager, "ErrorDialog")
                            }
                        }

                        override fun onFailure(call: Call<DeactivateTransactionResponse>, t: Throwable) {
                            loadingDialog.dismiss()
                            // Handle failure, e.g., network error, serialization error
                            Log.i("RecyclerTransactionAdapter", "onFailure: ${t.message}")
                            val errordialog = ErrorDialog("Failure: There might be an issue with the network")
                            errordialog.show(fragmentManager, "ErrorDialog")
                        }
                    })
                }
                    .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
                builder.show()
                true
            }
            else{
                Toast.makeText(context, "Action Not Permitted", Toast.LENGTH_SHORT).show()
                Log.i("RecyclerTransactionAdapter", "onLongClick: User is not an admin")
                false
            }

        }


    }


    override fun getItemCount(): Int = arrContacts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var idAmount: TextView = itemView.findViewById(R.id.idAmount)
        var idtotalAmount: TextView = itemView.findViewById(R.id.idtotalAmount)
        var idTransactionId: TextView = itemView.findViewById(R.id.idTransactionId)
        var idInitiatedFor: TextView = itemView.findViewById(R.id.idInitiatedFor)
        var idInitiatedBy: TextView = itemView.findViewById(R.id.idInitiatedBy)
        var idInitiatedDate: TextView = itemView.findViewById(R.id.idInitiatedDate)
        var idVerifiedDate: TextView = itemView.findViewById(R.id.idVerifiedDate)
        var idPaymentMode: TextView = itemView.findViewById(R.id.idPaymentMode)
        var idVerifiedBy: TextView = itemView.findViewById(R.id.idVerifiedBy)

        var llRow: LinearLayout = itemView.findViewById(R.id.llRow)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }
}
