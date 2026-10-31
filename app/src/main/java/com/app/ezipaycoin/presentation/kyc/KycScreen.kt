package com.app.ezipaycoin.presentation.kyc

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.VisualTransformation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import coil.compose.AsyncImage
import com.app.ezipaycoin.ui.composables.Dialogue
import com.app.ezipaycoin.ui.composables.GoldGradientButton
import com.app.ezipaycoin.ui.theme.AppBackgroundColor
import com.app.ezipaycoin.ui.theme.Gradient_1
import com.app.ezipaycoin.ui.theme.Gradient_2
import com.app.ezipaycoin.ui.theme.Gradient_3
import com.app.ezipaycoin.ui.theme.Gradient_4
import com.app.ezipaycoin.ui.theme.OnboardingGold1
import com.app.ezipaycoin.ui.theme.TextPrimaryColor
import com.app.ezipaycoin.ui.theme.greyButtonBackground
import com.app.ezipaycoin.utils.ResponseState
import com.app.ezipaycoin.utils.uriToTempFilePath
import java.io.File

private object KycFormDimens {
    val ScreenPadding = 16.dp
    val CardContentPadding = 20.dp
    val SectionSpacing = 16.dp
    val FieldSpacing = 14.dp
}

private val GradientBrush
    get() = Brush.linearGradient(listOf(Gradient_1, Gradient_2, Gradient_3, Gradient_4))

// ---------------------------------------------------------------------------
//  Capture helpers (require FileProvider "<applicationId>.fileprovider" +
//  res/xml/file_paths.xml exposing cache-path "kyc/", and CAMERA permission).
// ---------------------------------------------------------------------------
private fun createCaptureUri(context: Context, ext: String): Uri? = try {
    val dir = File(context.cacheDir, "kyc").apply { mkdirs() }
    val file = File.createTempFile("capture_", ".$ext", dir)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    Log.d("KYC", "createCaptureUri ok ext=$ext file=${file.absolutePath} uri=$uri")
    uri
} catch (e: Exception) {
    Log.e("KYC", "createCaptureUri FAILED ext=$ext — check fileprovider authority + file_paths.xml kyc/ path", e)
    null
}

/**
 * KYC capture flow aligned to common fintech standards: applicant details,
 * address, identity document (with per-type masking), and a video / liveness
 * check, gated behind an explicit consent declaration.
 *
 * NOTE on the ViewModel contract: this screen references several KycEvent /
 * KycState members. If any aren't in your VM yet, the list at the bottom of
 * this file documents what to add.
 */
@Composable
fun KycScreen(
    navController: NavController,
    viewModel: KycViewModel,
    onSubmitted: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    SubmitSideEffects(state = state, viewModel = viewModel, onSubmitted = onSubmitted)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(KycFormDimens.ScreenPadding)
        ) {
            KycTopBar(onBack = { navController.navigateUp() })
            Spacer(Modifier.height(8.dp))
            KycHeader()

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            ProgressTracker(state = state)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            PersonalDetailsCard(state = state, viewModel = viewModel)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            AddressCard(state = state, viewModel = viewModel)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            DocumentDetailsCard(state = state, viewModel = viewModel)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            UploadsCard(state = state, viewModel = viewModel)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            LivenessCard(state = state, viewModel = viewModel)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            ConsentCard(state = state, viewModel = viewModel)

            Spacer(Modifier.height(24.dp))
            SubmitButton(state = state, viewModel = viewModel)

            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
        }
    }
}

@Composable
private fun SubmitSideEffects(
    state: KycState,
    viewModel: KycViewModel,
    onSubmitted: () -> Unit
) {
    when (val res = state.submitResponse) {
        is ResponseState.Success -> Dialogue(isError = false, msg = "KYC submitted successfully") {
            viewModel.onEvent(KycEvent.DismissDialog(1))
            onSubmitted()
        }

        is ResponseState.Error -> Dialogue(isError = true, msg = res.message) {
            viewModel.onEvent(KycEvent.DismissDialog(1))
        }

        else -> Unit
    }
}

