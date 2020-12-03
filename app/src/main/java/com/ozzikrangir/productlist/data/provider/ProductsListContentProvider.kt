package com.ozzikrangir.productlist.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils


class ProductsListContentProvider : ContentProvider() {


    private var myDB: DBHandler? = null
    private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

    companion object {
        const val AUTHORITY = "com.ozzikrangir.productlist"
        private const val URI_NAME_LISTS = "lists"
        private const val URI_NAME_PRODUCTS = "products"

        val URI_PRODUCTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    URI_NAME_PRODUCTS
        )
        val URI_LISTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    URI_NAME_LISTS
        )
        val URI_PRODUCT_LISTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    URI_NAME_LISTS
        )

        private const val URI_ALL_PRODUCTS_CODE = 10
        private const val URI_PRODUCT_CODE = 11
        private const val URI_ALL_LISTS_CODE = 20
        private const val URI_LIST_CODE = 21
        private const val URI_PRODUCT_LIST_CODE = 30
    }

    init {
        sURIMatcher.addURI(AUTHORITY, URI_NAME_LISTS, URI_ALL_LISTS_CODE)
        sURIMatcher.addURI(AUTHORITY, "$URI_NAME_LISTS/#", URI_LIST_CODE)
        sURIMatcher.addURI(AUTHORITY, URI_NAME_PRODUCTS, URI_ALL_PRODUCTS_CODE)
        sURIMatcher.addURI(AUTHORITY, "$URI_NAME_PRODUCTS/#", URI_PRODUCT_CODE)
        sURIMatcher.addURI(AUTHORITY, "$URI_NAME_LISTS/#/#", URI_PRODUCT_LIST_CODE)
    }

    override fun onCreate(): Boolean {
        myDB = DBHandler(context, null)
        return false
    }


    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()


        val uriType = sURIMatcher.match(uri)
        var projectionLocal = projection
        when (uriType) {
            URI_PRODUCT_CODE -> {
                queryBuilder.tables = DBHandler.TABLE_PRODUCT
                queryBuilder.appendWhere(
                    DBHandler.COLUMN_ID + "="
                            + uri.lastPathSegment
                )
            }
            URI_ALL_PRODUCTS_CODE -> {
                queryBuilder.tables = DBHandler.TABLE_PRODUCT
            }
            URI_LIST_CODE -> {
                queryBuilder.tables =
                    "${DBHandler.TABLE_LIST},  ${DBHandler.TABLE_PRODUCT_LIST}, ${DBHandler.TABLE_PRODUCT}"
                queryBuilder.appendWhere(
                    """${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_LIST_ID} = ${uri.lastPathSegment}
                        AND ${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_PRODUCT_ID} = ${DBHandler.TABLE_PRODUCT}.${DBHandler.COLUMN_ID}
                        AND ${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_LIST_ID} = ${DBHandler.TABLE_LIST}.${DBHandler.COLUMN_ID}
                    """.trimMargin()
                )
                projectionLocal = arrayOf(
                    "${DBHandler.TABLE_LIST}.${DBHandler.COLUMN_ID}",
                    "${DBHandler.TABLE_LIST}.${DBHandler.COLUMN_LIST_NAME}",
                    "${DBHandler.TABLE_LIST}.${DBHandler.COLUMN_DATE}",
                    "${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_PRODUCT_ID}",
                    "${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_QUANTITY}",
                    "${DBHandler.TABLE_PRODUCT}.${DBHandler.COLUMN_PRODUCT_NAME}",
                    "${DBHandler.TABLE_PRODUCT}.${DBHandler.COLUMN_PRICE}",
                    "${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_MARK}"
                )
            }
            URI_PRODUCT_LIST_CODE -> {
                val productId = uri.pathSegments.last()
                val listId = uri.pathSegments.takeLast(2).first()
                queryBuilder.tables =
                    "${DBHandler.TABLE_PRODUCT_LIST}, ${DBHandler.TABLE_PRODUCT}"
                queryBuilder.appendWhere(
                    """${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_LIST_ID} = $listId
                        AND ${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_PRODUCT_ID} = $productId
                        AND ${DBHandler.TABLE_PRODUCT}.${DBHandler.COLUMN_ID} = $productId
                    """.trimMargin()
                )
                projectionLocal = arrayOf(
                    "${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_PRODUCT_ID}",
                    "${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_QUANTITY}",
                    "${DBHandler.TABLE_PRODUCT}.${DBHandler.COLUMN_PRODUCT_NAME}",
                    "${DBHandler.TABLE_PRODUCT}.${DBHandler.COLUMN_PRICE}",
                    "${DBHandler.TABLE_PRODUCT_LIST}.${DBHandler.COLUMN_MARK}"
                )
            }
            URI_ALL_LISTS_CODE -> {
                queryBuilder.tables = DBHandler.TABLE_LIST
            }
            else -> throw IllegalArgumentException("Unknown URI")
        }

        val cursor = queryBuilder.query(
            myDB?.readableDatabase,
            projectionLocal, selection, selectionArgs, null, null,
            sortOrder
        )
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val uriType = sURIMatcher.match(uri)

        val sqlDB = myDB!!.writableDatabase
        val retUri: String
        val id: Long
        when (uriType) {
            URI_ALL_PRODUCTS_CODE -> {
                id = sqlDB.insert(DBHandler.TABLE_PRODUCT, null, values)
                retUri = URI_NAME_PRODUCTS
            }
            URI_ALL_LISTS_CODE -> {
                id = sqlDB.insert(DBHandler.TABLE_LIST, null, values)
                retUri = URI_NAME_LISTS
            }
            URI_LIST_CODE -> {
                id = uri.lastPathSegment!!.toLong()
                val queryBuilder = SQLiteQueryBuilder()
                queryBuilder.tables = DBHandler.TABLE_PRODUCT
                queryBuilder.appendWhere(
                    "${DBHandler.COLUMN_PRODUCT_NAME} = '${values!!.get(DBHandler.COLUMN_PRODUCT_NAME)}'"
                )
                val cursor = queryBuilder.query(
                    myDB?.readableDatabase,
                    null, null, null, null, null,
                    null
                )

                val productContent = ContentValues()
                productContent.put(
                    DBHandler.COLUMN_PRICE,
                    values.getAsFloat(DBHandler.COLUMN_PRICE)
                )
                val productId: Long
                if (!cursor.moveToFirst()) {
                    productContent.put(
                        DBHandler.COLUMN_PRODUCT_NAME,
                        values.getAsString(DBHandler.COLUMN_PRODUCT_NAME)
                    )
                    productId = sqlDB.insert(DBHandler.TABLE_PRODUCT, null, productContent)
                } else {
                    productId = cursor.getString(0).toLong()
                    sqlDB.update(
                        DBHandler.TABLE_PRODUCT,
                        productContent,
                        DBHandler.COLUMN_ID + "=" + productId,
                        null
                    )
                }
                cursor.close()
                val intersectionContent = ContentValues()
                intersectionContent.put(DBHandler.COLUMN_PRODUCT_ID, productId)
                intersectionContent.put(DBHandler.COLUMN_LIST_ID, id)
                intersectionContent.put(
                    DBHandler.COLUMN_QUANTITY,
                    values.getAsInteger(DBHandler.COLUMN_QUANTITY)
                )
                intersectionContent.put(DBHandler.COLUMN_MARK, false)
                if (id != -1L && productId != -1L) {
                    try {
                        sqlDB.insert(DBHandler.TABLE_PRODUCT_LIST, null, intersectionContent)
                    } catch (ex: SQLiteConstraintException) {
                        return null
                    }
                }
                retUri = URI_NAME_LISTS
                return Uri.parse("$retUri/$id/$productId")
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        return Uri.parse("$retUri/$id")
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = myDB!!.writableDatabase
        val rowsDeleted: Int

        when (uriType) {
            URI_ALL_PRODUCTS_CODE -> rowsDeleted = sqlDB.delete(
                DBHandler.TABLE_PRODUCT,
                selection,
                selectionArgs
            )

            URI_PRODUCT_CODE -> {
                val id = uri.lastPathSegment
                rowsDeleted = if (TextUtils.isEmpty(selection)) {
                    sqlDB.delete(
                        DBHandler.TABLE_PRODUCT,
                        DBHandler.COLUMN_ID + "=" + id,
                        null
                    )
                } else {
                    sqlDB.delete(
                        DBHandler.TABLE_PRODUCT,
                        DBHandler.COLUMN_ID + "=" + id
                                + " and " + selection,
                        selectionArgs
                    )
                }
            }
            URI_ALL_LISTS_CODE -> rowsDeleted = sqlDB.delete(
                DBHandler.TABLE_LIST,
                selection,
                selectionArgs
            )
            URI_LIST_CODE -> {
                val id = uri.lastPathSegment
                rowsDeleted = if (TextUtils.isEmpty(selection)) {
                    sqlDB.delete(
                        DBHandler.TABLE_PRODUCT_LIST,
                        DBHandler.COLUMN_LIST_ID + "=" + id,
                        null
                    )
                    sqlDB.delete(
                        DBHandler.TABLE_LIST,
                        DBHandler.COLUMN_ID + "=" + id,
                        null
                    )
                } else {
                    sqlDB.delete(
                        DBHandler.TABLE_LIST,
                        DBHandler.COLUMN_ID + "=" + id
                                + " and " + selection,
                        selectionArgs
                    )
                }
            }
            URI_PRODUCT_LIST_CODE -> {
                val productId = uri.pathSegments.last()
                val listId = uri.pathSegments.takeLast(2).first()
                rowsDeleted = if (TextUtils.isEmpty(selection)) {
                    sqlDB.delete(
                        DBHandler.TABLE_PRODUCT_LIST,
                        DBHandler.COLUMN_LIST_ID + "=" + listId + " and " + DBHandler.COLUMN_PRODUCT_ID + "=" + productId,
                        null
                    )
                } else {
                    sqlDB.delete(
                        DBHandler.TABLE_PRODUCT_LIST,
                        DBHandler.COLUMN_LIST_ID + "=" + listId + " and " + DBHandler.COLUMN_PRODUCT_ID + "=" + productId
                                + " and " + selection,
                        selectionArgs
                    )
                }
            }
            else -> throw IllegalArgumentException("UnknownURI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = myDB!!.writableDatabase
        val rowsUpdated: Int
        when (uriType) {
            URI_ALL_PRODUCTS_CODE -> rowsUpdated = sqlDB.update(
                DBHandler.TABLE_PRODUCT,
                values,
                selection,
                selectionArgs
            )
            URI_PRODUCT_LIST_CODE -> {
                val productId = uri.pathSegments.last()
                val listId = uri.pathSegments.takeLast(2).first()
                rowsUpdated = if (TextUtils.isEmpty(selection)) {
                    sqlDB.update(
                        DBHandler.TABLE_PRODUCT_LIST,
                        values,
                        DBHandler.COLUMN_LIST_ID + "=" + listId + " and " + DBHandler.COLUMN_PRODUCT_ID + "=" + productId,
                        null
                    )
                } else {
                    sqlDB.update(
                        DBHandler.TABLE_PRODUCT_LIST,
                        values,
                        DBHandler.COLUMN_LIST_ID + "=" + listId + " and " + DBHandler.COLUMN_PRODUCT_ID + "=" + productId
                                + " and " + selection,
                        selectionArgs
                    )
                }
            }
            URI_LIST_CODE -> {
                val listId = uri.pathSegments.last()
                rowsUpdated = if (TextUtils.isEmpty(selection)) {
                    sqlDB.update(
                        DBHandler.TABLE_LIST,
                        values,
                        DBHandler.COLUMN_ID + "=" + listId,
                        null
                    )
                } else {
                    sqlDB.update(
                        DBHandler.TABLE_LIST,
                        values,
                        DBHandler.COLUMN_ID + "=" + listId
                                + " and " + selection,
                        selectionArgs
                    )
                }
            }
            URI_PRODUCT_CODE -> {
                val productId = uri.pathSegments.last()
                rowsUpdated = if (TextUtils.isEmpty(selection)) {
                    sqlDB.update(
                        DBHandler.TABLE_PRODUCT,
                        values,
                        DBHandler.COLUMN_ID + "=" + productId,
                        null
                    )
                } else {
                    sqlDB.update(
                        DBHandler.TABLE_PRODUCT,
                        values,
                        DBHandler.COLUMN_ID + "=" + productId
                                + " and " + selection,
                        selectionArgs
                    )
                }
            }
            else -> throw IllegalArgumentException("UnknownURI: $uri")
        }
        return rowsUpdated
    }
}