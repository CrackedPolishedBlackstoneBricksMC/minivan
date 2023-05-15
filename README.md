# minivan

Bare-minimum `afterEvaluate` Gradle plugin for putting vanilla Minecraft on the compilation classpath using official names.

`minivan` is much smaller and much less feature-rich than [VanillaGradle](https://github.com/SpongePowered/VanillaGradle/) (hence the name). `minivan` makes *no* attempt to provide "run configs", reobfuscation, asset downloading, `genSources`/`decompile`, access widening, good error messages...

The intended audience is people writing [jaredlll08/MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) -style mods and just want something to back the `Common`/`Xplat` subproject.

# License

MIT

# Usage

See `demo`.

```gradle
buildscript {
	repositories {
		mavenCentral()
		maven { url "https://maven.fabricmc.net/" }
		maven { url "https://repo.sleeping.town/" }
	}
	dependencies {
		classpath "agency.highlysuspect:minivan:0.1"
	}
}

apply plugin: "java"
apply plugin: "agency.highlysuspect.minivan"

minivan {
	version("1.18.2")
}
```

### Migrating from VanillaGradle

* Swap the plugin invocation to `agency.highlysuspect:minivan:0.1`.
* Change `minecraft {` to `minivan {`.

Currently the *only* supported function inside the `minivan` block is `version`, and *no* Gradle tasks are added.

Important note for IntelliJ users:

* Right click on any project or task in the Gradle Tool Window and select "Tasks Activation". There is surely an easier way to get to this dialog but I can't find it.
* Remove all task activations that refer to running a `:prepareWorkspace` task after syncing.
  * This will probably be "all of them".
  * `minivan` does not add this task, so IntelliJ sync will fail until this activation is removed.

# things that this plugin glues together

This plugin is like 5% original work by-weight. Most of the heavy lifting is done by:

* Mojang's mappings are parsed with the excellent [CadixDev/Lorenz](https://github.com/CadixDev/Lorenz) and `lorenz-io-proguard`.
* Jar remapping is performed with [FabricMC/tiny-remapper](https://github.com/FabricMC/tiny-remapper).
* Jar merging is done with a modified version of a tool from [FabricMC/stitch](https://github.com/FabricMC/stitch).

Much of the code has been copied from [voldeloom](https://github.com/CrackedPolishedBlackstoneBricksMC/voldeloom/).

# todo

* that stuff i commented out in JarMergerCooler in voldeloom might need to be brought back lol (⛄)
* Parchment stuff:
  * param-name mappings
  * javadoc (which requires implementing `genSources`, doing linemapping, etc. voldeloom has mosta that stuff)
* Hmm: optional mode that tries to use a cached jar from `fabric-loom`, to save RAM 