// ---------------------------------------------------------------------------
//  Header + progress
// ---------------------------------------------------------------------------

@Composable
private fun KycTopBar(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, greyButtonBackground, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Identity verification",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun KycHeader() {
    Column {
        Text(
            text = "Verify your identity",
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = GradientBrush,
                fontWeight = FontWeight(800)
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "A regulated check that keeps your account and funds secure. It takes about 3 minutes.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f))
        )
    }
}

@Composable
private fun ProgressTracker(state: KycState) {
    val steps = remember(state) { completedSteps(state) }
    val done = steps.count { it.done }
    val total = steps.size

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Step $done of $total",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = OnboardingGold1,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (done == total) "All set" else "${total - done} left",
                style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.5f))
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            steps.forEach { step ->
                val segColor by animateColorAsState(
                    targetValue = if (step.done) OnboardingGold1 else greyButtonBackground,
                    animationSpec = tween(350),
                    label = "segColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(segColor)
                )
            }
        }
    }
}

private data class StepProgress(val label: String, val done: Boolean)

private fun completedSteps(state: KycState): List<StepProgress> {
    val spec = KycDocumentCatalog.byId(state.selectedDocTypeId)
    val personalDone = state.fullName.isNotBlank() &&
            state.dateOfBirth.isNotBlank() &&
            state.nationality.isNotBlank()
    val addressDone = state.addressLine.isNotBlank() && state.city.isNotBlank() &&
            state.postalCode.isNotBlank() && state.country.isNotBlank()
    val docDone = spec.isComplete(state.documentNumber) &&
            state.frontImagePath.isNotBlank() &&
            (!spec.requiresBack || state.backImagePath.isNotBlank())
    val livenessDone = state.videoPath.isNotBlank() || state.livenessVerified
    val consentDone = state.consentAccepted
    return listOf(
        StepProgress("Details", personalDone),
        StepProgress("Address", addressDone),
        StepProgress("Document", docDone),
        StepProgress("Liveness", livenessDone),
        StepProgress("Consent", consentDone)
    )
}

// ---------------------------------------------------------------------------
//  Reusable building blocks
// ---------------------------------------------------------------------------

@Composable
private fun SectionCard(
    index: Int,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        border = BorderStroke(1.dp, greyButtonBackground),
        colors = CardDefaults.cardColors(containerColor = AppBackgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(KycFormDimens.CardContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(OnboardingGold1.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = OnboardingGold1,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.55f))
                )
            }
            Spacer(Modifier.height(KycFormDimens.SectionSpacing))
            content()
        }
    }
}

@Composable
private fun KycTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = Color.White.copy(alpha = 0.35f)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimaryColor,
            unfocusedTextColor = TextPrimaryColor,
            cursorColor = OnboardingGold1,
            focusedBorderColor = OnboardingGold1,
            unfocusedBorderColor = greyButtonBackground,
            focusedLabelColor = OnboardingGold1,
            unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(color = Color.White.copy(alpha = 0.7f))
    )
}

// ---------------------------------------------------------------------------
//  Date of birth — calendar picker (read-only field + Material3 dialog)
// ---------------------------------------------------------------------------

private val DobDisplayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd / MM / yyyy", Locale.getDefault())

