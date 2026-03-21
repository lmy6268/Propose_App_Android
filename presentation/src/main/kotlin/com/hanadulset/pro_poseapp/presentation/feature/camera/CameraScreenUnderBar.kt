package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.defaultButtonSize
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.galleryButtonSize
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.shutterButtonSize
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.CommonButtons
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.pose.model.PoseUIItem
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.lower.LowerButtons
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.lower.LowerButtons.ParticularZoomButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.lower.LowerButtons.ToggledButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.lower.PoseSelectSlider

object CameraScreenUnderBar {

    //하단바 구성
    //총 세 개의 층이고, 추천 층을 제외한 두 층 각각은 별도의 패딩이 필요하다.

    //버튼 사이즈
    val shutterButtonSize = 70.dp
    val defaultButtonSize = shutterButtonSize - 20.dp
    val galleryButtonSize = shutterButtonSize - 20.dp

    @Composable
    fun UnderBar(
        modifier: Modifier = Modifier,
        galleryImageUri: () -> Uri?,
        onPoseRecommendEvent: () -> Unit,
        onShutterClickEvent: () -> Unit,
        onGalleryButtonClickEvent: () -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
        lowerLayerPaddingBottom: Dp = 0.dp,
        zoomLevelState: () -> Float,
        userEdgeDetectionValue: () -> Boolean,
        systemEdgeDetectionValue: () -> Boolean,
        onSystemEdgeDetectionClicked: () -> Unit,
        onUserEdgeDetectionClicked: () -> Unit,
        isRecommendPoseEnabled: () -> Boolean
    ) {

        val galleryThumbUri by rememberUpdatedState(newValue = galleryImageUri)

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UpperLayer(
                modifier = Modifier.fillMaxWidth(),
                onUserEdgeDetectionClicked = onUserEdgeDetectionClicked,
                onZoomLevelChangeEvent = onZoomLevelChangeEvent,
                onFixedButtonClickEvent = onSystemEdgeDetectionClicked,
                userEdgeDetectionValue = userEdgeDetectionValue,
                systemEdgeDetectionValue = systemEdgeDetectionValue,
                zoomLevelState = zoomLevelState,
            )
            LowerLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = lowerLayerPaddingBottom),
                onShutterClickEvent = onShutterClickEvent,
                onGalleryButtonClickEvent = onGalleryButtonClickEvent,
                galleryImageUri = galleryThumbUri,
                isRecommendPoseEnabled = isRecommendPoseEnabled,
                onRecommendPoseEvent = onPoseRecommendEvent
            )
        }

    }

}


// 포즈 추천 , 따오기 -> On/Off , 줌레벨은 선택된 버튼이 돋보이도록 사이즈 조절
// 포즈 추천 버튼, 줌레벨 설정 버튼 , 따오기 버튼이 존재

@Composable
fun UpperLayer(
    modifier: Modifier = Modifier,
    systemEdgeDetectionValue: () -> Boolean,
    onUserEdgeDetectionClicked: () -> Unit,
    userEdgeDetectionValue: () -> Boolean,
    onZoomLevelChangeEvent: (Float) -> Unit,
    onFixedButtonClickEvent: () -> Unit = {},
    zoomLevelState: () -> Float
) {
    val edgeDetectorState by rememberUpdatedState(newValue = userEdgeDetectionValue)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        val fixedBtnValue by rememberUpdatedState(newValue = systemEdgeDetectionValue)
        //따오기 버튼
        ToggledButton(
            modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
            buttonSize = defaultButtonSize,
            onClickEvent = onUserEdgeDetectionClicked,
            buttonStatus = edgeDetectorState(),
            buttonText = "따오기",
            inActivatedColor = LocalColors.current.background100,
            buttonTextColor = LocalColors.current.textPrimary100
        )
        //줌 레벨 설정 버튼

        ZoomButtonRow(
            modifier = Modifier.wrapContentSize(),
            onClickEvent = onZoomLevelChangeEvent,
            zoomLevelState = zoomLevelState()
        )
        //고정
        LowerButtons.FixedButton(
            modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
            buttonSize = defaultButtonSize,
            onFixedButtonPressedEvent = onFixedButtonClickEvent,
            fixedBtnStatus = fixedBtnValue()
        )

    }


}

