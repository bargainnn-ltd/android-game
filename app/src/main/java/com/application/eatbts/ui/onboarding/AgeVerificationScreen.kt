package com.application.eatbts.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.eatbts.R

@Composable
fun AgeVerificationScreen(
    onAgeVerified: () -> Unit,
    onUnderAgeExit: () -> Unit,
    termsUrl: String = "https://bargainn.io/terms-of-service",
    privacyUrl: String = "https://bargainn.io/privacy-policy",
    safetyUrl: String = "https://bargainn.io/privacy-policy",
) {
    var mm by remember { mutableStateOf("") }
    var dd by remember { mutableStateOf("") }
    var yyyy by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val bgBrush = Brush.verticalGradient(
        listOf(AgeGateColors.BackgroundDeep, AgeGateColors.Background, AgeGateColors.BackgroundDeep),
    )

    fun digitsOnly(maxLen: Int, current: String, new: String): String {
        if (new.length > maxLen) return current
        if (new.isEmpty()) return ""
        return if (new.all { it.isDigit() }) new else current
    }

    fun tryContinue() {
        formError = null
        if (!accepted) {
            formError = context.getString(R.string.age_error_terms)
            return
        }
        when (val r = validateDob(mm, dd, yyyy)) {
            DobValidationResult.Incomplete -> formError = context.getString(R.string.age_error_incomplete_dob)
            DobValidationResult.InvalidDate -> formError = context.getString(R.string.age_error_invalid_date)
            DobValidationResult.Under18 -> formError = context.getString(R.string.age_error_under_18)
            DobValidationResult.Valid -> onAgeVerified()
        }
    }

    fun tryEighteenPlusNoDob() {
        formError = null
        if (!accepted) {
            formError = context.getString(R.string.age_error_terms)
            return
        }
        onAgeVerified()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(bgBrush)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("age_verification_screen"),
    ) {
        AgeTopBar()
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.age_access_protocol),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AgeGateColors.MutedRed,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.age_hero_line1),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AgeGateColors.White,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.age_hero_line2),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = AgeGateColors.PrimaryPurple,
                letterSpacing = 0.5.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.age_hero_body),
            style = MaterialTheme.typography.bodyMedium,
            color = AgeGateColors.BodyGrey,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(20.dp))
        AgeFeatureRow(
            iconContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AgeGateColors.FeatureAdultIconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "E",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
            title = stringResource(R.string.age_feature_adult_title),
            subtitle = stringResource(R.string.age_feature_adult_desc),
        )
        Spacer(Modifier.height(12.dp))
        AgeFeatureRow(
            iconContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AgeGateColors.FeaturePrivacyIconBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = AgeGateColors.YellowAccent,
                        modifier = Modifier.size(22.dp),
                    )
                }
            },
            title = stringResource(R.string.age_feature_privacy_title),
            subtitle = stringResource(R.string.age_feature_privacy_desc),
        )
        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = AgeGateColors.SurfaceCard,
            tonalElevation = 2.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, AgeGateColors.PrimaryPurple.copy(alpha = 0.45f)),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.age_verify_identity_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AgeGateColors.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.age_enter_dob),
                    style = MaterialTheme.typography.labelMedium,
                    color = AgeGateColors.DimGrey,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DobField(
                        label = stringResource(R.string.age_label_month),
                        value = mm,
                        onValueChange = { v ->
                            formError = null
                            mm = digitsOnly(2, mm, v)
                        },
                        modifier = Modifier.weight(1f).testTag("age_dob_mm"),
                    )
                    DobField(
                        label = stringResource(R.string.age_label_day),
                        value = dd,
                        onValueChange = { v ->
                            formError = null
                            dd = digitsOnly(2, dd, v)
                        },
                        modifier = Modifier.weight(1f).testTag("age_dob_dd"),
                    )
                    DobField(
                        label = stringResource(R.string.age_label_year),
                        value = yyyy,
                        onValueChange = { v ->
                            formError = null
                            yyyy = digitsOnly(4, yyyy, v)
                        },
                        modifier = Modifier.weight(1.2f).testTag("age_dob_yyyy"),
                    )
                }
                formError?.let { err ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgeGateColors.MutedRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = {
                            accepted = it
                            formError = null
                        },
                        modifier = Modifier.testTag("age_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = AgeGateColors.PrimaryPurple,
                            uncheckedColor = AgeGateColors.DimGrey,
                            checkmarkColor = Color.White,
                        ),
                    )
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = stringResource(R.string.age_consent_prefix),
                            style = MaterialTheme.typography.bodySmall,
                            color = AgeGateColors.BodyGrey,
                        )
                        Row {
                            TextButton(
                                onClick = { uriHandler.openUri(termsUrl) },
                                modifier = Modifier.padding(0.dp),
                            ) {
                                Text(
                                    stringResource(R.string.link_terms),
                                    color = AgeGateColors.PrimaryPurple,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(" · ", color = AgeGateColors.BodyGrey, style = MaterialTheme.typography.bodySmall)
                            TextButton(
                                onClick = { uriHandler.openUri(privacyUrl) },
                                modifier = Modifier.padding(0.dp),
                            ) {
                                Text(
                                    stringResource(R.string.link_privacy),
                                    color = AgeGateColors.PrimaryPurple,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { tryContinue() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("age_continue"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgeGateColors.PrimaryPurple,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        stringResource(R.string.age_continue),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = AgeGateColors.DimGrey.copy(alpha = 0.4f),
                    )
                    Text(
                        text = stringResource(R.string.age_or_divider),
                        style = MaterialTheme.typography.labelSmall,
                        color = AgeGateColors.DimGrey,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = AgeGateColors.DimGrey.copy(alpha = 0.4f),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { tryEighteenPlusNoDob() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("age_enter"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgeGateColors.SurfaceCharcoal,
                        contentColor = AgeGateColors.White,
                    ),
                ) {
                    Text(
                        stringResource(R.string.age_i_am_18_older),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = { uriHandler.openUri(safetyUrl) }) {
                Text(
                    stringResource(R.string.age_footer_safety),
                    color = AgeGateColors.DimGrey,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(" · ", color = AgeGateColors.DimGrey)
            TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                Text(
                    stringResource(R.string.link_privacy).uppercase(),
                    color = AgeGateColors.DimGrey,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(" · ", color = AgeGateColors.DimGrey)
            TextButton(onClick = { uriHandler.openUri(termsUrl) }) {
                Text(
                    stringResource(R.string.age_footer_legal),
                    color = AgeGateColors.DimGrey,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Text(
            text = stringResource(R.string.age_footer_copyright),
            style = MaterialTheme.typography.labelSmall,
            color = AgeGateColors.DimGrey.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onUnderAgeExit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.age_not_18_exit),
                color = AgeGateColors.BodyGrey,
            )
        }
    }
}

@Composable
private fun AgeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = { }) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = stringResource(R.string.age_cd_menu),
                tint = AgeGateColors.White,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = AgeGateColors.PrimaryPurple,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AgeGateColors.SurfaceCharcoal,
                border = BorderStroke(1.dp, AgeGateColors.DimGrey.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = stringResource(R.string.age_cd_secure),
                        tint = AgeGateColors.MutedRed,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.age_secure_access),
                        style = MaterialTheme.typography.labelSmall,
                        color = AgeGateColors.White,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.age_cd_notifications),
                    tint = AgeGateColors.White,
                )
            }
        }
    }
}

@Composable
private fun AgeFeatureRow(
    iconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconContent()
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AgeGateColors.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AgeGateColors.DimGrey,
                letterSpacing = 0.3.sp,
            )
        }
    }
}

@Composable
private fun DobField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AgeGateColors.DimGrey,
            letterSpacing = 0.5.sp,
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgeGateColors.PrimaryPurple,
                unfocusedBorderColor = AgeGateColors.DimGrey.copy(alpha = 0.5f),
                focusedTextColor = AgeGateColors.White,
                unfocusedTextColor = AgeGateColors.White,
                cursorColor = AgeGateColors.PrimaryPurple,
                focusedContainerColor = AgeGateColors.BackgroundDeep,
                unfocusedContainerColor = AgeGateColors.BackgroundDeep,
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        )
    }
}
