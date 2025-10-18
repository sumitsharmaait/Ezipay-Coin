package com.app.ezipaycoin.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.tagsTextColor
import com.app.ezipaycoin.utils.ResponseState

@Composable
fun EditableText(
    name: String,
    saveState: ResponseState<Any>,
    onSave: (String) -> Unit
) {

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(name) }

    LaunchedEffect(name) {
        editedName = name
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        if (isEditing) {
            AppTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = "Name",
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                onSave(editedName)
                isEditing = false
            }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = tagsTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryColor,
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(onClick = { isEditing = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit name",
                    tint = TextPrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (saveState == ResponseState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp),
                strokeWidth = 2.dp,
                color = OnboardingGold1
            )
        }
    }
}