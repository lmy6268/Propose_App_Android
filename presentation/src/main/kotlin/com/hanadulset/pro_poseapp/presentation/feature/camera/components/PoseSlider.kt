package com.hanadulset.pro_poseapp.presentation.feature.camera.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.core.designsystem.component.CircularProgressBar
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons
import com.hanadulset.pro_poseapp.presentation.feature.camera.PoseSelectionItem
import com.hanadulset.pro_poseapp.presentation.feature.camera.PoseSelectionItem
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.PoseUIItem

const val RECOMMEND_POSE = "포즈 추천 중.."


//포즈 선택을 위한 슬라이더
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PoseSelectSlider(
    modifier: Modifier = Modifier,
    currentSelectedIdx: () -> Int,
    inputPosedDataList: () -> List<PoseUIItem>?,
    onSelectedPoseIndexEvent: (Int) -> Unit,
    itemTextColor: () -> Color
) {
    val scrollState = rememberLazyListState()
    val nowSelected = rememberUpdatedState(newValue = currentSelectedIdx())
    val rowWidth = LocalConfiguration.current.screenWidthDp.dp
    val poseItemSize = DpSize(80.dp, 80.dp)

    val textSize = 10.dp
    val flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(scrollState))
    val padding by rememberUpdatedState(newValue = (rowWidth - poseItemSize.width) / 2)
    val immutableList = rememberUpdatedState(newValue = inputPosedDataList())

    LaunchedEffect(immutableList.value) {
        scrollState.scrollToItem(nowSelected.value)
    }

    LaunchedEffect(nowSelected.value) {
        scrollState.animateScrollToItem(nowSelected.value)
    }

    immutableList.value.run {
        if (this != null) {
            LazyRow(
                modifier = modifier,
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(
                    horizontal = padding
                ),
                flingBehavior = flingBehavior,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(count = this@run.size, key = {
                    this@run[it].poseId
                }, itemContent = { idx ->
                    val poseItem = this@run[idx]
                    PoseSelectionItem(
                        modifier = Modifier.size(poseItemSize),
                        isSelected = idx == nowSelected.value,
                        imageUri = poseItem.imageUri,
                        poseIndex = idx,
                        onClickEvent = {
                            onSelectedPoseIndexEvent(idx)
                        },
                        poseSize = poseItemSize.height - textSize * 2,
                        textSize = textSize,
                        itemTextColor = itemTextColor()
                    )
                })
            }
        } else {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(poseItemSize.height),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressBar()
                    Text(
                        text = RECOMMEND_POSE,
                        textAlign = TextAlign.Center,
                        color = itemTextColor(),
                        fontFamily = CameraScreenButtons.pretendardFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
    }
}