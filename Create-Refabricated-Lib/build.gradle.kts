version = properties["version"]!!
group = "${rootProject.properties["maven_group"]}.${rootProject.properties["archives_base_name"]}"

base {
	archivesBaseName = properties["archives_base_name"] as String
}

repositories {
    maven("https://mod-buildcraft.com/maven/") {
        name = "BuildCraft"
    }

	maven("https://jitpack.io/") {
		name = "Jitpack"

		content {
			// So Gradle doesn't spend time searching JitPack for other deps
			includeGroup("com.github.PepperCode1")
		}
	}
}

dependencies {
    val lba_version: String by rootProject
	val registrate_version: String by project
	val conrad_version: String by rootProject

	// Javax Annotations
	implementation("com.google.code.findbugs", "jsr305", "3.0.2")

    // LibBlockAttributes
    modImplementation("alexiil.mc.lib", "libblockattributes-all", lba_version)
    //modImplementation("alexiil.mc.lib", "libblockattributes-items", lba_version)
    //modImplementation("alexiil.mc.lib", "libblockattributes-fluids", lba_version)

	// Registrate
	modImplementation("com.github.PepperCode1", "Registrate-Fabric", registrate_version)
	include("com.github.PepperCode1", "Registrate-Fabric", registrate_version)
}

loom.accessWidener("src/main/resources/create_lib.accesswidener")
