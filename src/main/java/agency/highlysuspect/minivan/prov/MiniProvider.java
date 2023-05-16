package agency.highlysuspect.minivan.prov;

import agency.highlysuspect.minivan.MinivanExt;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MiniProvider {
	public MiniProvider(Project project) {
		this.project = project;
		this.ext = project.getExtensions().getByType(MinivanExt.class);
		this.log = project.getLogger();
	}
	
	protected final Project project;
	protected final MinivanExt ext;
	protected final Logger log;
	
	protected Path cacheDir() throws IOException {
		Path cacheDir = project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("minivan-cache");
		Files.createDirectories(cacheDir);
		return cacheDir;
	}
	
	protected Path getOrCreate(Path p, ThrowyConsumer<Path> creator) throws Exception {
		if(ext.refreshDependencies) Files.deleteIfExists(p);
		if(Files.notExists(p)) {
			creator.accept(p);
			
			//double-check that the file really does exist now
			if(Files.notExists(p)) throw new IllegalStateException("File at " + p + " wasn't created by " + creator);
		}
		return p;
	}
	
	protected Path getOrCreate(String p, ThrowyConsumer<Path> creator) throws Exception {
		return getOrCreate(cacheDir().resolve(p), creator);
	}
	
	protected interface ThrowyConsumer<T> {
		void accept(T t) throws Exception;
	}
}
