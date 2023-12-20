package com.sph.diagnostics.dji.tools

import java.nio.ByteBuffer
import java.nio.ByteOrder


interface CrcCalculator {
    fun feedByte(data: Byte)
    fun feedBytes(data: ByteArray?)
    val crcValue: Int
}

class CrcMCRF4XX : CrcCalculator {
    override var crcValue: Int = DEFAULT_CRC_INIT_VALUE

    override fun feedByte(data: Byte) {
        var tmp = data.toInt() and 0xff xor (crcValue and 0xff)
        tmp = tmp xor (tmp shl 4 and 0xff)
        crcValue = crcValue shr 8 and 0xff xor (tmp shl 8 xor (tmp shl 3) xor (tmp shr 4 and 0xff))
    }

    override fun feedBytes(data: ByteArray?) {
        for (b in data!!) {
            feedByte(b)
        }
    }

    companion object {
        private const val DEFAULT_CRC_INIT_VALUE = 0xFFFF
    }
}

class OnboardMessage {
    val id: Byte
    val length: Byte
    val payload: ByteArray
    val crc: Short

    private constructor(id: Byte, payload: ByteArray, crc: Short) {
        this.id = id
        length = payload.size.toByte()
        this.payload = payload
        this.crc = crc
    }

    constructor(id: Byte, payload: ByteArray) {
        this.id = id
        length = payload.size.toByte()
        this.payload = payload
        crc = computeCrc()
    }

    private fun computeCrc(): Short {
        val crcCalculator: CrcCalculator = CrcMCRF4XX()
        crcCalculator.feedByte(id)
        crcCalculator.feedByte(length)
        crcCalculator.feedBytes(payload)
        return crcCalculator.crcValue.toShort()
    }

    val isValid: Boolean
        get() = crc == computeCrc()

    fun generateByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 1 + payload.size + 2).order(BYTE_ORDER)
        return buffer.put(id).put(length).put(payload).putShort(crc).array()
    }

    companion object {
        // First two bytes of data section identifies the SkyHub command.
        // 0xC0 means "upload_mission" command
        const val SIZE_OF_COMMAND_ID_BYTES: Byte = 2
        const val CMD_PAYLOAD = 0xFC.toByte()
        const val CMD_PING = 0xFE.toByte()
        val BYTE_ORDER: ByteOrder = ByteOrder.LITTLE_ENDIAN
        const val EXTRA_BYTES = 4 // id + length + crc
        fun parseMessage(message: ByteArray): OnboardMessage {
            val buffer = ByteBuffer.wrap(message).order(BYTE_ORDER)
            val id = buffer.get()
            val payloadLength = buffer.get()
            val payload = ByteArray(payloadLength.toInt())
            buffer.get(payload)
            val crc = buffer.short
            return OnboardMessage(id, payload, crc)
        }
    }
}

data class Version(val Major: Int, val Minor: Int) : Comparable<Version> {
    override fun toString(): String {
        return "$Major.$Minor"
    }

    override operator fun compareTo(other: Version): Int {
        if (other.Major > Major)
            return LESS_THEN_OTHER
        else if (other.Major < Major)
            return GREATER_THEN_OTHER

        // Major versions are equal

        else if (other.Minor > Minor)
            return LESS_THEN_OTHER
        else if (other.Minor < Minor)
            return GREATER_THEN_OTHER
        else
            return EQ
    }

    companion object {
        const val LESS_THEN_OTHER = -1
        const val GREATER_THEN_OTHER = 1
        const val EQ = 0
    }
}

fun pingMessage(protocolVersion: Version = Version(1, 1)) = OnboardMessage(
    OnboardMessage.CMD_PING,
    ByteBuffer.allocate(10)
        .order(OnboardMessage.BYTE_ORDER)
        .putLong(System.currentTimeMillis())
        .array()
        .apply {
            set(8, protocolVersion.Major.toByte())
            set(9, protocolVersion.Minor.toByte())
        }
).generateByteArray()