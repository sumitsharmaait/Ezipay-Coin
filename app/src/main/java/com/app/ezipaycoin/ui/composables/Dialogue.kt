package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.ezipaycoin.ui.theme.DialogBackground
import com.app.ezipaycoin.ui.theme.DialogIconCircleGreen
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.sendAmountTextColor

@Composable
fun Dialogue(isError: Boolean, msg: String, onDismissRequest: () -> Unit) {

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allow custom width
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        val goldGradient = Brush.verticalGradient(
            colors = listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4)
        )

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = DialogBackground),
            modifier = Modifier
                .fillMaxWidth(0.85f) // Set dialog width
                .border(
                    BorderStroke(1.dp, goldGradient),
                    MaterialTheme.shapes.extraLarge
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(if (isError) sendAmountTextColor.copy(alpha = 0.1f) else DialogIconCircleGreen.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, if (isError) sendAmountTextColor else DialogIconCircleGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Filled.ErrorOutline else Icons.Filled.Check,
                        contentDescription = "Success",
                        tint = if (isError) sendAmountTextColor else DialogIconCircleGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    msg,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimaryColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                GoldGradientButton(
                    label = "Done",
                    onClick = {
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

            }
        }

    }

}

//@Composable
//@Preview
//fun DialogPreview(){
//    EzipayCoinTheme {
//        Dialogue(isError = true, "Error can be long enough to go in the next line") {
//
//        }
//    }
//}