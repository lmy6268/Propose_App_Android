import com.hanadulset.pro_poseapp.build_logic.convention.ConventionRes
import com.hanadulset.pro_poseapp.build_logic.convention.configureKotlin
import com.hanadulset.pro_poseapp.build_logic.convention.findPluginId
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(findPluginId(ConventionRes.Plugin.KOTLIN_JVM))
            configureKotlin<KotlinJvmProjectExtension>()

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}
