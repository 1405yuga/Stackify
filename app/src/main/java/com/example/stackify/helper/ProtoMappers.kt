package com.example.stackify.helper

import com.example.stackify.ShoppingCartProto
import com.example.stackify.entity.cart.CartItem

fun ShoppingCartProto.CartItem.toEntity(): CartItem {
    return CartItem(
        itemId = this.itemId,
        itemName = this.itemName,
        stock = this.stock,
        isPurchased = this.isPurchased
    )
}

fun List<ShoppingCartProto.CartItem>.toEntity(): List<CartItem> {
    return this.map { protoCartItem -> protoCartItem.toEntity() }
}