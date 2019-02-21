package com.pauenrech.ejerciciocontentprovider.provider

class Product {

    var id: Int = 0
    var productName: String? = null
    var quantity: Int = 0

    constructor(identificador: Int, nombre: String, cantidad: Int) {
        id = identificador
        productName = nombre
        quantity = cantidad
    }

    constructor(productName: String, quantity: Int) {
        this.productName = productName
        this.quantity = quantity
    }

}