/** Parses a stored "DD / MM / YYYY" string back to epoch millis (UTC), or null. */
private fun dobToUtcMillis(stored: String): Long? = runCatching {
    val parts = stored.filter { it.isDigit() || it == '/' || it == ' ' }
        .split("/").map { it.trim() }
    if (parts.size != 3) return null
    val (d, m, y) = parts
    if (y.length != 4) return null
    LocalDate.of(y.toInt(), m.toInt(), d.toInt())
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()

private fun utcMillisToDobString(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
        .format(DobDisplayFormatter)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthField(
    value: String,
    onDateSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // Read-only field that opens the calendar on tap. We wrap it in a Box with a
    // transparent clickable overlay because OutlinedTextField swallows clicks
    // when not focusable.
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false, // disabled so the whole field is tappable; colors restored below
            label = { Text("Date of birth") },
            placeholder = { Text("DD / MM / YYYY", color = Color.White.copy(alpha = 0.35f)) },
            trailingIcon = {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = "Pick date",
                    tint = OnboardingGold1
                )
            },
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = TextPrimaryColor,
                disabledBorderColor = greyButtonBackground,
                disabledLabelColor = Color.White.copy(alpha = 0.6f),
                disabledPlaceholderColor = Color.White.copy(alpha = 0.35f),
                disabledTrailingIconColor = OnboardingGold1
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        // Transparent overlay to capture taps over the disabled field.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        val initialMillis = dobToUtcMillis(value)
            ?: LocalDate.now().minusYears(18)
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayMode = DisplayMode.Picker,
            // Block future dates — a DOB can't be in the future.
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()

                override fun isSelectableYear(year: Int): Boolean =
                    year <= LocalDate.now().year
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(utcMillisToDobString(it))
                        }
                        showDialog = false
                    }
                ) { Text("OK", color = OnboardingGold1) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            colors = DatePickerDefaults.colors(containerColor = AppBackgroundColor)
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                title = {
                    Text(
                        "Select your date of birth",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = AppBackgroundColor,

                    // Header / title / headline
                    titleContentColor = Color.White.copy(alpha = 0.7f),
                    headlineContentColor = TextPrimaryColor,

                    // Weekday letters (S M T W ...) and the "2024 ▼" subhead
                    weekdayContentColor = Color.White.copy(alpha = 0.7f),
                    subheadContentColor = Color.White.copy(alpha = 0.7f),

                    // Month/year navigation chevrons + label
                    navigationContentColor = TextPrimaryColor,

                    // Day cells
                    dayContentColor = TextPrimaryColor,
                    disabledDayContentColor = Color.White.copy(alpha = 0.3f),
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = OnboardingGold1,
                    disabledSelectedDayContentColor = Color.White.copy(alpha = 0.5f),

                    // Today marker
                    todayContentColor = OnboardingGold1,
                    todayDateBorderColor = OnboardingGold1,

                    // Year-grid picker
                    yearContentColor = TextPrimaryColor,
                    disabledYearContentColor = Color.White.copy(alpha = 0.3f),
                    currentYearContentColor = OnboardingGold1,
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = OnboardingGold1
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
//  1. Personal details
// ---------------------------------------------------------------------------

@Composable
private fun PersonalDetailsCard(state: KycState, viewModel: KycViewModel) {
    SectionCard(
        index = 1,
        title = "Personal details",
        subtitle = "Enter your legal name exactly as it appears on your ID."
    ) {
        KycTextField(
            value = state.fullName,
            onValueChange = { viewModel.onEvent(KycEvent.FullNameChanged(it)) },
            label = "Full legal name"
        )
        Spacer(Modifier.height(KycFormDimens.FieldSpacing))
        DateOfBirthField(
            value = state.dateOfBirth,
            onDateSelected = { viewModel.onEvent(KycEvent.DateOfBirthChanged(it)) }
        )
        Spacer(Modifier.height(KycFormDimens.FieldSpacing))
        KycTextField(
            value = state.nationality,
            onValueChange = { viewModel.onEvent(KycEvent.NationalityChanged(it)) },
            label = "Nationality"
        )
    }
}

/** DD / MM / YYYY masking from raw digits. */
private fun formatDate(input: String): String {
    val d = input.filter { it.isDigit() }.take(8)
    return buildString {
        append(d.take(2))
        if (d.length > 2) append(" / ").append(d.substring(2, minOf(4, d.length)))
        if (d.length > 4) append(" / ").append(d.substring(4))
    }
}

// ---------------------------------------------------------------------------
//  2. Address + proof of address
// ---------------------------------------------------------------------------

@Composable
private fun AddressCard(state: KycState, viewModel: KycViewModel) {
    SectionCard(
        index = 2,
        title = "Residential address",
        subtitle = "Your current address. We may ask for a proof of address document."
    ) {
        KycTextField(
            value = state.addressLine,
            onValueChange = { viewModel.onEvent(KycEvent.AddressLineChanged(it)) },
            label = "Address line"
        )
        Spacer(Modifier.height(KycFormDimens.FieldSpacing))
        Row(horizontalArrangement = Arrangement.spacedBy(KycFormDimens.FieldSpacing)) {
            KycTextField(
                value = state.city,
                onValueChange = { viewModel.onEvent(KycEvent.CityChanged(it)) },
                label = "City",
                modifier = Modifier.weight(1f)
            )
            KycTextField(
                value = state.postalCode,
                onValueChange = { viewModel.onEvent(KycEvent.PostalCodeChanged(it)) },
                label = "Postal code",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(KycFormDimens.FieldSpacing))
        KycTextField(
            value = state.country,
            onValueChange = { viewModel.onEvent(KycEvent.CountryChanged(it)) },
            label = "Country"
        )

        Spacer(Modifier.height(KycFormDimens.SectionSpacing))
        FieldLabel("Proof of address (optional)")
        Spacer(Modifier.height(8.dp))
        ImagePickerTile(
            label = "Utility bill / bank statement",
            hint = "Issued within the last 3 months, full address visible",
            tileKind = TileKind.DOCUMENT,
            imagePath = state.proofOfAddressPath,
            onPicked = { viewModel.onEvent(KycEvent.ProofOfAddressPicked(it)) }
        )
    }
}

// ---------------------------------------------------------------------------
//  3. Identity document (masking driven by the catalog)
// ---------------------------------------------------------------------------

@Composable
private fun DocumentDetailsCard(state: KycState, viewModel: KycViewModel) {
    val spec = KycDocumentCatalog.byId(state.selectedDocTypeId)

    SectionCard(
        index = 3,
        title = "Identity document",
        subtitle = "Pick a document type, then enter its number in the shown format."
    ) {
        FieldLabel("Document type")
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(KycDocumentCatalog.all) { doc ->
                DocTypeChip(
                    label = doc.label,
                    selected = state.selectedDocTypeId == doc.id,
                    onClick = { viewModel.onEvent(KycEvent.DocumentTypeSelected(doc.id)) }
                )
            }
        }

        Spacer(Modifier.height(KycFormDimens.SectionSpacing))

        val keyboard = when (spec.keyboard) {
            DocKeyboard.NUMBER -> KeyboardType.Number
            else -> KeyboardType.Text
        }
        KycTextField(
            value = state.documentNumber,
            onValueChange = { viewModel.onEvent(KycEvent.DocumentNumberChanged(spec.format(it))) },
            label = "${spec.label} number",
            placeholder = spec.placeholder,
            keyboardType = keyboard
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Format: ${spec.placeholder}  ·  ${spec.region}",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.45f))
        )
    }
}

@Composable
private fun DocTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (selected) Modifier.background(GradientBrush)
                else Modifier.background(greyButtonBackground)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else greyButtonBackground,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (selected) Color.Black else TextPrimaryColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

// ---------------------------------------------------------------------------
//  Photo uploads (front / back / selfie)
// ---------------------------------------------------------------------------

@Composable
private fun UploadsCard(state: KycState, viewModel: KycViewModel) {
    val spec = KycDocumentCatalog.byId(state.selectedDocTypeId)

    SectionCard(
        index = 3,
        title = "Document photos",
        subtitle = "Use your camera or gallery. Keep the document flat, in focus, and glare-free."
    ) {
        ImagePickerTile(
            label = "Front of document",
            hint = "Show the side with your photo and number",
            tileKind = TileKind.DOCUMENT,
            imagePath = state.frontImagePath,
            onPicked = { viewModel.onEvent(KycEvent.FrontImagePicked(it)) }
        )

        AnimatedVisibility(
            visible = spec.requiresBack,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(KycFormDimens.FieldSpacing))
                ImagePickerTile(
                    label = "Back of document",
                    hint = "Capture the reverse side",
                    tileKind = TileKind.DOCUMENT,
                    imagePath = state.backImagePath,
                    onPicked = { viewModel.onEvent(KycEvent.BackImagePicked(it)) }
                )
            }
        }

        Spacer(Modifier.height(KycFormDimens.FieldSpacing))
        ImagePickerTile(
            label = "Selfie",
            hint = "Face the camera in good lighting",
            tileKind = TileKind.SELFIE,
            imagePath = state.selfieImagePath,
            onPicked = { viewModel.onEvent(KycEvent.SelfieImagePicked(it)) }
        )
    }
}

// ---------------------------------------------------------------------------
//  4. Video / liveness
// ---------------------------------------------------------------------------

@Composable
private fun VideoUploadingTile() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, greyButtonBackground, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(color = OnboardingGold1, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                "Verifying your video…",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimaryColor, fontWeight = FontWeight.Medium
                )
            )
            Text(
                "This usually takes a few seconds",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun StatusTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    loading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, greyButtonBackground, RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(OnboardingGold1.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            if (loading) CircularProgressIndicator(
                color = OnboardingGold1,
                modifier = Modifier.size(20.dp)
            )
            else Icon(
                icon,
                contentDescription = null,
                tint = OnboardingGold1,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimaryColor, fontWeight = FontWeight.Medium
                )
            )
            Text(
                subtitle, style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.55f)
                )
            )
        }
    }
}

