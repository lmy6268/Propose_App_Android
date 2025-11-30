import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.hanadulset.pro_poseapp.build_logic.convention.ConventionRes
import com.hanadulset.pro_poseapp.build_logic.convention.findLibrary
import com.hanadulset.pro_poseapp.build_logic.convention.findPluginId
import com.hanadulset.pro_poseapp.build_logic.convention.implementation
import com.hanadulset.pro_poseapp.build_logic.convention.ksp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(findPluginId(ConventionRes.Plugin.ROOM))
                apply(findPluginId(ConventionRes.Plugin.KSP))
            }
            extensions.configure<KspExtension> {
                arg("room.generateKotlin", "true")
            }

            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                implementation(findLibrary(ConventionRes.Library.ROOM_KTX))
                implementation(findLibrary(ConventionRes.Library.ROOM_RUNTIME))
                ksp(findLibrary(ConventionRes.Library.ROOM_COMPILER))
            }


        }
    }
}