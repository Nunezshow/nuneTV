package com.nunetv.iptv.data

import com.nunetv.iptv.model.EpgProgram
import java.io.StringReader
import org.xml.sax.InputSource
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class EpgParser {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    fun parse(xmlContent: String): List<EpgProgram> {
        if (xmlContent.isBlank()) return emptyList()
        val factory = SAXParserFactory.newInstance()
        val programs = mutableListOf<EpgProgram>()
        val handler = object : DefaultHandler() {
            var currentChannelId: String? = null
            var currentTitle: StringBuilder? = null
            var currentDescription: StringBuilder? = null
            var start: Long = 0
            var stop: Long = 0
            var inTitle = false
            var inDesc = false

            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                when (qName.lowercase(Locale.ROOT)) {
                    "programme" -> {
                        currentChannelId = attributes.getValue("channel")
                        start = parseDate(attributes.getValue("start"))
                        stop = parseDate(attributes.getValue("stop"))
                        currentTitle = StringBuilder()
                        currentDescription = StringBuilder()
                    }
                    "title" -> {
                        inTitle = true
                    }
                    "desc" -> {
                        inDesc = true
                    }
                }
            }

            override fun endElement(uri: String?, localName: String?, qName: String) {
                when (qName.lowercase(Locale.ROOT)) {
                    "programme" -> {
                        val id = currentChannelId
                        if (!id.isNullOrBlank() && start > 0 && stop > 0) {
                            programs += EpgProgram(
                                channelId = id,
                                title = currentTitle?.toString().orEmpty(),
                                description = currentDescription?.toString(),
                                startTime = start,
                                endTime = stop
                            )
                        }
                        currentChannelId = null
                        currentTitle = null
                        currentDescription = null
                        start = 0
                        stop = 0
                    }
                    "title" -> inTitle = false
                    "desc" -> inDesc = false
                }
            }

            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (inTitle) {
                    currentTitle?.append(ch, start, length)
                } else if (inDesc) {
                    currentDescription?.append(ch, start, length)
                }
            }
        }

        factory.newSAXParser().parse(org.xml.sax.InputSource(StringReader(xmlContent)), handler)
        return programs
    }

    private fun parseDate(value: String?): Long {
        if (value.isNullOrBlank()) return 0
        return try {
            dateFormat.parse(value)?.time ?: 0
        } catch (ex: ParseException) {
            0
        }
    }
}
