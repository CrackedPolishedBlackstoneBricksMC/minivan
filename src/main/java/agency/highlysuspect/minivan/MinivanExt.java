package agency.highlysuspect.minivan;

import agency.highlysuspect.minivan.prov.MinecraftProvider;
import agency.highlysuspect.minivan.prov.Props;
import org.gradle.api.Project;

public class MinivanExt {
	public MinivanExt(Project project) {
		this.project = project;
		
		this.offline = project.getGradle().getStartParameter().isOffline() ||
			project.hasProperty("minivan.offline") ||
			System.getProperty("minivan.offline") != null;
		
		this.refreshDependencies = project.getGradle().getStartParameter().isRefreshDependencies() ||
			project.hasProperty("minivan.refreshDependencies") ||
			System.getProperty("minivan.refreshDependencies") != null;
	}
	
	private final Project project;
	public boolean offline, refreshDependencies;
	
	/// VanillaGradle-ish API ///
	
	public String version = null;
	
	@SuppressWarnings("unused")
	public MinivanExt version(String v) {
		version = v;
		return this;
	}
	
	void setupAfterEvaluate() {
		//this part is where the magic happens:
		project.afterEvaluate(__ -> {
			if(version == null) return;
			tryGetMinecraft(version).installTo(project, "compileOnly");
		});
	}
	
	/// Lower-level, direct API ///
	
	@SuppressWarnings("unused")
	public MinecraftProvider.Result getMinecraft(String version) throws Exception {
		return new MinecraftProvider(project, version).getMinecraft();
	}
	
	public MinecraftProvider.Result tryGetMinecraft(String version) {
		try {
			return getMinecraft(version);
		} catch (Exception e) {
			project.getLogger().error("problem getting Minecraft " + version + ": " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
