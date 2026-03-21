plugins {
    alias(libs.plugins.propose.jvm.library)
    alias(libs.plugins.google.devtool.ksp)
}

dependencies {
    implementation(libs.hilt.core)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}