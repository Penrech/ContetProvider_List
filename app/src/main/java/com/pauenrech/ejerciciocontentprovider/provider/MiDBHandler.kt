package com.pauenrech.ejerciciocontentprovider.provider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.annotation.IntegerRes

class MiDBHandler(val context: Context,
                  nombreDB: String?,
                  factory: SQLiteDatabase.CursorFactory?,
                  version: Int):
    SQLiteOpenHelper(context, DATABASE_NAME,
        factory, DATABASE_VERSION) {

    //Content Resolver
    private val miContentResolver: ContentResolver

    init {
        miContentResolver = context.contentResolver
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "productDB.db"

        val TABLE_PRODUCTS = "products"
        val COLUMN_ID = "_id"
        val COLUMN_PRODUCTNAME = "productname"
        val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        /*(CREATE TABLE products(_id INTEGER PRIMARY KEY AUTOINCREMENT,
            productName TEXT, quantity INTEGER))
        */

        val CREATE_PRODUCTS_TABLE = ("CREATE TABLE $TABLE_PRODUCTS"
                + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PRODUCTNAME
                + " TEXT, "
                + COLUMN_QUANTITY
                + " INTEGER"
                + ")")

        val CREATE_PRODUCTS_TABLE2 = ("CREATE TABLE $TABLE_PRODUCTS($COLUMN_ID " +
                "INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_PRODUCTNAME TEXT, " +
                "$COLUMN_QUANTITY INTEGER)")

        p0?.execSQL(CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // DROP TABLE IF EXISTS products
        //ELiminamos la tabla
        p0?.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        //La volvemos a crear
        onCreate(p0)
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        return super.getWritableDatabase()
    }

    //Nuestros métodos
    fun addProduct(product: Product) {

        //val baseDatos = this.writableDatabase
        val registro = ContentValues()
        registro.put(COLUMN_PRODUCTNAME, product.productName)
        registro.put(COLUMN_QUANTITY, product.quantity)

        //baseDatos.insert(TABLE_PRODUCTS, null, registro)
        //baseDatos.close()

        miContentResolver.insert(MyContentProvider.CONTENT_URI, registro)
    }

    fun findProduct(productName: String): Product? {
        val projection = arrayOf(COLUMN_ID, COLUMN_PRODUCTNAME, COLUMN_QUANTITY)
        val selection = "productname = \"$productName\""
        val cursor = miContentResolver.query(MyContentProvider.CONTENT_URI,
            projection,
            selection,
            null, null)

        //val baseDatos = this.writableDatabase
        // SELECT * FROM products WHERE productName = "productName"
        //val peticion = "SELECT * FROM $TABLE_PRODUCTS " +
        //        "WHERE $COLUMN_PRODUCTNAME = \"$productName\""
        //val cursor = baseDatos.rawQuery(peticion, null)

        var product: Product? = null



        //Comprobamos si hay resultados
        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val id = Integer.parseInt(cursor.getString(0))
            val nombre = cursor.getString(1)
            val cantidad = Integer.parseInt(cursor.getString(2))

            product = Product(id, nombre, cantidad)
            cursor.close()
        }

        //baseDatos.close()
        return product
    }

    fun deleteProduct(productName: String): Boolean {
        //val baseDatos = this.writableDatabase
        //val peticion = "SELECT * FROM $TABLE_PRODUCTS " +
        //       "WHERE $COLUMN_PRODUCTNAME = \"$productName\""

        var resultado = false
        var selection = "productname = \"$productName\""
        var rowsDeleted = miContentResolver.delete(MyContentProvider.CONTENT_URI,
            selection, null)
        //val cursor = baseDatos.rawQuery(peticion, null)

        /*
        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val id = Integer.parseInt(cursor.getString(0))
            //Eliminamos el registro en la BDD
            baseDatos.delete(TABLE_PRODUCTS,
                "$COLUMN_ID = $id",
                null)
            cursor.close()
            resultado = true
        }
        */

        if (rowsDeleted > 0) { resultado = true }
        //baseDatos.close()
        return resultado
    }

}