package com.adobe.marketing.mobile.test.demoapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExperienceEvent
import com.adobe.marketing.mobile.test.demoapp.ui.theme.DemoAppTheme
import com.adobe.marketing.mobile.test.demoapp.v2.eventHub
import com.adobe.marketing.mobile.test.demoapp.v2.initializeSDK
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(8.dp),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            val counter = AtomicInteger(0)
            val requestNumber = 10
            val startTime = System.currentTimeMillis()

            (1..requestNumber).forEach { _ ->
                val xdmData = mapOf(
                    "eventType" to "SampleXDMEvent",
                    "sample" to "data"
                )
                val event: ExperienceEvent =
                    ExperienceEvent.Builder().setXdmSchema(xdmData).build()
                Edge.sendEvent(event){ handles->
//                handles.forEach {
//                    Log.e("Edge", "handle: $it")
//                }
                    if(counter.incrementAndGet() == requestNumber) {
                        val endTime = System.currentTimeMillis()
                        Thread.getAllStackTraces().keys.size.let {
                            Log.e("--------------------", "thread number: $it")
                        }
                        Thread.getAllStackTraces().keys.forEach { Log.e("--------------------", "thread: ${it.name}") }
                        Log.e("--------------------", "total time: ${endTime - startTime}")
                    }
                }
            }

        }) {
            Text(text = "sendEvent")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            initializeSDK()
        }) {
            Text(text = "Init")
        }
        Button(onClick = {
            eventHub.dispatch(Event.Builder("name1","t","Edge").build())
        }) {
            Text(text = "sendEdgeEvent")
        }
        Button(onClick = {
            eventHub.dispatch(Event.Builder("name2","t","Configuration").build())
        }) {
            Text(text = "sendConfig")
        }
        Button(onClick = {

        }) {
            Text(text = "xxx")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DemoAppTheme {
        Greeting("Android")
    }
}