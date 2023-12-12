package com.sph.diagnostics.dji

import com.sph.diagnostics.dji.tools.ensureDjiSdkIsInitialized
import com.sph.diagnostics.dji.tools.listen
import com.sph.diagnostics.dji.tools.listenPayloadBasicInfo
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.v5.et.create
import dji.v5.manager.KeyManager
import dji.v5.manager.aircraft.payload.PayloadCenter
import dji.v5.manager.aircraft.payload.PayloadIndexType
import dji.v5.manager.interfaces.IPayloadManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

import org.junit.Test

import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

class ReadDataFromPayload {
    /**
     * This test explains the issue when MSDK doesn't receive data from the payload.
     *
     *
     * Common preconditions:
     * - The payload is an onboard computer that is connected to DJI M350 via UART;
     * - The payload hosts a simple PSDK application that does nothing but sends
     * some small data every 1 second.
     *
     * SCENARIO 1 (success) - test receives data successfully.
     * Preconditions: the payload is switched on.
     * Scenario: Run the test, wait for the result.
     * Result: the test received data from the payload, test passes.
     *
     * SCENARIO 2 (failure) - test does not receive any data from the payload.
     * Preconditions: the payload is switched off
     * Scenario: Run the test, switch on the payload, wait for the result.
     * Result: Test fails as there is no data received from the payload.
     */
    @Test
    fun mobileAppStartedFirst(): Unit = runBlocking {
        ensureDjiSdkIsInitialized()

        awaitAircraft()

        // The payload is connected to the EXTERNAL port as it is an onboard computer
        val pm = PayloadCenter.getInstance().payloadManager[PayloadIndexType.EXTERNAL]!!

        awaitPayload(pm)


        // Now we know (from MSDK) that the payload is connected,
        // let's listen data from the payload

        val dataChannel = Channel<ByteArray>()
        pm.addPayloadDataListener { data ->
            log.info("${data.size} bytes received from the payload.")
            val r = dataChannel.trySendBlocking(data)
            if (r.isFailure)
                dataChannel.close(Exception("Failed to send data to the channel.", r.exceptionOrNull()))
        }

        // Waiting for any data from the payload, up to 30 seconds.
        withTimeout(30.seconds) {
            dataChannel.receive()
        }
    }

    /**
     * Returns as soon as any payload is being connected on the payload manager.
     */
    private suspend fun awaitPayload(pm: IPayloadManager) {
        pm.listenPayloadBasicInfo().first { it.isConnected }
    }

    /**
     * Returns as soon as the aircraft is being connected.
     */
    private suspend fun awaitAircraft() {
        val km = KeyManager.getInstance()
        val fcConnectedKey = FlightControllerKey.KeyConnection.create()

        if (km.getValue(fcConnectedKey) == true)
            return

        km.listen(fcConnectedKey)
            .first { it == true }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ReadDataFromPayload::class.java)
    }
}