@Composable
private fun LivenessCard(state: KycState, viewModel: KycViewModel) {
    val context = LocalContext.current
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        Log.d("KYC", "CaptureVideo callback success=$success pendingUri=$pendingVideoUri")
        if (!success) {
            Log.w("KYC", "CaptureVideo returned false — user cancelled OR OEM camera didn't set RESULT_OK")
        }
        if (success) {
            val uri = pendingVideoUri
            if (uri == null) {
                Log.e("KYC", "pendingVideoUri is null in success branch — state was lost")
            } else {
                val p = context.uriToTempFilePath(uri)
                Log.d("KYC", "uriToTempFilePath -> $p")
                if (p == null) {
                    Log.e("KYC", "uriToTempFilePath returned null — copy from content uri failed")
                } else {
                    Log.d("KYC", "dispatching VideoRecorded path=$p")
                    viewModel.onEvent(KycEvent.VideoRecorded(p))
                }
            }
        }
        pendingVideoUri = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("KYC", "CAMERA permission granted=$granted")
        if (granted) {
            val uri = createCaptureUri(context, "mp4")
            if (uri != null) {
                pendingVideoUri = uri
                Log.d("KYC", "launching CaptureVideo with uri=$uri")
                videoLauncher.launch(uri)
            } else {
                Log.e("KYC", "createCaptureUri returned null — cannot launch camera")
            }
        } else {
            Log.w("KYC", "CAMERA permission denied")
        }
    }

    SectionCard(
        index = 4,
        title = "Video verification",
        subtitle = "A short video confirms you're a real, present person and matches your ID."
    ) {
        // Liveness toggle: when on, an SDK-based live session is used instead of a recording.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Automated liveness check",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimaryColor,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    "Guided on-screen prompts. Recommended.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.55f))
                )
            }
            Switch(
                checked = state.livenessEnabled,
                onCheckedChange = { viewModel.onEvent(KycEvent.LivenessToggled(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = OnboardingGold1,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = greyButtonBackground,
                    uncheckedBorderColor = greyButtonBackground
                )
            )
        }

        Spacer(Modifier.height(KycFormDimens.SectionSpacing))

        if (state.livenessEnabled) {
            LivenessAction(
                state = state.liveness,
                verified = state.livenessVerified,
                onStart = { viewModel.onEvent(KycEvent.StartLiveness) },
                onFrame = { viewModel.onEvent(KycEvent.LivenessFrame(it)) },
                onStop = { viewModel.onEvent(KycEvent.StopLiveness) }
            )
        } else {
            if (state.videoUploading) {
                VideoUploadingTile()
            } else {
                VideoRecordTile(
                    videoPath = state.videoPath,
                    error = state.videoUploadError,
                    onRecord = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
    }
}

@Composable
private fun LivenessAction(
    state: LivenessUiState,
    verified: Boolean,
    onStart: () -> Unit,
    onFrame: (String) -> Unit,
    onStop: () -> Unit
) {
    when {
        verified -> StatusTile(
            icon = Icons.Filled.CheckCircle,
            title = "Liveness verified", subtitle = "You're all set for this step"
        )

        state.connecting -> StatusTile(
            icon = Icons.Filled.Videocam,
            title = "Connecting…", subtitle = "Setting up secure session", loading = true
        )

        state.activeChallenge != null -> Column {
            LivenessCameraPreview(
                onFrame = onFrame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                state.activeChallenge,
                style = MaterialTheme.typography.titleMedium.copy(color = OnboardingGold1)
            )
            if (state.total > 0) {
                Text(
                    "Step ${state.step} of ${state.total}",
                    style = MaterialTheme.typography.bodySmall
                        .copy(color = Color.White.copy(alpha = 0.55f))
                )
            }
            TextButton(onClick = onStop) { Text("Cancel") }
        }

        state.error != null -> StatusTile(
            icon = Icons.Filled.Videocam,
            title = "Try again", subtitle = state.error, onClick = onStart
        )

        else -> StatusTile(
            icon = Icons.Filled.Videocam,
            title = "Start liveness check",
            subtitle = "Follow the prompts to blink and turn your head",
            onClick = onStart
        )
    }
}

@Composable
private fun VideoRecordTile(videoPath: String, error: String? = null, onRecord: () -> Unit) {
    val recorded = videoPath.isNotBlank()
    val borderColor by animateColorAsState(
        targetValue = if (recorded) OnboardingGold1 else greyButtonBackground,
        animationSpec = tween(300),
        label = "videoBorder"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onRecord)
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(OnboardingGold1.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (recorded) Icons.Filled.CheckCircle else Icons.Filled.Videocam,
                contentDescription = null,
                tint = OnboardingGold1,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (recorded) "Video recorded" else "Record a short selfie video",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimaryColor,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(Modifier.height(3.dp))
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(error, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE57373)))
        }
        Text(
            text = if (recorded) "Tap to record again" else "5–10 seconds, face clearly visible",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
//  5. Consent / declaration
// ---------------------------------------------------------------------------

@Composable
private fun ConsentCard(state: KycState, viewModel: KycViewModel) {
    SectionCard(
        index = 5,
        title = "Consent & declaration",
        subtitle = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.onEvent(KycEvent.ConsentToggled(!state.consentAccepted)) }
                .padding(2.dp),
            verticalAlignment = Alignment.Top
        ) {
            val boxColor by animateColorAsState(
                targetValue = if (state.consentAccepted) OnboardingGold1 else Color.Transparent,
                animationSpec = tween(200),
                label = "consentBox"
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(boxColor)
                    .border(
                        1.dp,
                        if (state.consentAccepted) OnboardingGold1 else greyButtonBackground,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.consentAccepted) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "I confirm the information and documents provided are accurate and belong to me, and I consent to their processing for identity verification in line with applicable AML/KYC regulations.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
//  Image picker tile (camera + gallery)
// ---------------------------------------------------------------------------

private enum class TileKind { DOCUMENT, SELFIE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePickerTile(
    label: String,
    hint: String,
    tileKind: TileKind,
    imagePath: String,
    onPicked: (String) -> Unit
) {
    val context = LocalContext.current
    var showSourceSheet by remember { mutableStateOf(false) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { context.uriToTempFilePath(it)?.let(onPicked) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) pendingCaptureUri?.let { context.uriToTempFilePath(it)?.let(onPicked) }
        pendingCaptureUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCaptureUri(context, "jpg")
            if (uri != null) {
                pendingCaptureUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    fun launchCamera() = cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

    val hasImage = imagePath.isNotBlank()
    val borderColor by animateColorAsState(
        targetValue = if (hasImage) OnboardingGold1 else greyButtonBackground,
        animationSpec = tween(300),
        label = "tileBorder"
    )

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            )
            if (hasImage) {
                Spacer(Modifier.size(6.dp))
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Uploaded",
                    tint = OnboardingGold1,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (tileKind == TileKind.SELFIE) 1.4f else 1.7f)
                .clip(RoundedCornerShape(16.dp))
                .background(AppBackgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable {
                    if (tileKind == TileKind.SELFIE) launchCamera() else showSourceSheet = true
                },
            contentAlignment = Alignment.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )
                FilledTileOverlay(onReplace = {
                    if (tileKind == TileKind.SELFIE) launchCamera() else showSourceSheet = true
                })
            } else {
                EmptyTileContent(tileKind = tileKind, hint = hint)
            }
        }
    }

    if (showSourceSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState,
            containerColor = AppBackgroundColor,
            dragHandle = null
        ) {
            SourcePickerSheet(
                onCamera = { showSourceSheet = false; launchCamera() },
                onGallery = { showSourceSheet = false; galleryLauncher.launch("image/*") }
            )
        }
    }
}

@Composable
private fun SourcePickerSheet(onCamera: () -> Unit, onGallery: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(greyButtonBackground)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            "Add a photo",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimaryColor,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(Modifier.height(16.dp))
        SourceOption(Icons.Filled.CameraAlt, "Take a photo", "Use your camera", onCamera)
        Spacer(Modifier.height(12.dp))
        SourceOption(
            Icons.Filled.PhotoLibrary,
            "Choose from gallery",
            "Pick an existing image",
            onGallery
        )
    }
}

@Composable
private fun SourceOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, greyButtonBackground, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OnboardingGold1.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = OnboardingGold1,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.size(14.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimaryColor,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun FilledTileOverlay(onReplace: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.6f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = OnboardingGold1,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = "Tap to replace",
            style = MaterialTheme.typography.labelMedium.copy(color = Color.White),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clickable(onClick = onReplace)
        )
    }
}

