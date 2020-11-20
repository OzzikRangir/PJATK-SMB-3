package com.ozzikrangir.productlist.data.provider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.ozzikrangir.productlist.data.model.Product
import com.ozzikrangir.productlist.data.model.ProductsList

class DBHandler(
    context: Context?,
    factory: CursorFactory?
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    private val myCR: ContentResolver = context!!.contentResolver

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PRODUCTS_TABLE =
            "CREATE TABLE $TABLE_PRODUCT($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COLUMN_PRODUCT_NAME TEXT NOT NULL UNIQUE,$COLUMN_PRICE FLOAT)"
        val CREATE_LIST_TABLE =
            "CREATE TABLE $TABLE_LIST($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COLUMN_LIST_NAME,$COLUMN_DATE DATETIME DEFAULT (datetime('now','localtime')))"
        val CREATE_PRODUCT_LIST_TABLE =
            """CREATE TABLE $TABLE_PRODUCT_LIST(
                $COLUMN_PRODUCT_ID INTEGER NOT NULL,
                $COLUMN_LIST_ID INTEGER NOT NULL,
                $COLUMN_QUANTITY INTEGER NOT NULL,
                $COLUMN_MARK BOOLEAN,
                FOREIGN KEY($COLUMN_LIST_ID) REFERENCES $TABLE_LIST($COLUMN_ID),
                FOREIGN KEY($COLUMN_PRODUCT_ID) REFERENCES $TABLE_PRODUCT($COLUMN_ID),
                PRIMARY KEY($COLUMN_LIST_ID,$COLUMN_PRODUCT_ID)
)""".trimMargin()
        db.execSQL(CREATE_PRODUCTS_TABLE)
        db.execSQL(CREATE_LIST_TABLE)
        db.execSQL(CREATE_PRODUCT_LIST_TABLE)
        db.execSQL("PRAGMA foreign_keys = ON")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCT_LIST")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LIST")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCT")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }
//    fun addProduct(product: Product) {
//        val values = ContentValues()
//        values.put(COLUMN_PRODUCT_NAME, product.name)
//        values.put(COLUMN_PRICE, product.price)
//
//        myCR.insert(ProductsListContentProvider.URI_PRODUCTS, values)
//    }
//
//    fun findProduct(name: String): Product? {
//        val projection = arrayOf(COLUMN_ID, COLUMN_LIST_NAME, COLUMN_PRICE)
//        val selection = "name = \"$name\""
//        val cursor = myCR.query(
//            ProductsListContentProvider.URI_PRODUCTS,
//            projection, selection, null, null
//        )
//
//        var product: Product? = null
//
//        if (cursor!!.moveToFirst()) {
//            val id = cursor.getString(0).toInt()
//            val productName = cursor.getString(1)
//            val price = cursor.getString(2).toFloat()
//            product = Product(productName, price, id, null, null, null)
//            cursor.close()
//        } else {
//        }
//        return product
//    }
//
//    fun deleteProduct(name: String?): Boolean {
//
//        var result = false
//
//        val selection = "name = \"$name\""
//
//        val rowsDeleted = myCR.delete(
//            ProductsListContentProvider.URI_PRODUCTS,
//            selection, null
//        )
//
//        if (rowsDeleted > 0)
//            result = true
//
//        return result
//    }
//
//
//    fun addList(productsList: ProductsList) {
//        val values = ContentValues()
//        values.put(COLUMN_LIST_NAME, productsList.name)
//        values.put(COLUMN_DATE, productsList.date.toString())
//        val db = this.writableDatabase
//        db.insert(TABLE_LIST, null, values)
//        db.close()
//    }
//
//    fun deleteList(listname: String?): Boolean {
//        var result = false
//        val query =
//            String.format(
//                "Select * FROM %s WHERE %s =  \"%s\"",
//                TABLE_LIST,
//                COLUMN_LIST_NAME,
//                listname
//            )
//        val db = this.writableDatabase
//        val cursor = db.rawQuery(query, null)
//        val productsList = ProductsList(null, null, null)
//        if (cursor.moveToFirst()) {
//            productsList.id = cursor.getString(0).toInt()
//            db.delete(TABLE_LIST, "$COLUMN_ID = ?", arrayOf(productsList.id.toString()))
//            cursor.close()
//            result = true
//        }
//        db.close()
//        return result
//    }

    companion object {
        private const val DATABASE_VERSION = 7
        private const val DATABASE_NAME = "productDB.db"
        const val TABLE_LIST = "list"
        const val TABLE_PRODUCT = "product"
        const val TABLE_PRODUCT_LIST = "product_list"
        const val COLUMN_ID = "_id"
        const val COLUMN_PRODUCT_NAME = "product_name"
        const val COLUMN_LIST_NAME = "list_name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_PRODUCT_ID = "_product_id"
        const val COLUMN_LIST_ID = "_list_id"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_DATE = "date"
        const val COLUMN_MARK = "mark"
    }
}