// 갤러리 버튼, 셔터 버튼, 고정 버튼이 위치
@Composable
fun LowerLayer(
    modifier: Modifier = Modifier,
    galleryImageUri: () -> Uri?,
    onRecommendPoseEvent: () -> Unit,
    onShutterClickEvent: () -> Unit = {},
    isRecommendPoseEnabled: () -> Boolean,
    onGalleryButtonClickEvent: () -> Unit = {},
) {


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //갤러리 이미지 버튼
        GalleryImageButton(
            galleryImageUri = galleryImageUri,
            buttonSize = galleryButtonSize,
            onClickEvent = onGalleryButtonClickEvent
        )

        //셔터
        LowerButtons.ShutterButton(
            buttonSize = shutterButtonSize
        ) { onShutterClickEvent() }


        CommonButtons.NormalButton(
            modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
            buttonSize = defaultButtonSize,
            buttonName = "포즈 추천 버튼",
            buttonText = "포즈",
            buttonTextColor = LocalColors.current.textPrimary100,
            colorTint = LocalColors.current.background100,
            onClick = onRecommendPoseEvent,
            buttonTextSize = 12
        )

    }
}


@Composable
private fun GalleryImageButton(
    modifier: Modifier = Modifier,
    galleryImageUri: () -> Uri?,
    buttonSize: Dp,
    defaultBackgroundColor: Color = Color(0x80FAFAFA),
    onClickEvent: () -> Unit,
) {

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current).data(galleryImageUri())
            .size(with(LocalDensity.current) { buttonSize.toPx().toInt() }) //현재 버튼의 크기만큼 리사이징한다.
            .build()
    )
    val mutableInteractionSource = remember { MutableInteractionSource() }

    Surface(
        shadowElevation = 2.dp,
        shape = CircleShape,
        modifier = Modifier.wrapContentSize(),
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "갤러리 이미지",
            modifier = modifier
                .size(buttonSize)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(color = defaultBackgroundColor)
                }
                .clickable(
                    interactionSource = mutableInteractionSource,
                    indication = CommonButtons.CustomIndication,
                ) {
                    onClickEvent()
                },
            contentScale = ContentScale.Crop,
        )
    }

}


@Composable
fun ZoomButtonRow(
    modifier: Modifier,
    selectedButtonScale: Float = 1.2F,
    defaultButtonSize: Dp = 40.dp,
    spaceByEachItems: Dp = 10.dp,
    onClickEvent: (Float) -> Unit,
    zoomLevelState: Float
) {
    val zoomLevel by rememberUpdatedState(newValue = zoomLevelState)

    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(
                shape = RoundedCornerShape(100.dp),
                color = LocalColors.current.textSecondary100.copy(alpha = 0.5F)
            )
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(
                spaceByEachItems, Alignment.CenterHorizontally
            )
        ) {
            listOf(1, 2).forEachIndexed { index, value ->
                ParticularZoomButton(
                    selected = (index + 1) == zoomLevel.toInt(),
                    defaultButtonSize = defaultButtonSize / selectedButtonScale,
                    buttonValue = value,
                    selectedButtonScale = selectedButtonScale
                ) {
                    onClickEvent(value.toFloat())
                }
            }
        }
    }



}


