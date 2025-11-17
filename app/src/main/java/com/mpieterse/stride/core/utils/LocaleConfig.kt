package com.mpieterse.stride.core.utils

import android.content.Context
import com.mpieterse.stride.R
import org.xmlpull.v1.XmlPullParser

object LocaleConfig {


    private const val LOCALE_XML_NODE = "locale"
    private const val NAME_XML_ATTRIBUTE_HEAD = "http://schemas.android.com/apk/res/android"
    private const val NAME_XML_ATTRIBUTE_FOOT = "name"


    /**
     * Serializes the XML document into a Kotlin-type collection.
     */
    fun readLocales(context: Context): List<String> {
        val idCollection = R.xml.locales_config
        if (idCollection != 0) {
            val parser = context.resources.getXml(idCollection)
            val result = mutableListOf<String>()

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == LOCALE_XML_NODE) {
                    val tag = parser.getAttributeValue(
                        NAME_XML_ATTRIBUTE_HEAD,
                        NAME_XML_ATTRIBUTE_FOOT
                    )

                    if (!tag.isNullOrBlank()) {
                        result.add(tag)
                    }
                }

                eventType = parser.next()
            }

            return result
        }

        return emptyList()
    }
}