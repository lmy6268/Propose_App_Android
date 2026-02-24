package com.hanadulset.pro_poseapp.ui.core

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalTypography
import com.hanadulset.pro_poseapp.ui.R
import kotlin.math.roundToInt

object CustomDialog {
    @Composable
    fun CustomAlertDialog(
        modifier: Modifier = Modifier,
        dialogTitle: String,
        subTitle: String,
        subTitleAdd: String = "",
        dismissText: String = stringResource(id = R.string.cancel),
        confirmText: String = stringResource(id = R.string.ok),
        onDismissRequest: () -> Unit,
        onConfirmRequest: () -> Unit
    ) {
        val typography = LocalTypography.current
        val colors = LocalColors.current

        val mainStyle = typography.heading02.copy(
            textAlign = TextAlign.Start
        )
        val subStyle = typography.sub02.copy(
            textAlign = TextAlign.Start
        )
        val buttonSize = DpSize(150.dp, 55.dp)

        Surface(
            modifier = modifier.wrapContentSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                Modifier.padding(20.dp, 30.dp, 20.dp, 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = dialogTitle, style = mainStyle
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = subTitle, style = subStyle
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (subTitleAdd.isNotEmpty()) {
                    Text(
                        text = subTitleAdd, style = subStyle
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DialogButton(
                        buttonText = dismissText,
                        buttonSize = buttonSize,
                        backgroundColor = colors.background100,
                        textStyle = typography.sub01.copy(fontWeight = FontWeight.Light),
                        onClick = onDismissRequest
                    )
                    DialogButton(
                        buttonText = confirmText,
                        buttonSize = buttonSize,
                        backgroundColor = colors.primaryBlue100,
                        textStyle = typography.sub01.copy(fontWeight = FontWeight.Bold),
                        onClick = onConfirmRequest
                    )
                }
            }
        }
    }

    @Composable
    private fun DialogButton(
        modifier: Modifier = Modifier,
        buttonText: String,
        buttonSize: DpSize,
        backgroundColor: Color,
        textStyle: TextStyle,
        onClick: () -> Unit
    ) {
        Surface(
            Modifier.wrapContentSize(),
            shadowElevation = 2.dp,
            shape = ShapeDefaults.Medium
        ) {
            Box(
                modifier = modifier
                    .background(
                        shape = RoundedCornerShape(12.dp), color = backgroundColor
                    )
                    .size(buttonSize)
                    .clickable(onClick = onClick), contentAlignment = Alignment.Center
            ) {
                Text(
                    buttonText,
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun DownloadAlertDialog(
        isDownload: Boolean,
        totalSize: Long,
        modifier: Modifier = Modifier,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        val actionText = stringResource(
            id = if (isDownload) R.string.download_action_download else R.string.download_action_update
        )
        val sizeMb = (totalSize / 1e+6).roundToInt()

        CustomAlertDialog(
            modifier = modifier,
            dialogTitle = stringResource(id = R.string.download_title_format, actionText, sizeMb),
            subTitle = stringResource(id = R.string.download_subtitle),
            dismissText = stringResource(
                id = if (isDownload) R.string.download_dismiss_exit else R.string.download_dismiss_later
            ),
            confirmText = stringResource(id = R.string.ok),
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest
        )
    }

    @Composable
    fun InternetConnectionDialog(
        modifier: Modifier = Modifier,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        CustomAlertDialog(
            modifier = modifier,
            dialogTitle = stringResource(id = R.string.internet_error_title),
            subTitle = stringResource(id = R.string.internet_error_subtitle),
            subTitleAdd = stringResource(id = R.string.internet_error_subtitle_add),
            dismissText = stringResource(id = R.string.go_to_settings),
            confirmText = stringResource(id = R.string.retry),
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest
        )
    }

    @Composable
    fun AppUpdateDialog(
        modifier: Modifier = Modifier,
        noticeText: String,
        mustUpdate: Boolean,
        versionName: String,
        onConfirmRequest: () -> Unit,
        onDismissRequest: () -> Unit
    ) {
        CustomAlertDialog(
            modifier = modifier,
            dialogTitle = stringResource(id = R.string.update_title),
            subTitle = stringResource(id = R.string.update_subtitle_format, versionName),
            subTitleAdd = noticeText,
            dismissText = stringResource(id = if (mustUpdate) R.string.close else R.string.later),
            confirmText = stringResource(id = R.string.update_action_confirm),
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun TestDownloadAlert() {
    CustomDialog.DownloadAlertDialog(isDownload = false, totalSize = 100900200, onConfirmRequest = {
        Log.d("Hello", "clickConfirm")
    }, onDismissRequest = {
        Log.d("Hello", "clickStop")
    })
}