@Composable
private fun EmptyTileContent(tileKind: TileKind, hint: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OnboardingGold1.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = OnboardingGold1,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (tileKind == TileKind.SELFIE) "Take a selfie" else "Add photo",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimaryColor,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
//  Submit
// ---------------------------------------------------------------------------

@Composable
private fun SubmitButton(state: KycState, viewModel: KycViewModel) {
    val submitting = state.submitResponse is ResponseState.Loading
    val enabled = state.isFormValid && !submitting

    when {
        submitting -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(GradientBrush),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
        }

        enabled -> GoldGradientButton(
            label = "Submit for verification",
            onClick = { viewModel.onEvent(KycEvent.Submit) },
            modifier = Modifier.fillMaxWidth()
        )

        else -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(greyButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Complete all steps to submit",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/* ---------------------------------------------------------------------------
 ViewModel contract this screen expects (add any that are missing):

 KycState fields:
   fullName: String, dateOfBirth: String, nationality: String
   addressLine: String, city: String, postalCode: String, country: String
   proofOfAddressPath: String
   selectedDocTypeId: String?            // matches DocSpec.id in KycDocumentCatalog
   documentNumber: String
   frontImagePath / backImagePath / selfieImagePath: String
   livenessEnabled: Boolean              // toggle: SDK liveness vs recorded video
   livenessVerified: Boolean             // set true when the SDK session passes
   videoPath: String                     // recorded video temp path
   consentAccepted: Boolean
   isFormValid: Boolean                  // derive from all required steps + consent
   submitResponse: ResponseState<...>

 KycEvent cases:
   FullNameChanged, DateOfBirthChanged, NationalityChanged
   AddressLineChanged, CityChanged, PostalCodeChanged, CountryChanged
   ProofOfAddressPicked(path)
   DocumentTypeSelected(id: String), DocumentNumberChanged(value)
   FrontImagePicked / BackImagePicked / SelfieImagePicked (path)
   LivenessToggled(Boolean), StartLiveness, VideoRecorded(path)
   ConsentToggled(Boolean)
   Submit, DismissDialog(code)
--------------------------------------------------------------------------- */
