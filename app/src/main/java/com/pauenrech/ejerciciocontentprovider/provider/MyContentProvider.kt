package com.pauenrech.ejerciciocontentprovider.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import java.lang.IllegalArgumentException

class MyContentProvider : ContentProvider() {

    //Content URI
    companion object {
        val AUTHORITY = "com.pauenrech.ejerciciocontentprovider.MyContentProvider"
        val PRODUCTS_TABLE = "products"

        val CONTENT_URI = Uri.parse("content://$AUTHORITY/$PRODUCTS_TABLE")
    }

    //Uri Matcher
    private val PRODUCTS = 1
    private val PRODUCTS_ID = 2
    private val miUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        miUriMatcher.addURI(AUTHORITY, PRODUCTS_TABLE, PRODUCTS)
        miUriMatcher.addURI(AUTHORITY, "$PRODUCTS_TABLE/#", PRODUCTS_ID)
    }

    private var miDBHandler: MiDBHandler? = null

    override fun onCreate(): Boolean {
        miDBHandler = MiDBHandler(context, null, null, 1)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? {
        val uriType = miUriMatcher.match(uri)
        val sqlDB = miDBHandler?.writableDatabase
        val identificador: Long
        when(uriType) {
            PRODUCTS -> identificador = sqlDB!!.insert(MiDBHandler.TABLE_PRODUCTS,
                null,
                values)
            PRODUCTS_ID -> throw IllegalArgumentException("Uri incorrecta: $uri")
            else -> throw IllegalArgumentException("Uri incorrecta: $uri")
        }
        context.contentResolver.notifyChange(uri, null)
        return Uri.parse("$PRODUCTS_TABLE/$identificador")
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        //Parámetros
        /*
            uri -> Data Source
            projection -> Nombre de las columnas de la tabla
            selection -> WHERE ....
            selectionArgs -> Argumentos extra en la petición SQL
            sortOrder -> Orden en el que evolver los resultados
         */

        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = MiDBHandler.TABLE_PRODUCTS
        val uriType = miUriMatcher.match(uri)

        when(uriType) {
            PRODUCTS -> {
                //com.cice.formador.androidstorage.MiContentProvider/products
                //select * from products
            }
            PRODUCTS_ID -> {
                queryBuilder.
                    appendWhere(
                        "${MiDBHandler.COLUMN_ID} = ${uri.lastPathSegment}")
                //com.cice.formador.androidstorage.MiContentProvider/products/3
                //select * from products where _id = 3
            }
            else -> throw IllegalArgumentException("Uri incorrecto: $uri")
        }

        val cursor = queryBuilder
            .query(miDBHandler?.readableDatabase,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder)
        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val uriType = miUriMatcher.match(uri)
        val sqlDB = miDBHandler?.writableDatabase
        val rowsUpdated: Int
        when(uriType) {
            PRODUCTS -> {
                rowsUpdated = sqlDB!!.update(MiDBHandler.TABLE_PRODUCTS,
                    values,
                    selection,
                    selectionArgs)
            }
            PRODUCTS_ID -> {
                //com.cice.formador.androidstorage.MiContentProvider/products/3
                val id = uri.lastPathSegment
                //Comprobamos si hay algún filtro
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB!!.update(MiDBHandler.TABLE_PRODUCTS,
                        values,
                        "${MiDBHandler.COLUMN_ID} = $id",
                        null)
                } else {
                    rowsUpdated = sqlDB!!.update(MiDBHandler.TABLE_PRODUCTS,
                        values,
                        "${MiDBHandler.COLUMN_ID} = $id and $selection",
                        selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Uri incorrecto: $uri")
        }
        context.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val uriType = miUriMatcher.match(uri)
        val sqlDB = miDBHandler?.writableDatabase
        val rowsDeleted: Int

        when(uriType) {
            PRODUCTS -> {
                rowsDeleted = sqlDB!!.delete(MiDBHandler.TABLE_PRODUCTS,
                    selection, selectionArgs)
            }
            PRODUCTS_ID -> {
                //com.cice.formador.androidstorage.MiContentProvider/products/3
                val id = uri?.lastPathSegment
                //Comprobamos si hay algún filtro
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB!!.delete(MiDBHandler.TABLE_PRODUCTS,
                        "${MiDBHandler.COLUMN_ID} = $id",
                        null)
                } else {
                    rowsDeleted = sqlDB!!.delete(MiDBHandler.TABLE_PRODUCTS,
                        "${MiDBHandler.COLUMN_ID} = $id and $selection",
                        selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Uri incorrecto: $uri")
        }
        context.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    //No es necesario
    override fun getType(uri: Uri): String? {
        TODO(
            "Implement this to handle requests for the MIME type of the data" +
                    "at the given URI"
        )
    }
}
