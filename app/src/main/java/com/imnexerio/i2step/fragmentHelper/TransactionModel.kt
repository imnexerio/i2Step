package com.imnexerio.i2step.fragmentHelper

data class TransactionModel(
    val transaction_id: String,
    val paymentMethod: String?,
    val amount: Float,
    val status: String,
    val initiatedDate: String,
    val verifiedDate: String?,
    val initiatedById: String?,
    val verifiedById: String?,
    val initiatedFor: String?,
    val totalAmount: Float?,
    val comments: String?

)


