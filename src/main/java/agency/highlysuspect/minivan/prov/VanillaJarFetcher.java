package agency.highlysuspect.minivan.prov;

import agency.highlysuspect.minivan.DownloadSession;
import agency.highlysuspect.minivan.ManifestIndex;
import agency.highlysuspect.minivan.MinivanPlugin;
import agency.highlysuspect.minivan.VersionManifest;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.time.Period;

public class VanillaJarFetcher extends MiniProvider {
	public VanillaJarFetcher(Project project, String version) {
		super(project);
		this.version = version;
		this.versionFilenameSafe = MinivanPlugin.filenameSafe(version);
	}
	
	private final String version, versionFilenameSafe;
	
	public Result fetch() throws Exception {
		Path versionManifestIndexJson = getOrCreate("version_manifest.json", to -> {
			log.lifecycle("Downloading version_manifest.json to {}", to);
			new DownloadSession(project)
				.url("https://launchermeta.mojang.com/mc/game/version_manifest.json")
				.dest(to)
				.etag(true)
				.gzip(true)
				.skipIfNewerThan(Period.ofDays(14))
				.download();
		});
		log.info("version_manifest.json: {}", versionManifestIndexJson);
		
		ManifestIndex versionManifestIndex = ManifestIndex.read(versionManifestIndexJson);
		ManifestIndex.VersionData selectedVersion = versionManifestIndex.versions.get(version);
		
		Path thisVersionManifestJson = getOrCreate("minecraft-" + versionFilenameSafe + "-info.json", to -> {
			log.lifecycle("Downloading {} manifest to {}", version, to);
			new DownloadSession(project)
				.url(selectedVersion.url)
				.dest(to)
				.gzip(true)
				.etag(true)
				.download();
		});
		log.info("{} manifest: {}", version, thisVersionManifestJson);
		
		VersionManifest vm = VersionManifest.read(thisVersionManifestJson);
		
		Path clientJar = fetch(vm, "client", "client.jar");
		Path serverJar = fetch(vm, "server", "server.jar");
		Path clientMap = fetch(vm, "client_mappings", "client-mappings.txt");
		Path serverMap = fetch(vm, "server_mappings", "server-mappings.txt");
		
		log.info("client: {}", clientJar);
		log.info("server: {}", serverJar);
		
		return new Result(clientJar, serverJar, clientMap, serverMap, vm);
	}
	
	private Path fetch(VersionManifest vm, String downloadType, String filenameSuffix) throws Exception {
		String filename = "minecraft-" + versionFilenameSafe + "-" + filenameSuffix;
		boolean isJar = filename.endsWith(".jar");
		
		return getOrCreate(filename, to -> {
			log.lifecycle("Downloading {}{} to {}", downloadType, isJar ? " jar" : "", to);
			new DownloadSession(project)
				.url(vm.downloads.get(downloadType).url)
				.dest(to)
				.etag(true)
				.gzip(!isJar) //I've had problems with gzipping minecraft jars before
				.download();
		});
	}
	
	public static class Result {
		public Result(Path client, Path server, Path clientMappings, Path serverMappings, VersionManifest versionManifest) {
			this.client = client;
			this.server = server;
			this.clientMappings = clientMappings;
			this.serverMappings = serverMappings;
			this.versionManifest = versionManifest;
		}
		
		public final Path client, server;
		public final Path clientMappings, serverMappings; //TODO: make Nullable (versions without official mappings support?)
		public final VersionManifest versionManifest;
	}
}
