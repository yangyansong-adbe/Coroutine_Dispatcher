package com.adobe.marketing.mobile.test.demoapp.v2

import android.util.Log
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
fun Flow<Event>.readyForEvent(predicate: suspend (Event) -> Boolean): Flow<Event> {

    return flow {
        coroutineScope {
            val events = ArrayDeque<Event>()
            try {
                val upstreamValues = produce {
                    collect {
                        Log.e("readyForEvent_1", "collect, thread name = ${Thread.currentThread().name}")
                        send(it)
                    }
                }

                while (isActive) {

                    select<Unit> {
                        upstreamValues.onReceive {
                            events.add(it)
                            while (events.isNotEmpty()) {
                                Log.e("readyForEvent_2", "thread name = ${Thread.currentThread().name}")
                                if (predicate(it)) {
                                    Log.e("readyForEvent_2", "emit, thread name = ${Thread.currentThread().name}")
                                    emit(events.removeFirst())
                                } else {
                                    Log.e("readyForEvent_2", "break, thread name = ${Thread.currentThread().name}")
                                    break
                                }
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // drain remaining events
//                if (events.isNotEmpty()) emit(events.toList())
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