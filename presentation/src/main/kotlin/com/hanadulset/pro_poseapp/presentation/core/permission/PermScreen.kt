package com.hanadulset.pro_poseapp.presentation.core.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalTypography

object PermScreen {
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    //권한 설정화면
    fun PermScreen(
        multiplePermissionsState: MultiplePermissionsState,
        permissionAllowed: () -> Unit
    ) {
        LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
            if (multiplePermissionsState.allPermissionsGranted) {
                permissionAllowed()
            }
        }

        Screen(
            //사용자가 권한 요청 이벤트를 보낸 경우
            onClickToReqPermissionEvent = {
                if (multiplePermissionsState.allPermissionsGranted.not()) {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }

    @Composable
    fun Screen(
        onClickToReqPermissionEvent: () -> Unit
    ) {
        val localTypography = LocalTypography.current
        val localColors = LocalColors.current

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = localColors.primaryBlue100
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "프로_포즈(Pro_Pose) 서비스를 이용하기 위해\n" +
                            "접근 권한의 허용이 필요합니다.",
                    style = localTypography.heading02,
                    color = localColors.textPrimary100
                )
                Column(
                    modifier = Modifier
                        .background(
                            color = localColors.background100,
                            shape = RoundedCornerShape(size = 20.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "[정보통신망 이용촉진 및 정보보호 등에 관한 법률]에 맞추어\n서비스에 꼭 필요한 항목만 필수 접근하고 있습니다. ",
                        style = localTypography.sub02.copy(
                            textAlign = TextAlign.Center,
                            color = localColors.textPrimary100
                        )
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "카메라",
                            style = localTypography.heading02.copy(
                                textAlign = TextAlign.Start,
                                color = localColors.textPrimary100
                            )
                        )
                        Text(
                            text = "- 사진을 촬영하기 위한 권한",
                            style = localTypography.sub02.copy(
                                textAlign = TextAlign.Start,
                                color = localColors.textPrimary100
                            )
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "저장소",
                            style = localTypography.heading02.copy(
                                textAlign = TextAlign.Start,
                                color = localColors.textPrimary100
                            )
                        )
                        Text(
                            text = "- 사진을 저장하기 위한 권한" +
                                    "\n- 갤러리에서 사진을 볼 수 있는 권한",
                            style = localTypography.sub02.copy(
                                textAlign = TextAlign.Start,
                                color = localColors.textPrimary100
                            )
                        )
                    }
                }



                Button(
                    onClick = onClickToReqPermissionEvent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = localColors.background100
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "권한 설정하기",
                        Modifier
                            .padding(vertical = 10.dp, horizontal = 30.dp),
                        style = localTypography.heading02,
                        color = localColors.textPrimary100
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun PreviewScreen() {
    PermScreen.Screen {

    }
}