package com.imnexerio.i2step.fragmentHelper

data class OrderModel (
    val order_id: String,
    val no_bags: Int,
    val rate: Float,
    val vehicle_no: String,
    val initiated_for: String,
    val status: String,
    val initiated_date: String,
    val verified_date: String,
    val initiated_by: String,
    val verified_by: String,
    val comments: String
)