//포즈 추천 버튼을 누르면 나오는 버튼 배열
@Composable
fun ClickPoseBtnUnderBar(
    modifier: Modifier = Modifier,
    poseList: () -> List<PoseUIItem>?,
    galleryImageUri: () -> Uri?,
    initPoseItemScale: () -> Float = { 1F },
    currentSelectedPoseItemIdx: () -> Int,
    onRefreshPoseData: () -> Unit,
    onClickShutterBtn: () -> Unit,
    onGalleryButtonClickEvent: () -> Unit,
    onClickCloseBtn: () -> Unit,
    onSelectedPoseIndexEvent: (Int) -> Unit,
    onChangeScale: (Float) -> Unit,
    is16By9AspectRatio: () -> Boolean,
    maxScale: () -> Float,
) {
    BackHandler(onBack = onClickCloseBtn) //뒤로가기 버튼을 누르면 이전 화면으로 돌아감.


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val localColor = LocalColors.current
        val sliderBackgroundColor =
            rememberUpdatedState(newValue = if (is16By9AspectRatio()) localColor.background100 else Color.Unspecified)
        val backgroundColor =
            rememberUpdatedState(newValue = if (is16By9AspectRatio()) Color.Unspecified else localColor.background100)
        val itemTextColor = rememberUpdatedState(
            newValue = if (is16By9AspectRatio()) localColor.background100
            else localColor.textPrimary100
        )



        if (poseList() != null && currentSelectedPoseItemIdx() > 0) {
            val trackedPoseScaleValue =
                remember { mutableFloatStateOf(initPoseItemScale()) } //현재 상태의 스케일 값을 추적
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .padding(start = 10.dp),
                    painter = painterResource(id = R.drawable.icon_zoom_out),
                    contentDescription = "",
                    tint = sliderBackgroundColor.value
                )

                Slider(
                    colors = SliderDefaults.colors(
                        thumbColor = localColor.primaryBlue100,
                        activeTrackColor = localColor.primaryBlue100,
                        inactiveTrackColor = localColor.background80,
                        activeTickColor = localColor.primaryBlue100,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(width = (LocalConfiguration.current.screenWidthDp / 1.5).dp)
                        .height(50.dp),
                    value = trackedPoseScaleValue.floatValue,
                    steps = 10,
                    valueRange = 0.5F.rangeTo(2F),
                    onValueChange = {
                        it.coerceIn(
                            maximumValue = maxScale(), minimumValue = 0.5F
                        ).run {
                            trackedPoseScaleValue.floatValue = this
                            onChangeScale(this)
                        }
                    })

                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 10.dp),
                    painter = painterResource(id = R.drawable.icon_zoom_in),
                    contentDescription = "",
                    tint = sliderBackgroundColor.value

                )
            }

        } else {
            Spacer(
                modifier = Modifier.height(50.dp)
            )
        }
        //포즈 선택 할 수 있는 Row -> 선택된 포즈를 가지고 스케일 변경 진행
        PoseSelectSlider(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = backgroundColor.value)
                }
                .padding(top = 10.dp),
            currentSelectedIdx = currentSelectedPoseItemIdx,
            inputPosedDataList = poseList,
            onSelectedPoseIndexEvent = { onSelectedPoseIndexEvent(it) },
            itemTextColor = { itemTextColor.value })

        PoseSelectLowerMenu(
            modifier = Modifier
                .drawBehind {
                    drawRect(color = backgroundColor.value)
                }
                .padding(horizontal = 50.dp)
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            //창 닫는 버튼
            GalleryImageButton(
                galleryImageUri = galleryImageUri,
                buttonSize = galleryButtonSize,
                onClickEvent = onGalleryButtonClickEvent
            )

            //셔터 버튼
            LowerButtons.ShutterButton(
                onClickEvent = onClickShutterBtn, buttonSize = shutterButtonSize
            )

            //포즈 새로고침 버튼
            CommonButtons.NormalButton(
                modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
                buttonName = "포즈 새로고침",
                innerIconDrawableSize = defaultButtonSize / 3,
                colorTint = localColor.background100,
                innerIconDrawableId = R.drawable.refresh,
                onClick = { onRefreshPoseData() },
                buttonSize = defaultButtonSize,
                innerIconColorTint = localColor.textPrimary100
            )
        }
    }
}






@Composable
fun PoseSelectLowerMenu(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        content = content,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    )
}




