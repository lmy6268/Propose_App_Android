import com.android.build.api.dsl.LibraryExtension
import com.hanadulset.pro_poseapp.build_logic.convention.ConventionRes
import com.hanadulset.pro_poseapp.build_logic.convention.findLibrary
import com.hanadulset.pro_poseapp.build_logic.convention.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(ConventionRes.Plugin.ANDROID_LIBRARY_CONVENTION)
                apply(ConventionRes.Plugin.HILT_CONVENTION)
            }
            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true
            }

            dependencies {
//                implementation(project(NameAlias.Path.CORE.UI))
//                implementation(project(NameAlias.Path.CORE.DESIGN_SYSTEM))
                implementation(findLibrary(ConventionRes.Library.COMPOSE_HILT_NAVIGATION))
                implementation(findLibrary(ConventionRes.Library.COMPOSE_VIEWMODEL))
                implementation(findLibrary(ConventionRes.Library.COMPOSE_RUNTIME))
            }
        }
    }
}