package com.example.stackify.network.cart_data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.stackify.ShoppingCartProto.ShoppingCart
import java.io.InputStream
import java.io.OutputStream

object ShoppingCartSerializer : Serializer<ShoppingCart> {
    override val defaultValue: ShoppingCart
        get() = ShoppingCart.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ShoppingCart {
        try {
            return ShoppingCart.parseFrom(input)
        } catch (e: Exception) {
            e.printStackTrace()
            throw CorruptionException("Cannot read proto. ", e)
        }
    }

    override suspend fun writeTo(t: ShoppingCart, output: OutputStream) {
        t.writeTo(output)
    }
}