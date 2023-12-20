package com.sph.diagnostics.dji.tools

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sph.diagnostics.dji.ReadDataFromPayload
import dji.sdk.keyvalue.key.DJIKey
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.KeyManager
import dji.v5.manager.SDKManager
import dji.v5.manager.aircraft.payload.data.PayloadBasicInfo
import dji.v5.manager.aircraft.payload.listener.PayloadBasicInfoListener
import dji.v5.manager.interfaces.IPayloadManager
import dji.v5.manager.interfaces.ISDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


private val log = LoggerFactory.getLogger("MsdkFacade")
private var isInitialized = false
private val initializationSyncObj = Mutex(false)
suspend fun ensureDjiSdkIsInitialized(): ISDKManager {
    val sdk = SDKManager.getInstance()

    initializationSyncObj.withLock {
        if (!isInitialized) {
            sdk.init(InstrumentationRegistry.getInstrumentation().targetContext)
            isInitialized = true
        }
    }

    return sdk
}

private suspend fun ISDKManager.init(context: Context) {
    return suspendCoroutine { continuation ->
        init(context, object : SDKManagerCallback {
            override fun onRegisterSuccess() {
                continuation.resume(Unit)
            }

            override fun onRegisterFailure(djiError: IDJIError?) {
                continuation.resumeWithException(djiError?.asException()
                    ?: Exception("Unknown error."))
            }
            override fun onProductDisconnect(productId: Int) {
            }

            override fun onProductConnect(productId: Int) {
            }

            override fun onProductChanged(productId: Int) {
            }

            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    registerApp()
                }
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
            }
        })
    }
}

suspend fun IPayloadManager.sendDataToPayload(data: ByteArray) = suspendCancellableCoroutine<Unit> {continuation ->
    sendDataToPayload(data, object : CommonCallbacks.CompletionCallback {
        override fun onSuccess() {
            continuation.resume(Unit)
        }

        override fun onFailure(error: IDJIError) {
            continuation.resumeWithException(error.asException())
        }
    })
}

fun IPayloadManager.listenPayloadBasicInfo(): Flow<PayloadBasicInfo> = callbackFlow {
    val listener = PayloadBasicInfoListener {
        val r = trySendBlocking(it)
        if (r.isFailure) {
            val e = r.exceptionOrNull()
            val msg = "Failed to send payload info into the channel."
            if (e != null)
                log.error(msg, e)
            else
                log.error(msg)
        }
    }

    addPayloadBasicInfoListener(listener)

    awaitClose {
        removePayloadBasicInfoListener(listener)
    }
}


fun <T> KeyManager.listen(key: DJIKey<T>): Flow<T?> = callbackFlow {
    val holder = Any()
    val listener = CommonCallbacks.KeyListener<T> { _, newValue ->
        val r = trySendBlocking(newValue)
        if (r.isFailure) {
            val msg = "Failed to send a value of DJI key '$key' into callback flow.";
            val e = r.exceptionOrNull()
            if (e != null)
                log.error(msg, e)
            else
                log.error(msg)
        }
    }

    listen(key, holder, listener)

    awaitClose {
        cancelListen(key, holder)
    }
}


fun IDJIError.asException(): Throwable {
    if (this.errorCode() == "REQUEST_TIMEOUT")
        return TimeoutException()
    return DjiErrorException(this)
}

class DjiErrorException(djiError: IDJIError) : Exception(toString(djiError)) {
    companion object {
        fun toString(e: IDJIError): String {
            return "type: ${e.errorType()}, code: ${e.errorCode()}, description: ${e.description()}"
        }
    }
}

