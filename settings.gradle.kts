pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		maven("https://server.bbkr.space/artifactory/libs-release/") {
			name = "Cotton"
		}
		gradlePluginPortal()
	}
}

rootProject.name = "Create-Refabricated"
include("Create-Refabricated-Lib")
include("Style-Checks")
