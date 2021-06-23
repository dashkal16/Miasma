# Miasma Minecraft Mod
Copyright © 2021 Dashkal <dashkal@darksky.ca>

This is the source code repository for the Miasma mod for Minecraft.

This project is built using Forge Gradle.

## Build Instructions
To generate a mod jar, run `./gradlew build`

When finished the jars will be placed in `build/libs/`:
* `miasma-${version}.jar`
  * The main mod jar.
* `miasma-${version}-integration.jar`
  * The integration mod jar.
* `miasma-${version}-api.jar`
  * A jar with only the API classes and sources.
  * Suitable as a compile time dependency.
* `miasma-${version}-sources.jar`
  * A jar containing complete mod sources.

`${version}` is defined in `gradle.properties`.

## Organization
The mod is organized into the following modules:
* main
  * The core Miasma mod itself
* api
  * The public API other mods may use to interact with Miasma
* lib
  * Utility code used by other modules
* integration
  * Companion mod that provides integrations with other minecraft mods
  * Integrated Mods:
    * Curios
      * Curio slots will be checked for the IMiasmaModifier capability
* testmod
  * Dev only testing mod that provides a few test items
    * Protection Ring (Curios)
    * Protection Helmet
    * Event handlers that react when holding a stick, iron ingot, or gold ingot
* generated
  * Unused
* test
  * Unused

## Depending on Miasma

**_MIASMA IS NOT YET AVAILABLE. THIS SECTION DISCUSSES HOW TO OBTAIN IT AFTER RELEASE._**

### Cursemaven

Miasma will be available via Cursemaven (https://www.cursemaven.com/).

Assuming the use of Forge Gradle, depend on Miasma with the following:

Add the following to repositories:
```groovy
repositories {
    maven {
        url "https://www.cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}
```

Add the following to dependencies:
```groovy
dependencies {
    compileOnly fg.deobf("curse.maven:miasma-${projectId}:${fileId_api}")
    runtimeOnly fg.deobf("curse.maven:miasma-${projectId}:${fileId}")
}
```

`${projectId}` may be found on CurseForge in the right-hand panel under "About Project".

`${fileId}` may be found on CurseForge by checking for the number of the file url. Be sure to find the correct ID for
the API jar as opposed to the main mod Jar. The key is to depend on the API jar at compile time, which will avoid
accidentally accessing internal classes, and the full mod at runtime.
