package agency.highlysuspect.minivan;

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
		project.getLogger().info("vroom vroom! applying minivan 0.4");
		
		project.getExtensions().create("minivan", MinivanExt.class, project)
			.setupAfterEvaluate();
		
		//Free bonus Mavens, vanilla 3rdparty libraries are found here.
		project.getRepositories().maven(repo -> {
			repo.setName("Mojang");
			repo.setUrl("https://libraries.minecraft.net/"); //and here
		});
		project.getRepositories().mavenCentral();
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
