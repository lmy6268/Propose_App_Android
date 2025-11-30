import com.hanadulset.pro_poseapp.build_logic.convention.ConventionRes
import com.hanadulset.pro_poseapp.build_logic.convention.findLibrary
import com.hanadulset.pro_poseapp.build_logic.convention.findPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.implementation
import com.hanadulset.pro_poseapp.build_logic.convention.ksp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(findPluginId(ConventionRes.Plugin.KSP))
                apply(findPluginId(ConventionRes.Plugin.HILT))
            }
            dependencies {
                implementation(findLibrary(ConventionRes.Library.HILT_ANDROID))
                ksp(findLibrary(ConventionRes.Library.HILT_COMPILER))
            }
        }
    }

}