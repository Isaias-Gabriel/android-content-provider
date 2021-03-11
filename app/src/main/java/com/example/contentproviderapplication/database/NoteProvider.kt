package com.example.contentproviderapplication.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.UnsupportedSchemeException
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns._ID
import androidx.annotation.RequiresApi
import com.example.contentproviderapplication.database.NotesDatabaseHelper.Companion.TABLE_NOTES

class NoteProvider : ContentProvider() {

    private lateinit var mUriMatcher: UriMatcher
    private lateinit var databaseHelper: NotesDatabaseHelper

    override fun onCreate(): Boolean {
        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        mUriMatcher.addURI(AUTHORITY, "notes", NOTES)
        mUriMatcher.addURI(AUTHORITY, "notes/#", NOTES_BY_ID)

        if(context != null) {
            databaseHelper = NotesDatabaseHelper(context as Context)
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if(mUriMatcher.match(uri) == NOTES_BY_ID) {
            val database: SQLiteDatabase = databaseHelper.writableDatabase
            val linesAffected = database.delete(
                TABLE_NOTES, "$_ID =?", arrayOf(uri.lastPathSegment)
            )
            database.close()
            context?.contentResolver?.notifyChange(uri, null)

            return linesAffected
        }

        else {
            throw UnsupportedSchemeException("Invalid uri for delete.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun getType(uri: Uri): String? = throw UnsupportedSchemeException("Non implemented uri.")

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if(mUriMatcher.match(uri) == NOTES) {
            val database: SQLiteDatabase = databaseHelper.writableDatabase
            val id = database.insert(TABLE_NOTES, null, values)
            val insertUri = Uri.withAppendedPath(BASE_URI, id.toString())
            database.close()

            return insertUri
        }

        else {
            throw UnsupportedSchemeException("Invalid uri for insert.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when {
            mUriMatcher.match(uri) == NOTES -> {
                val database: SQLiteDatabase = databaseHelper.writableDatabase
                val cursor = database.query(
                    TABLE_NOTES, projection, selection, selectionArgs, null, null,
                    sortOrder
                )
                cursor.setNotificationUri(context?.contentResolver, uri)

                cursor
            }

            mUriMatcher.match(uri) == NOTES_BY_ID -> {
                val database: SQLiteDatabase = databaseHelper.writableDatabase
                val cursor = database.query(
                    TABLE_NOTES, projection, "$_ID = ?", arrayOf(uri.lastPathSegment),
                    null, null,
                    sortOrder
                )
                cursor.setNotificationUri((context as Context).contentResolver, uri)

                cursor
            }

            else -> {
                throw UnsupportedSchemeException("Non implemented uri.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if(mUriMatcher.match(uri) == NOTES_BY_ID) {
            val database: SQLiteDatabase = databaseHelper.writableDatabase
            val lineAffected = database.update(
                TABLE_NOTES, values, "$_ID =?", arrayOf(uri.lastPathSegment)
            )
            database.close()
            context?.contentResolver?.notifyChange(uri, null)

            return lineAffected
        }

        else {
            throw UnsupportedSchemeException("Non implemented uri.")
        }
    }

    companion object {
        const val AUTHORITY = "com.example.contentproviderapplication.provider"

        val BASE_URI = Uri.parse("content://$AUTHORITY")
        val URI_NOTES = Uri.withAppendedPath(BASE_URI, "notes")

        const val NOTES = 1
        const val NOTES_BY_ID = 2
    }
}