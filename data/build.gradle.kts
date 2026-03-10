plugins {
    alias(libs.plugins.propose.android.library)
    alias(libs.plugins.propose.android.hilt)
    id("kotlinx-serialization")
    alias(libs.plugins.propose.android.room)
//    // Add the Google services Gradle plugin
//    id 'com.google.gms.google-services'
}
android {
    namespace = "com.hanadulset.pro_poseapp.data"
}

dependencies {
    implementation(libs.bundles.android.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)

    implementation(projects.domain)
    implementation(projects.opencv)
    implementation(projects.core.db)

    implementation(libs.bundles.pytorch)
    implementation(libs.cameraX.core)
    implementation(libs.bundles.aws.s3)

    // Open csv file
    implementation(libs.opencsv)

    implementation(libs.bundles.ktor)

    implementation(libs.androidx.datastore)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.pose.detection.accurate)

    implementation(libs.androidx.concurrent.futures)

    implementation(libs.zip4j)

}