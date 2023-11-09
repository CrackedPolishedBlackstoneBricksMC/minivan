# minivan

Bare-minimum Gradle plugin for putting vanilla Minecraft, with official names, on the compilation classpath.

`minivan` is much smaller and much less feature-rich than [VanillaGradle](https://github.com/SpongePowered/VanillaGradle/) (hence the name). `minivan` makes *no* attempt to provide "run configs", reobfuscation, asset downloading, native library downloading, `genSources`/`decompile`, access widening, good error messages...

`minivan` is just one of the lower-level supporting bricks in your Minecraft project. The intended audience is people writing [jaredlll08/MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) -style mods, who need something to fill out the compilation classpath on their `Common`/`Xplat` subproject, but don't need much else.

# Usage

## Quick start

<details><summary>Sample buildscript</summary>

Your (sub)project's `build.gradle`

```gradle
buildscript {
	repositories {
		mavenCentral()
		maven { url "https://maven.fabricmc.net/" }
		maven { url "https://repo.sleeping.town/" }
	}
	dependencies {
		classpath "agency.highlysuspect:minivan:0.4"
	}
}

apply plugin: "java"
apply plugin: "agency.highlysuspect.minivan"

minivan {
	version("1.20.2")
}
```

</details>

<details><summary>Sample buildscript using "plugins dsl"</summary>

Your root project's `settings.gradle`:

```gradle
pluginManagement {
	repositories {
		gradlePluginPortal() //i think
		maven { url "https://maven.fabricmc.net/" }
		maven { url "https://repo.sleeping.town/" }
	}
}
```

Your (sub)project's `build.gradle`:

```gradle
plugins {
	id "java"
	id "agency.highlysuspect.minivan" version "0.4"
}

minivan {
	version("1.20.2")
}
```

</details>

Either of these buildscripts will cause the Minecraft 1.20.2 client and server to be downloaded, remapped to official names, merged, and stuck onto the `compileOnly` configuration along with all its dependencies (LWJGL, etc). This'll happen in `afterEvaluate`. See `./demo` for a worked example.

## Nuts and bolts

Using `version` is optional. For a lower-level imperative experience, try the `minivan.getMinecraft` function (available since `0.2`) instead:

```gradle
//this object is a `agency.highlysuspect.minivan.prov.MinecraftProvider.Result`:
def mc = minivan.getMinecraft("1.20.2")

//java.nio.Path to the "minecraft, remapped and merged" jar on your computer
println("merged minecraft jar: ${mc.minecraft}")

//List<String>, maven-style coordinates to Minecraft's dependencies
//(psst- the plugin automatically adds Mojang's maven and mavenCentral, so these should just work) 
println("maven dependency count: ${mc.dependencies.size()}")

//let's add Minecraft to the compileOnly classpath manually:
project.dependencies.add("compileOnly", project.files(mc.minecraft))
mc.dependencies.forEach { project.dependencies.add("compileOnly", it) }
```

`getMinecraft` takes care of the downloading/remapping/merging, what you do with the data is up to you.

## Other options

Useful information is logged at the `--info` level.

If you pass `--refresh-dependencies`, pass `-Dminivan.refreshDependencies=true`, or set `minivan { refreshDependencies = true }` in-script, all derived artifacts will be deleted and recomputed. Similar for `--offline`/`-Dminivan.offline`/`minivan { offline = true }`; it will now error-out instead of making network connections. The `minivan`-specific ones only affect `minivan` and not the other things in Gradle that are controlled by those switches.

## Migrating from VanillaGradle

* Swap the plugin invocation to `agency.highlysuspect.minivan`.
* Change `minecraft {` to `minivan {`.

Currently the *only* supported function inside the `minivan` block is `version`, and *no* Gradle tasks are added.

Important note for IntelliJ users:

* Right click on any project or task in the Gradle Tool Window and select "Tasks Activation". There is surely an easier way to get to this dialog but I can't find it.
* Remove all task activations that refer to running a `:prepareWorkspace` task after syncing.
  * This will probably be "all of them".
  * `minivan` does not add this task, so IntelliJ sync will fail until this activation is removed.

# things that this plugin glues together

The neat thing about writing Gradle tooling for Minecraft in 2023 is that everyone has already wrote everything by now. It's just a matter of assembling other people's libraries in the right way with the right glue code. This plugin is like 5% original work by-weight. Most of the heavy lifting is done by:

* [CadixDev/Lorenz](https://github.com/CadixDev/Lorenz) and `lorenz-io-proguard` parse Mojang's official mappings.
* [FabricMC/tiny-remapper](https://github.com/FabricMC/tiny-remapper) is the jar remapper of choice.
* A modified version of a tool from [FabricMC/stitch](https://github.com/FabricMC/stitch) merges the client and server jars.

The general approach (`prov` package) has been copied from how I ended up structuring [voldeloom](https://github.com/CrackedPolishedBlackstoneBricksMC/voldeloom/).

# todo

* that stuff i commented out in JarMergerCooler in voldeloom might need to be brought back lol (⛄)
* Parchment stuff:
  * param-name mappings
  * javadoc (which requires implementing `genSources`, doing linemapping, etc. voldeloom has mosta that stuff)
* Hmm: optional mode that tries to use a cached jar from `fabric-loom`, to save RAM

# License

MIT