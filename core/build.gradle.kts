dependencies {
    implementation(libs.bundles.ktor)

    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging.jvm)

    testImplementation(kotlin("test"))
}
