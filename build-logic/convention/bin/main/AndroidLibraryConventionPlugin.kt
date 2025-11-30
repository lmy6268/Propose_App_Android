import com.android.build.api.dsl.LibraryExtension
import com.hanadulset.pro_poseapp.build_logic.convention.AppConfigure
import com.hanadulset.pro_poseapp.build_logic.convention.ConventionRes
import com.hanadulset.pro_poseapp.build_logic.convention.configureKotlinAndroid
import com.hanadulset.pro_poseapp.build_logic.convention.findPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.findVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure


/** Plugin For Library ( Core )
 *
 * */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(findPluginId(ConventionRes.Plugin.ANDROID_LIBRARY))
            pluginManager.apply(findPluginId(ConventionRes.Plugin.KOTLIN_ANDROID))
            extensions.configure<LibraryExtension> {
                lint.targetSdk = findVersion("targetSdk").toInt()
                configureKotlinAndroid(this)
            }
        }
    }
}