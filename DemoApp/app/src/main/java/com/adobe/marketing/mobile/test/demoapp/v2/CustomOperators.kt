package com.adobe.marketing.mobile.test.demoapp.v2

import com.adobe.marketing.mobile.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import java.time.Duration

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun Flow<Event>.readyForEvent(): Flow<List<Event>> {

    return flow {
        coroutineScope {
            val events = ArrayList<Event>()
            try {
                val upstreamValues = produce { collect { send(it) } }

                while (isActive) {

                    select<Unit> {
                        upstreamValues.onReceive {
                            events.add(it)
                            if (it.source == "Configuration") {
                                emit(events.toList())
                                events.clear()
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // drain remaining events
                if (events.isNotEmpty()) emit(events.toList())
            } finally {
//                tickerChannel.cancel()
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <T> Flow<T>.bufferTimeout(size: Int, duration: Duration): Flow<List<T>> {
    require(size > 0) { "Window size should be greater than 0" }
    require(duration.toMillis() > 0) { "Duration should be greater than 0" }

    return flow {
        coroutineScope {
            val events = ArrayList<T>(size)
            val tickerChannel = ticker(duration.toMillis())
            try {
                val upstreamValues = produce { collect { send(it) } }

                while (isActive) {
                    var hasTimedOut = false

                    select<Unit> {
                        upstreamValues.onReceive {
                            events.add(it)
                        }

                        tickerChannel.onReceive {
                            hasTimedOut = true
                        }
                    }

                    if (events.size == size || (hasTimedOut && events.isNotEmpty())) {
                        emit(events.toList())
                        events.clear()
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // drain remaining events
                if (events.isNotEmpty()) emit(events.toList())
            } finally {
                tickerChannel.cancel()
            }
        }
    }
}