//클릭 할 수 있는 포즈 아이템 카드
@Composable
fun PoseSelectionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    imageUri: Uri?,
    poseIndex: Int,
    poseSize: Dp,
    textSize: Dp,
    itemTextColor: Color,
    onClickEvent: () -> Unit
) {
    val colorTheme = LocalColors.current
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val unSelectedColor = colorTheme.background100
    val selectedColor = colorTheme.primaryBlue100
    val stateColor = if (isSelected) selectedColor else unSelectedColor
    val defaultModifier = Modifier
        .padding(2.dp)
        .wrapContentSize()
        .clickable(
            interactionSource = mutableInteractionSource, indication = rememberRipple(
                color = if (isSelected) selectedColor
                else unSelectedColor, bounded = true, radius = poseSize / 2
            )
        ) { onClickEvent() }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUri ?: R.drawable.impossible_icon).size(with(LocalDensity.current) {
                poseSize.toPx().toInt()
            }) //현재 버튼의 크기만큼 리사이징한다.
            .build()
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val checkedPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.selected_icon)
                .build()
        )

        Surface(
            shadowElevation = 4.dp,
            color = stateColor,
            shape = ShapeDefaults.Medium,
            modifier = Modifier.wrapContentSize()
        ) {
            Box(modifier = Modifier.wrapContentSize()) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(poseSize)
                            .zIndex(1F)
                    ) {
                        Image(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(poseSize / 2),
                            painter = checkedPainter,
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(color = LocalColors.current.background100)
                        )
                    }
                }
                Card(
                    modifier = defaultModifier
                ) {
                    Image(
                        modifier = Modifier
                            .background(color = stateColor)
                            .size(poseSize),
                        painter = painter,
                        contentDescription = "이미지",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(color = LocalColors.current.textPrimary100)
                    )
                }
            }
        }

        Text(
            text = if (imageUri == null) "없음" else "포즈 #$poseIndex",
            textAlign = TextAlign.Center,
            fontFamily = CommonButtons.pretendardFamily,
            fontWeight = FontWeight.Bold,
            fontSize = with(LocalDensity.current) { textSize.toSp() },
            color = itemTextColor,
            modifier = Modifier
                .padding(top = 3.dp)
                .width(poseSize)
                .wrapContentHeight()

        )
    }


}


//@Composable
//@Preview
//fun PreviewLowerLayer() {
//    LowerLayer(modifier = Modifier.fillMaxWidth(), onShutterClickEvent = {
//
//    }, onGalleryButtonClickEvent = {
//
//    }, galleryImageUri = null, disturbFromEdgeDetector = false
//    )
//}
//
//
//@Composable
//@Preview
//fun PreviewUnderBar() {
//    CameraScreenUnderBar.UnderBar(
//        galleryImageUri = null,
//        onPoseRecommendEvent = {},
//        onEdgeDetectEvent = { false },
//        onShutterClickEvent = { /*TODO*/ },
//        onGalleryButtonClickEvent = { /*TODO*/ },
//        onZoomLevelChangeEvent = {},
//        onFixedButtonClickEvent = {}
//    )
//}


@Composable
@Preview
fun PreviewSelector() {
    val poseList = listOf(
        PoseUIItem(
            poseId = 0, imageUri = null, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        ), PoseUIItem(
            poseId = 0, poseCat = 1
        )


    )
    ClickPoseBtnUnderBar(
        modifier = Modifier.fillMaxWidth(),
        poseList = { poseList },
        onSelectedPoseIndexEvent = {},
        onClickCloseBtn = {},
        onClickShutterBtn = {},
        onRefreshPoseData = {},
        currentSelectedPoseItemIdx = { 0 },
        galleryImageUri = { null },
        onGalleryButtonClickEvent = {

        },
        onChangeScale = {

        },
        is16By9AspectRatio = { true },
        maxScale = { 2F })
}

@Preview
@Composable
fun TestZoomRow() {
    ZoomButtonRow(modifier = Modifier.wrapContentSize(), onClickEvent = { }, zoomLevelState = 1F)

}
