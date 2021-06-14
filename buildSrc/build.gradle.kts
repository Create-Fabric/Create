plugins {
	`java`
}

repositories {
	jcenter()
	mavenCentral()
	maven("https://maven.fabricmc.net/")
	maven("https://jitpack.io/")
	maven("https://maven.shedaniel.me/")
}

dependencies {
	implementation("net.fabricmc", "fabric-loom", "0.7-SNAPSHOT")
	implementation("com.github.PepperCode1", "mcp-tiny", "1bde92310cd80f0d834403d40219a2a06be431da")
}
