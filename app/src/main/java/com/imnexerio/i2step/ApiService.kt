package com.imnexerio.i2step

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call

object RetrofitClient {
    private const val BASE_URL = "https://adversely-daring-fox.ngrok-free.app"

    private var authToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        authToken?.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(request.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

    fun setAuthToken(token: String) {
        authToken = token
    }
}

interface ApiService {
    @POST("/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/gettransactions")
    fun getTransactions(): Call<List<TransactionResponse>>

    @GET("/getorders")
    fun getOrders(): Call<List<OrderResponse>>

    @POST("initiatetransaction")
    fun initiateTransaction(@Body request: InitiateTransactionRequest): Call<InitiatedTransactionResponse>

    @POST("/modifytransaction")
    fun modifyTransaction(@Body request: ModifyTransactionRequest): Call<ModifyTransactionResponse>

    @POST("/modifyorder")
    fun modifyOrder(@Body request: ModifyOrderRequest): Call<ModifyOrderResponse>

    @POST("/modifytransaction_delete")
    fun deactivateTransaction(@Body request: DeactivateTransactionRequest): Call<DeactivateTransactionResponse>

    @POST("/modifyorder_delete")
    fun deactivateOrder(@Body request: DeactivateOrderRequest): Call<DeactivateOrderResponse>


    @POST("/initiateorder")
    fun initiateOrder(@Body request: InitiateOrderRequest): Call<InitiateOrderResponse>

}

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val access_token: String,val name:String, val username: String, val role: String)

data class TransactionResponse(
    val transaction_id: String,
    val amount: Float,
    val initiated_by: String,
    val initiated_date: String,
    val initiated_for: String,
    val payment_method: String?,
    val status: String,
    val verified_by: String?,
    val verified_date: String?,
    val total_amount: Float?,
    val comments: String?
)

data class InitiateTransactionRequest(
    val transaction_id: String,
    val payment_method: String,
    val amount: Float,
    val initiated_for: String,
    val comments: String?
)

data class InitiatedTransactionResponse(
    val message: String?,
    val transaction: String,
    val error: String?
)



data class ModifyTransactionRequest(
    val transaction_id: String,
    val status: String,
)

data class ModifyTransactionResponse(
    val message: String?,
    val transaction: String,
    val error: String?
)

data class DeactivateTransactionRequest(
    val transaction_id: String
)

data class DeactivateTransactionResponse(
    val message: String?,
    val transaction: String,
    val error: String?
)

data class DeactivateOrderRequest(
    val order_id: String
)

data class DeactivateOrderResponse(
    val message: String?,
    val transaction: String,
    val error: String?
)

data class InitiateOrderRequest(
    val transaction_id: String,
    val no_bags: Int,
    val rate: Float,
    val vehicle_no: String?,
    val initiated_for: String,
    val payment_method: String,
    val comments: String?
)

data class InitiateOrderResponse(
    val message: String?,
    val order_id: String?,
    val error: String?
)

data class OrderResponse(
    val order_id: String,
    val no_bags: Int,
    val rate: Float,
    val vehicle_no: String?,
    val initiated_for: String,
    val status: String,
    val initiated_date : String,
    val initiated_by: String,
    val verified_by: String?,
    val verified_date: String?,
    val comments: String?
)

data class ModifyOrderRequest(
    val order_id: String,
    val status: String,
)

data class ModifyOrderResponse(
    val message: String?,
    val transaction: String,
    val error: String?
)