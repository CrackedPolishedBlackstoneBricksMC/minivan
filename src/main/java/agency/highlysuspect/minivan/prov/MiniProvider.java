package agency.highlysuspect.minivan.prov;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class MiniProvider {
	public MiniProvider(Project project) {
		this.project = project;
		this.log = project.getLogger();
	}
	
	protected final Project project;
	protected final Logger log;
	
	protected Path cacheDir() throws IOException {
		Path cacheDir = project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("minivan-cache");
		Files.createDirectories(cacheDir);
		return cacheDir;
	}
	
	protected Path getOrCreate(Path p, ThrowyConsumer<Path> creator) throws Exception {
		if(project.getGradle().getStartParameter().isRefreshDependencies()) Files.deleteIfExists(p);
		if(Files.notExists(p)) creator.accept(p);
		return p;
	}
	
	protected Path getOrCreate(String p, ThrowyConsumer<Path> creator) throws Exception {
		return getOrCreate(cacheDir().resolve(p), creator);
	}
	
	protected interface ThrowyConsumer<T> {
		void accept(T t) throws Exception;
	}
}
