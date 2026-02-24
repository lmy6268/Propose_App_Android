package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import android.util.Size
import android.util.SizeF

// 이미지 저장 등과 같은 처리 담당
interface ImageProcessDataSource {
    /**
     * 입력된 비트맵 이미지에서 외곽선(Contour)을 추출하여 새로운 비트맵으로 반환합니다.
     *
     * [개선된 처리 방식]
     * 1. Grayscale 변환
     * 2. Bilateral Filter: 질감(의류 주름, 벽면 등)과 노이즈를 뭉개면서 주요 외곽선의 선명도는 유지 (d=5, sigma=75.0)
     * 3. Canny Edge Detection: 부드러워진 이미지에서 단일 픽셀 두께의 깔끔한 주요 선만 추출 (threshold: 40.0, 120.0)
     * 4. Morphological Closing: 끊어진 선들을 연결하고 미세한 노이즈 점들을 제거하여 드로잉 느낌 강화
     *
     * (기존에는 Canny 결과물에 findContours 후 drawContours를 수행하여 질감까지 전부 그려지는 문제가 있었음)
     *
     * @param bitmap 원본 형태의 안드로이드 비트맵 객체
     * @return 깔끔한 외곽선이 마스킹된 B&W 비트맵 객체
     */
    fun getFixedImage(bitmap: Bitmap): Bitmap

    fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: org.opencv.core.Size): Bitmap

    /** 호모그래피(Homography) 기반 고성능 트래킹 카메라의 모든 변화(이동, 회전, 줌)를 계산하여 Anchor를 고정합니다. */
    suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF?

    fun stopToUseOpticalFlow()

    /**
     * 입력된 비트맵 이미지의 색상을 반전(Invert)시킵니다. OpenCV의 bitwise_not SIMD 연산을 사용하여 실시간 카메라 프레임에서도 CPU/메모리 부하
     * 없이 즉각적으로 어두운 화면을 밝게(또는 그 반대로) 반전시킵니다.
     *
     * @param bitmap 색상을 반전시킬 원본 비트맵
     * @return 색상이 반전된 새로운 비트맵
     */
    // fun invertImageColors(bitmap: Bitmap): Bitmap
}
