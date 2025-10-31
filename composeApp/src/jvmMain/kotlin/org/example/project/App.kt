package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import inventoryapp.composeapp.generated.resources.Res
import inventoryapp.composeapp.generated.resources.compose_multiplatform
import androidx.compose.ui.graphics.Color
/*
import composeResources.searchIcon
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Search
*/

@Composable
@Preview
fun App() {
    MaterialTheme {
        WelcomePage()
        /*
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        } */
    }
}

val darkBlue = Color(0xFF023047)
val medBlue = Color(0xFF219EBC)
val lightBlue = Color(0xFF8ECAE6)
val yellow = Color(0xFFFFB703)
val orange = Color(0xFFFB8500)


val monospace = FontFamily.Monospace

@Composable
@Preview
fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = darkBlue)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.SpaceAround,
    ) { // full page
        Column(
            modifier = Modifier
                .weight(3f)
                .fillMaxWidth(),
                //.background(color = medBlue), // remove background color
            horizontalAlignment = Alignment.CenterHorizontally
        ) { // title
            Text(
                "GROCERY STORE",
                color = yellow,
                fontFamily = monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                modifier = Modifier.padding(top = 100.dp)
            )
            Text(
                "INVENTORY",
                color = yellow,
                fontFamily = monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                //.background(color = orange) // remove background color
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.SpaceEvenly,
        ) { // actions
            ElevatedButton( // view inventory button
                onClick = {},
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = lightBlue,
                    contentColor = darkBlue,
                ),
                //elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(250.dp),
                    //.padding(top = 20.dp),
            ) {
                Text(
                    "VIEW INVETORY",
                    fontFamily = monospace,
                    fontSize = 16.sp,
                )
            }

            ElevatedButton( // generate report button
                onClick = {},
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = lightBlue,
                    contentColor = darkBlue
                ),
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 30.dp),
            ) {
                Text(
                    "GENERATE REPORT",
                    fontFamily = monospace,
                    fontSize = 16.sp
                )
            }
        }
    }
}
