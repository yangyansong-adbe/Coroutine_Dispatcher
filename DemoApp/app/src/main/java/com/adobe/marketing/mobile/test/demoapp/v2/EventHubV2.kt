package com.adobe.marketing.mobile.test.demoapp.v2

import android.util.Log
import com.adobe.marketing.mobile.Event
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Duration


internal interface EventHubV2 {
    //    fun subscribe(scope: CoroutineScope, block: suspend (Event) -> Unit) : Job
    fun dispatch(event: Event)
    val events: SharedFlow<Event>
}

internal class EventHubV2Impl : EventHubV2 {
    private val _eventsFlow = MutableSharedFlow<Event>()
    override val events = _eventsFlow.asSharedFlow()
    val scope = CoroutineScope(Dispatchers.Default)

//    override fun subscribe(scope: CoroutineScope, block: suspend (Event) -> Unit) = events.onEach(block).launchIn(scope)

    override fun dispatch(event: Event) {
        scope.launch(CoroutineName("DispatchEvent")) {
            Log.e("EventHub", "thread name = ${Thread.currentThread().name}")
            _eventsFlow.emit(event)
        }
    }

}

internal val eventHub = EventHubV2Impl()

internal fun initializeSDK() {
    initConfiguration()
    initEdge()
}

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
private fun initEdge() {
    var configurationIsReady = false
    val scope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))
    val job = eventHub.events.filter { event ->
        event.source in listOf("Configuration", "Edge")
    }.buffer(1000, BufferOverflow.DROP_LATEST).readyForEvent{
        if(it.source == "Configuration") {configurationIsReady = true}
        return@readyForEvent configurationIsReady
    }.onEach { event ->
        scope.launch {
            delay(10000)
            Log.e("Edge", "Event: type = ${event.source} thread name = ${Thread.currentThread().name}")
        }

    }.launchIn(CoroutineScope(Dispatchers.Default))

}

private fun initConfiguration() {
    val scope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))
    val job = eventHub.events
        .filter { event ->
            event.source in listOf("Configuration")
        }
//        .buffer()
        .onEach { event ->
            Log.e("Configuration", "Event: type = ${event.source} thread name = ${Thread.currentThread().name}")
        }
        .launchIn(scope)
}

