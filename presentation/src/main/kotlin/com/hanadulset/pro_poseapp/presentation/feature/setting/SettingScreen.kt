package com.hanadulset.pro_poseapp.presentation.feature.setting


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalTypography
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.SettingBoxItem
import com.hanadulset.pro_poseapp.presentation.component.SettingBoxItemWithToggle
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.UserUIItem

object SettingScreen {
    @Composable
    fun Screen(
        modifier: Modifier = Modifier,
        userSet: UserUIItem,
        onSaveUserSet: (UserUIItem) -> Unit,
        onBackPressed: () -> Unit
    ) {
        val setState = remember {
            mutableStateOf(userSet)
        }
        BackHandler(
            enabled = true
        ) {
            onBackPressed()
        }
        val ossLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(), onResult = {})
        val privacyLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(), onResult = {})
        val context = LocalContext.current
        val iconPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.app_icon_rounded_without_background)
                .placeholder(R.drawable.app_icon_rounded_without_background).build()
        )

        val versionName = (context.packageManager.getPackageInfo(
            context.packageName, 0
        )?.versionName)

        val verticalScrollState = rememberScrollState()
        LaunchedEffect(key1 = setState.value) {
            setState.value.run {
                onSaveUserSet(
                    UserUIItem(isCompOn, isPoseOn, poseCnt)
                )
            }
        }

        Surface(
            color = LocalColors.current.primaryBlue100,
            modifier = modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .align(Alignment.Center)
                            .width(240.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    shape = CircleShape,
                                    color = Color.White
                                )
                        ) {
                            Image(
                                painter = iconPainter,
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.Center),
                                contentDescription = "앱 아이콘 "
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "프로_포즈",
                                style = LocalTypography.current.heading01
                            )
                            Text(
                                text = "V $versionName",
                                style = LocalTypography.current.sub01
                            )
                        }
                    }

                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 150.dp),
                        text = "Copyright 2023. 하나, 둘, 셋  All rights reserved.",
                        style = LocalTypography.current.sub02
                    )
                }


                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = LocalColors.current.background100
                    ),
                    modifier = Modifier
                        .weight(2F)
                        .fillMaxWidth()
                        .verticalScroll(
                            verticalScrollState
                        ),
                    shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(
                            30.dp, Alignment.Top
                        )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "앱 설정",
                                style = LocalTypography.current.heading01
                            )
                            HorizontalDivider()
                        }
                        SettingBoxItemWithToggle(
                            modifier = Modifier.fillMaxWidth(),
                            innerText = "구도 추천",
                            isToggled = { setState.value.isCompOn },
                            onToggleEvent = {
                                setState.value = setState.value.copy(isCompOn = it)
                            })
                        SettingBoxItemWithToggle(
                            modifier = Modifier.fillMaxWidth(),
                            innerText = "포즈 추천",
                            isEnabled = { setState.value.isCompOn },
                            isToggled = { setState.value.isPoseOn },
                            onToggleEvent = {
                                setState.value = setState.value.copy(isPoseOn = it)
                            })

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "앱 정보",
                                style = LocalTypography.current.heading01
                            )
                            HorizontalDivider()
                        }
                        SettingBoxItem(
                            modifier = Modifier.fillMaxWidth(),
                            innerText = "오픈 소스 라이센스",
                            onClick = {
                                ossLauncher.launch(
                                    Intent(
                                        context.applicationContext,
                                        OssLicensesMenuActivity::class.java
                                    )
                                )
                            })
                        SettingBoxItem(
                            modifier = Modifier.fillMaxWidth(),
                            innerText = "개인정보 처리 방침",
                            onClick = {
                                privacyLauncher.launch(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://sites.google.com/view/privacyhanadulset/%ED%99%88/privacy".toUri()
                                    )
                                )
                            })
                        SettingBoxItem(
                            modifier = Modifier.fillMaxWidth(), innerText = "문의", onClick = {
                                sendEmailToAdmin(
                                    privacyLauncher, versionName = versionName
                                )
                            })

                    }
                }
            }


        }
    }

    @SuppressLint("QueryPermissionsNeeded", "UseKtx")
    private fun sendEmailToAdmin(
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>, versionName: String?
    ) = kotlin.runCatching {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_EMAIL, SettingText.Email.EMAIL_RECEIVER)
            putExtra(Intent.EXTRA_SUBJECT, SettingText.Email.EMAIL_TITLE)
            putExtra(Intent.EXTRA_TEXT, SettingText.Email.makeEmailContent(versionName))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            selector = SettingText.Email.EMAIL_SELECTOR
        }
        launcher.launch(emailIntent)
    }.getOrNull()

    private object SettingText {
        object Email {
            const val EMAIL_TITLE = "프로_포즈 사용 관련 문의"
            val EMAIL_RECEIVER = arrayOf("lmy6268@gmail.com")
            val EMAIL_SELECTOR = Intent(Intent.ACTION_SENDTO).apply {
                setDataAndType(
                    "mailto:".toUri(),
                    "message/rfc822"
                )
            }

            fun makeEmailContent(versionName: String?) = """
                App Version : $versionName
                Device : ${Build.MANUFACTURER} ${Build.PRODUCT}
                Android(SDK) : ${Build.VERSION.RELEASE}
            """.trimIndent()

        }

    }

}


@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
@Composable
private fun TestSettingScreen() {
    SettingScreen.Screen(userSet = UserUIItem(true, true), onSaveUserSet = {

    }, onBackPressed = {

    })
}