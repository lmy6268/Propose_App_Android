import com.android.build.api.dsl.ApplicationExtension
import com.hanadulset.pro_poseapp.build_logic.convention.AppConfigure
import com.hanadulset.pro_poseapp.build_logic.convention.ConventionRes
import com.hanadulset.pro_poseapp.build_logic.convention.configureKotlinAndroid
import com.hanadulset.pro_poseapp.build_logic.convention.findPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.findVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** 안드로이드 앱의 기본적인 의존성을 제공하는 플러그인
 *
 *
 * */

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(findPluginId(ConventionRes.Plugin.ANDROID_APPLICATION))
                apply(findPluginId(ConventionRes.Plugin.KOTLIN_ANDROID))
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = ConventionRes.Library.APPLICATION_ID
                    versionName = findVersion("versionName")
                    versionCode = findVersion("versionCode").toInt()
                }

                defaultConfig.targetSdk = findVersion("targetSdk").toInt()
                configureKotlinAndroid(this)
            }

        }
    }
}