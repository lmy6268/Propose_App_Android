plugins {
    alias(libs.plugins.propose.android.library)
    alias(libs.plugins.propose.android.hilt)
    alias(libs.plugins.propose.android.room)
}

android {
    namespace = "com.hanadulset.pro_poseapp.core.db"
}

dependencies {
    implementation(libs.bundles.android.core)
    implementation(libs.kotlinx.coroutines.android)
}
