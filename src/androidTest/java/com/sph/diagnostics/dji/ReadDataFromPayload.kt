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
import kotlin.time.Duration.Companion.minutes

class ReadDataFromPayload {
    /**
     * This test explains the issue when MSDK doesn't receive data from the payload.
     *
     * Hardware configuration:
     * - The payload is an ARM64 onboard computer that is connected to DJI M350 via UART.
     * - The payload hosts a simple application that that does nothing but sends
     * some small data to the mobile application through DJI PSDK every 1 second.
     *
     * SCENARIO 1 (success).
     *
     * Preconditions:
     *      1. The drone is switched off;
     *      2. The RC is switched off;
     *      3. The Payload is connected to the drone and switched off.
     *
     * Steps:
     *      1. Switch on the drone and the payload, wait until the drone becomes initialized.
     *      2. When the drone is initialized switch on the RC.
     *      3. Run the test, wait for the result.
     *
     * Result: the test passed as it received data from the payload.
     *
     *
     * SCENARIO 2 (failure).
     *
     * * Preconditions:
     *      1. The drone is switched off;
     *      2. The RC is switched off;
     *      3. The Payload is connected to the drone and switched off.
     *
     *  Steps:
     *      1. Switch on the RC, wait until RC becomes running.
     *      2. Run the test.
     *      2. Switch on the drone and the payload.
     *      3. Wait for the test result.
     *
     * Result: The test fails as there is no data received from the payload
     *      during 2 minutes (2 minutes is enough to get the drone initialized).
     *
     * Expected result: the test receive some data from the payload.
     */
    @Test
    fun mobileAppStartedFirst(): Unit = runBlocking {
        ensureDjiSdkIsInitialized()

        awaitAircraft()

        // The payload is connected to the EXTERNAL port
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
        withTimeout(2.minutes) {
            dataChannel.receive()
        }
    }

    /**
     * Returns as soon as any payload is being connected on the passed payload manager.
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