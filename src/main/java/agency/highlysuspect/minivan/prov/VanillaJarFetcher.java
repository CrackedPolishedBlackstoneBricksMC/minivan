package agency.highlysuspect.minivan.prov;

import agency.highlysuspect.minivan.DownloadSession;
import agency.highlysuspect.minivan.ManifestIndex;
import agency.highlysuspect.minivan.MinivanPlugin;
import agency.highlysuspect.minivan.VersionManifest;
import org.gradle.api.Project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;

public class VanillaJarFetcher extends MiniProvider {
	public VanillaJarFetcher(Project project, String version) {
		super(project);
		this.version = version;
		this.filenamePrefix = "minecraft-" + MinivanPlugin.filenameSafe(version);
	}
	
	private final String version;
	private final String filenamePrefix;
	public static final String PISTON_META = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
	
	public Result fetch() throws Exception {
		VersionManifest vm = getVersionManifestFor(version);
		
		//Try to fetch mappings first, just to crash early if there are no official mappings available
		Path clientMap = getOrCreate(filenamePrefix + "-client-mappings.txt", to -> {
			log.lifecycle("Downloading client mappings to {}", to);
			new DownloadSession(project).url(vm.getUrl("client_mappings")).dest(to).etag(true).gzip(true).download();
		});
		Path serverMap = getOrCreate(filenamePrefix + "-server-mappings.txt", to -> {
			log.lifecycle("Downloading server mappings to {}", to);
			new DownloadSession(project).url(vm.getUrl("server_mappings")).dest(to).etag(true).gzip(true).download();
		});
		
		//Don't gzip minecraft jars in-flight, i've had bizarre issues with it in the past. Sorry
		Path clientJar = getOrCreate(filenamePrefix + "-client.jar", to -> {
			log.lifecycle("Downloading client jar to {}", to);
			new DownloadSession(project).url(vm.getUrl("client")).dest(to).etag(true).gzip(false).download();
		});
		Path serverJar = getOrCreate(filenamePrefix + "-server.jar", to -> {
			log.lifecycle("Downloading server jar to {}", to);
			new DownloadSession(project).url(vm.getUrl("server")).dest(to).etag(true).gzip(false).download();
		});
		
		log.info("client: {}", clientJar);
		log.info("server: {}", serverJar);
		
		return new Result(clientJar, serverJar, clientMap, serverMap, vm);
	}

	//TODO: This sucks, i'm just taping things together.
	// One way to make this less crap would be to make a DownloadSession that downloads in-memory, that way
	// i don't need to worry about a local piston-meta cache.

	private VersionManifest getVersionManifestFor(String version) throws Exception {
		Path versionManifestJson = getOrCreate(filenamePrefix + "-info.json", to -> {
			//We don't know anything about this version yet. First, check our copy of piston-meta
			ManifestIndex pistonMetaCache = fetchPistonMeta();
			ManifestIndex.VersionData selectedVersion = pistonMetaCache.versions.get(version);

			if(selectedVersion != null) {
				log.lifecycle("Downloading {} manifest to {}", version, to);
				new DownloadSession(project).url(selectedVersion.url).dest(to).etag(true).gzip(true).download();
				return;
			}

			log.lifecycle("Don't know about version '{}', trying to re-fetch version_manifest_v2.json...", version);

			//We still don't know about this version, delete piston-meta and check again.
			Files.deleteIfExists(cacheDir().resolve("version_manifest_v2.json"));

			pistonMetaCache = fetchPistonMeta();
			selectedVersion = pistonMetaCache.versions.get(version);
			if(selectedVersion != null) {
				log.lifecycle("Downloading new {} manifest to {}", version, to);
				new DownloadSession(project).url(selectedVersion.url).dest(to).etag(true).gzip(true).download();
				return;
			}

			throw new IllegalArgumentException("Don't know of any version named '" + version + "'");
		});

		log.info("{} manifest: {}", version, versionManifestJson);
		return VersionManifest.read(versionManifestJson);
	}

	private ManifestIndex fetchPistonMeta() throws Exception {
		//TODO same problem as voldeloom, the skipIfNewerThan happens only if the file doesn't exist so it's redundant
		Path versionManifestIndexJson = getOrCreate("version_manifest_v2.json", to -> {
			log.lifecycle("Downloading version_manifest.json to {}", to);
			new DownloadSession(project).url(PISTON_META).dest(to).etag(true).gzip(true).skipIfNewerThan(Period.ofDays(14)).download();
		});
		log.info("version_manifest.json: {}", versionManifestIndexJson);

		return ManifestIndex.read(versionManifestIndexJson);
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
		public final Path clientMappings, serverMappings;
		public final VersionManifest versionManifest;
	}
}
