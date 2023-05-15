package agency.highlysuspect.minivan;

import agency.highlysuspect.minivan.prov.Merger;
import agency.highlysuspect.minivan.prov.RemapperPrg;
import agency.highlysuspect.minivan.prov.VanillaJarFetcher;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Pattern;

public class MinivanPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getLogger().info("vroom vroom! applying minivan");
		project.getExtensions().create("minivan", MinivanExt.class, project);
		
		//Free bonus Mavens, vanilla 3rdparty libraries found here
		project.getRepositories().maven(repo -> {
			repo.setName("Mojang");
			repo.setUrl("https://libraries.minecraft.net/"); //and here
		});
		project.getRepositories().mavenCentral();
		
		project.afterEvaluate(this::afterEvaluate);
	}
	
	public void afterEvaluate(Project project) {
		//Gradle calls afterEvaluate even in the face of blatant errors, which is not what we want
		if(project.getState().getFailure() != null) return;
		
		MinivanExt ext = project.getExtensions().getByType(MinivanExt.class);
		
		String version = ext.version;
		if(version == null) {
			project.getLogger().warn("minivan: version not set in MinivanExt");
			return;
		}
		
		try {
			doIt(project, version);
		} catch (Exception e) {
			throw new RuntimeException("problem setting up minivan: " + e.getMessage(), e);
		}
	}
	
	private void doIt(Project project, String version) throws Exception {
		//Fetch vanilla jars, official mappings, version manifest
		VanillaJarFetcher.Result vanillaJars = new VanillaJarFetcher(project, version).fetch();
		
		//Add Gradle dependencies on third-party libraries
		for(VersionManifest.Library lib : vanillaJars.versionManifest.libraries) {
			if(lib.isNative() || !lib.allowed()) continue;
			
			if(lib.name.contains("mojang:logging")) {
				continue; //TODO, this one is broken on my machine for some reason
			}
			
			project.getLogger().info("found vanilla dependency: {}", lib.getArtifactName());
			project.getDependencies().add("compileOnly", lib.getArtifactName());
		}
		
		//Remap client and server using official names
		String minecraftPrefix = "minecraft-" + filenameSafe(version);
		Path clientMapped = new RemapperPrg(project, vanillaJars.client, vanillaJars.clientMappings, minecraftPrefix + "-client-mapped.jar").remap();
		Path serverMapped = new RemapperPrg(project, vanillaJars.server, vanillaJars.serverMappings, minecraftPrefix + "-server-mapped.jar").remap();
		
		//Merge client and server
		Path merged = new Merger(project, clientMapped, serverMapped, minecraftPrefix + "-merged.jar").merge();
		
		//Add Gradle dependency on merged minecraft
		project.getDependencies().add("compileOnly", project.files(merged));
	}
	
	//util
	private static final Pattern NON_FILENAME_SAFE = Pattern.compile("[^A-Za-z0-9.-]");
	public static String filenameSafe(String in) {
		return NON_FILENAME_SAFE.matcher(in).replaceAll("_");
	}
	
	public static FileSystem openFs(Path path) throws IOException {
		return FileSystems.newFileSystem(URI.create("jar:" + path.toUri()), Collections.emptyMap());
	}
	
	public static FileSystem createFs(Path path) throws IOException {
		return FileSystems.newFileSystem(URI.create("jar:" + path.toUri()), Collections.singletonMap("create", "true"));
	}
}
