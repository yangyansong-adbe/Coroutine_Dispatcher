package com.adobe.marketing.mobile.test.demoapp.v2

import android.util.Log
import com.adobe.marketing.mobile.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

//    override fun subscribe(scope: CoroutineScope, block: suspend (Event) -> Unit) = events.onEach(block).launchIn(scope)

    override fun dispatch(event: Event) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            _eventsFlow.emit(event)
        }
    }

}

internal val eventHub = EventHubV2Impl()

internal fun initializeSDK() {
    initConfiguration()
    initEdge()
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun initEdge() {
    val scope = CoroutineScope(Dispatchers.Default)
    val job = eventHub.events.filter { event ->
        event.source in listOf("Configuration", "Edge")
    }.readyForEvent().onEach { events ->
        events.forEach {event ->
            Log.e("Edge", "Event: type = ${event.source}")
        }
    }.launchIn(scope)

}

private fun initConfiguration() {
    val scope = CoroutineScope(Dispatchers.Default)
    val job = eventHub.events
        .filter { event ->
            event.source in listOf("Configuration")
        }
//        .buffer()
        .onEach { event ->
            Log.e("Configuration", "Event: type = ${event.source}")
        }
        .launchIn(scope)
}

