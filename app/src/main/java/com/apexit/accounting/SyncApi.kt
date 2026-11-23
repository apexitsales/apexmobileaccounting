package com.apexit.accounting

import retrofit2.http.*

interface SyncApi {
    @POST("api/customers") suspend fun uploadCustomer(@Body customer: Customer)
    @POST("api/documents") suspend fun uploadDocument(@Body document: Document)
    @GET("api/customers") suspend fun getCustomers(): List<Customer>
}
