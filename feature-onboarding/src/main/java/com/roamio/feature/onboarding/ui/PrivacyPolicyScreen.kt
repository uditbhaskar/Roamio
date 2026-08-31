package com.roamio.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roamio.core.R as CoreR
import com.roamio.feature.onboarding.R

private val PrivacyFont = FontFamily(
    Font(CoreR.font.poppins_regular, FontWeight.Normal),
    Font(CoreR.font.poppins_medium, FontWeight.Medium),
    Font(CoreR.font.poppins_extrabold, FontWeight.ExtraBold),
)

/**
 * Scrollable privacy policy for Roamio.
 *
 * @param onBack Returns to onboarding.
 * @author udit
 */
@Composable
fun PrivacyPolicyScreenContent(
    onBack: () -> Unit,
) {
    val sage = colorResource(CoreR.color.roamio_sage)
    val forest = colorResource(CoreR.color.roamio_forest)
    val muted = colorResource(CoreR.color.roamio_muted)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sage)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.privacy_cd_back),
                tint = forest,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.privacy_title),
                color = forest,
                fontFamily = PrivacyFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                lineHeight = 38.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.privacy_updated),
                color = muted,
                fontFamily = PrivacyFont,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrivacySection(
                title = stringResource(R.string.privacy_section_collect_title),
                body = stringResource(R.string.privacy_section_collect_body),
                forest = forest,
                muted = muted,
            )
            PrivacySection(
                title = stringResource(R.string.privacy_section_use_title),
                body = stringResource(R.string.privacy_section_use_body),
                forest = forest,
                muted = muted,
            )
            PrivacySection(
                title = stringResource(R.string.privacy_section_third_party_title),
                body = stringResource(R.string.privacy_section_third_party_body),
                forest = forest,
                muted = muted,
            )
            PrivacySection(
                title = stringResource(R.string.privacy_section_choices_title),
                body = stringResource(R.string.privacy_section_choices_body),
                forest = forest,
                muted = muted,
            )
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    body: String,
    forest: Color,
    muted: Color,
) {
    Text(
        text = title,
        color = forest,
        fontFamily = PrivacyFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = body,
        color = muted,
        fontFamily = PrivacyFont,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(20.dp))
}

/**
 * Root composable for the privacy policy route.
 *
 * @param onBack Pops back to onboarding.
 * @author udit
 */
@Composable
fun PrivacyPolicyScreenRoot(
    onBack: () -> Unit,
) {
    PrivacyPolicyScreenContent(onBack = onBack)
}

@Preview(name = "Privacy", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PrivacyPolicyScreenContentPreview() {
    MaterialTheme {
        PrivacyPolicyScreenContent(onBack = {})
    }
}
