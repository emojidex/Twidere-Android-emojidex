package org.mariotaku.twidere.provider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.MimeTypeMap
import okio.ByteString
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.annotation.CacheFileType
import org.mariotaku.twidere.model.CacheMetadata
import org.mariotaku.twidere.task.SaveFileTask
import org.mariotaku.twidere.util.BitmapUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Created by mariotaku on 16/1/1.
 */
class CacheProvider : ContentProvider() {
    @Inject
    internal lateinit var fileCache: FileCache

    override fun onCreate(): Boolean {
        GeneralComponentHelper.build(context).inject(this)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        val metadata = getMetadata(uri)
        if (metadata != null) {
            return metadata.contentType
        }
        val type = uri.getQueryParameter(TwidereConstants.QUERY_PARAM_TYPE)
        when (type) {
            CacheFileType.IMAGE -> {
                val file = fileCache.get(getCacheKey(uri)) ?: return null
                return BitmapUtils.getImageMimeType(file)
            }
            CacheFileType.VIDEO -> {
                return "video/mp4"
            }
            CacheFileType.JSON -> {
                return "application/json"
            }
        }
        return null
    }

    fun getMetadata(uri: Uri): CacheMetadata? {
        val bytes = fileCache.getExtra(getCacheKey(uri)) ?: return null
        return ByteArrayInputStream(bytes).use {
            val mapper = LoganSquareMapperFinder.mapperFor(CacheMetadata::class.java)
            return mapper.parse(it)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        try {
            val file = fileCache.get(getCacheKey(uri)) ?: throw FileNotFoundException()
            val modeBits = modeToMode(mode)
            if (modeBits != ParcelFileDescriptor.MODE_READ_ONLY)
                throw IllegalArgumentException("Cache can't be opened for write")
            return ParcelFileDescriptor.open(file, modeBits)
        } catch (e: IOException) {
            throw FileNotFoundException()
        }

    }

    class CacheFileTypeCallback(private val context: Context, @CacheFileType private val type: String?) : SaveFileTask.FileInfoCallback {

        override fun getFilename(source: Uri): String {
            var cacheKey = getCacheKey(source)
            val indexOfSsp = cacheKey.indexOf("://")
            if (indexOfSsp != -1) {
                cacheKey = cacheKey.substring(indexOfSsp + 3)
            }
            return cacheKey.replace("[^\\w\\d_]".toRegex(), specialCharacter.toString())
        }

        override fun getMimeType(source: Uri): String? {
            if (type == null || source.getQueryParameter(TwidereConstants.QUERY_PARAM_TYPE) != null) {
                return context.contentResolver.getType(source)
            }
            val builder = source.buildUpon()
            builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_TYPE, type)
            return context.contentResolver.getType(builder.build())
        }

        override fun getExtension(mimeType: String?): String? {
            val typeLowered = mimeType?.toLowerCase(Locale.US) ?: return null
            return when (typeLowered) {
            // Hack for fanfou image type
                "image/jpg" -> "jpg"
                else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(typeLowered)
            }
        }

        override val specialCharacter: Char
            get() = '_'
    }


    companion object {

        fun getCacheUri(key: String, @CacheFileType type: String?): Uri {
            val builder = Uri.Builder()
            builder.scheme(ContentResolver.SCHEME_CONTENT)
            builder.authority(TwidereConstants.AUTHORITY_TWIDERE_CACHE)
            builder.appendPath(ByteString.encodeUtf8(key).base64Url())
            if (type != null) {
                builder.appendQueryParameter(TwidereConstants.QUERY_PARAM_TYPE, type)
            }
            return builder.build()
        }

        fun getCacheKey(uri: Uri): String {
            if (ContentResolver.SCHEME_CONTENT != uri.scheme)
                throw IllegalArgumentException(uri.toString())
            if (TwidereConstants.AUTHORITY_TWIDERE_CACHE != uri.authority)
                throw IllegalArgumentException(uri.toString())
            return ByteString.decodeBase64(uri.lastPathSegment).utf8()
        }


        /**
         * Copied from ContentResolver.java
         */
        private fun modeToMode(mode: String): Int {
            val modeBits: Int
            if ("r" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_ONLY
            } else if ("w" == mode || "wt" == mode) {
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            } else if ("wa" == mode) {
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_APPEND
            } else if ("rw" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            } else if ("rwt" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            } else {
                throw IllegalArgumentException("Invalid mode: " + mode)
            }
            return modeBits
        }
    }
}
