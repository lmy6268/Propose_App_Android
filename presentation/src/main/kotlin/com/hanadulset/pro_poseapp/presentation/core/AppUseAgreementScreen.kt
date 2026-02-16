package com.hanadulset.pro_poseapp.presentation.core

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalTypography
import androidx.core.net.toUri

object AppUseAgreementScreen {
    @Composable
    fun AppUseAgreementScreen(
        onSuccess: () -> Unit
    ) {
        val iconPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.right_arrow)
                .placeholder(R.drawable.right_arrow).build()
        )
        val localTypography = LocalTypography.current
        val colors = LocalColors.current
        val privacyLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
                onResult = {})
        val isChecked = remember {
            mutableStateOf(false)
        }
        val buttonSize = 15.dp
        val agreementUrl = stringResource(id = R.string.agreement_url)

        Surface(
            modifier = Modifier.fillMaxSize(), color = colors.primaryBlue100
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(id = R.string.agreement_title),
                    style = localTypography.heading01,
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .background(
                            color = Color.White, shape = RoundedCornerShape(size = 20.dp)
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 50.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(id = R.string.agreement_welcome),
                        style = localTypography.sub02.copy(
                            textAlign = TextAlign.Start,
                            lineHeight = LocalDensity.current.run { (buttonSize + 5.dp).toSp() }
                        )
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Checkbox(
                            modifier = Modifier.size(buttonSize),
                            checked = isChecked.value,
                            onCheckedChange = {
                                isChecked.value = it
                            })
                        Text(
                            text = stringResource(id = R.string.agreement_required),
                            style = localTypography.heading02,
                            fontSize = LocalDensity.current.run {
                                buttonSize.toSp()
                            }
                        )
                        Text(
                            text = stringResource(id = R.string.agreement_service_terms),
                            style = localTypography.sub02,
                            fontSize = LocalDensity.current.run {
                                (buttonSize - 2.dp).toSp()
                            }
                        )
                        Icon(painter = iconPainter,
                            contentDescription = "",
                            modifier = Modifier
                                .size(buttonSize + 5.dp)
                                .clickable {
                                    privacyLauncher.launch(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            agreementUrl.toUri()
                                        )
                                    )
                                })
                    }
                }

                Button(
                    enabled = isChecked.value,
                    onClick = onSuccess,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f)
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.agreement_button),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 30.dp),
                        style = localTypography.heading02.copy(color = colors.primaryBlue100)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewScreen() {
    AppUseAgreementScreen.AppUseAgreementScreen {

    }
}
