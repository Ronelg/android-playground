import org.gradle.api.JavaVersion

object App {

    private const val major: Int = 1
    private const val minor: Int = 0
    private const val patch: Int = 9
    private const val suffix: String = "-dev"

    const val rootName: String = "DFM Test"

    const val name: String = "$major.$minor.$patch${suffix}"
    const val code: Int = major * 10000 + minor * 1000 + patch

    const val compileSdk: Int = 28
    const val targetSdk: Int = compileSdk
    const val minSdk: Int = 21

    const val applicationId: String = "com.worldturtlemedia.playground"

    val javaVersion: JavaVersion = JavaVersion.VERSION_1_8
}