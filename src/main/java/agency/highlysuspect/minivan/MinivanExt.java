package agency.highlysuspect.minivan;

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
	public String version = null;
	
	@SuppressWarnings("unused") //gradle api, function version for text-compat with VanillaGradle
	public MinivanExt version(String v) {
		version = v;
		return this;
	}
}
