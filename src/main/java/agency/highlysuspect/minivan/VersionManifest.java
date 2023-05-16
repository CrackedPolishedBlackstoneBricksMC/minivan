package agency.highlysuspect.minivan;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * A per-version Minecraft version manifest.
 * <p>
 * This class is intended to be deserialized with Google GSON.
 */
@SuppressWarnings("unused")
public class VersionManifest {
	public static VersionManifest read(Path path) throws IOException {
		try(BufferedReader reader = Files.newBufferedReader(path)) {
			return new Gson().fromJson(reader, VersionManifest.class);
		}
	}
	
	public List<Library> libraries;
	public Map<String, Downloads> downloads;
	@SerializedName("assetIndex") public AssetIndexReference assetIndexReference;
	public String id; //version number
	public String mainClass;
	public String minecraftArguments;
	
	public String getUrl(String type) {
		Downloads dl = downloads.get(type);
		
		if(dl == null) {
			String message = "No download of type '" + type + "' available in the Minecraft " + id + " version manifest.";
			if(type.endsWith("_mappings")) message += "\nIt looks like Mojang did not provide official mappings for this version.";
			throw new IllegalArgumentException(message);
		}
		
		return dl.url;
	}
	
	public static class Downloads {
		public String url;
		public String sha1;
	}
	
	public static class AssetIndexReference {
		public String id;
		public String sha1;
		public String url;
	}
	
	public static class Library {
		public String name;
		@SerializedName("url") public String forgeDownloadRoot; //used by Forge 1.6/1.7's version.json, i don't think it's vanilla
		public JsonObject natives;
		public JsonObject downloads;
		private Artifact artifact;
		public Rule[] rules;
		
		/** url pattern that can be appended to mojang's libraries.minecraft.net server */
		public String getURLSuffix() {
			String[] parts = this.name.split(":", 3);
			return parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + getClassifier() + ".jar";
		}
		
		public Path getPath(Path basePath) {
			return basePath.resolve(MinivanPlugin.filenameSafe(name) + ".jar");
		}
		
		public String getSha1() {
			if (this.downloads == null) {
				return "";
			} else if (this.downloads.getAsJsonObject("artifact") == null) {
				return "";
			} else if (this.downloads.getAsJsonObject("artifact").get("sha1") == null) {
				return "";
			} else {
				return this.downloads.getAsJsonObject("artifact").get("sha1").getAsString();
			}
		}
		
		public String getClassifier() {
			if (natives == null) {
				return "";
			} else {
				JsonElement element = natives.get(OperatingSystem.getOS().replace("${arch}", OperatingSystem.getArch()));
				
				if (element == null) {
					return "";
				}
				
				return "-" + element.getAsString().replace("\"", "").replace("${arch}", OperatingSystem.getArch());
			}
		}
		
		public boolean isNative() {
			return getClassifier().contains("natives");
		}
		
		public boolean allowed() {
			if (this.rules == null || this.rules.length <= 0) {
				return true;
			}
			
			boolean success = false;
			
			for (Rule rule : this.rules) {
				if (rule.os != null && rule.os.name != null) {
					if (rule.os.name.equalsIgnoreCase(OperatingSystem.getOS())) {
						return rule.action.equalsIgnoreCase("allow");
					}
				} else {
					success = rule.action.equalsIgnoreCase("allow");
				}
			}
			
			return success;
		}
		
		public String getArtifactName() {
			if (artifact == null) {
				artifact = new Artifact(name);
			}
			
			if (natives != null) {
				JsonElement jsonElement = natives.get(OperatingSystem.getOS());
				
				if (jsonElement != null) {
					return artifact.getArtifact(jsonElement.getAsString());
				}
			}
			
			return artifact.getArtifact(artifact.classifier);
		}
		
		private static class Artifact {
			private final String domain, name, version, classifier, ext;
			
			Artifact(String name) {
				String[] splitedArtifact = name.split(":");
				int idx = splitedArtifact[splitedArtifact.length - 1].indexOf('@');
				
				if (idx != -1) {
					ext = splitedArtifact[splitedArtifact.length - 1].substring(idx + 1);
					splitedArtifact[splitedArtifact.length - 1] = splitedArtifact[splitedArtifact.length - 1].substring(0, idx);
				} else {
					ext = "jar";
				}
				
				this.domain = splitedArtifact[0];
				this.name = splitedArtifact[1];
				this.version = splitedArtifact[2];
				this.classifier = splitedArtifact.length > 3 ? splitedArtifact[3] : null;
			}
			
			public String getArtifact(String classifier) {
				String ret = domain + ":" + name + ":" + version;
				
				if (classifier != null && classifier.indexOf('$') > -1) {
					classifier = classifier.replace("${arch}", OperatingSystem.getArch());
				}
				
				if (classifier != null) {
					ret += ":" + classifier;
				}
				
				if (!"jar".equals(ext)) {
					ret += "@" + ext;
				}
				
				return ret;
			}
			
			public String getClassifier() {
				return classifier;
			}
		}
	}
	
	private static class Rule {
		public String action;
		public OS os;
		
		private static class OS {
			String name;
		}
	}
	
	//TODO: Pretty sure this is only used for native libraries, which I don't need in minivan
	// because native libraries are only needed for runtime I think
	private static class OperatingSystem {
		public static String getOS() {
			String osName = System.getProperty("os.name").toLowerCase();
			
			if (osName.contains("win")) {
				return "windows";
			} else if (osName.contains("mac")) {
				return "osx";
			} else {
				return "linux";
			}
		}
		
		public static String getArch() {
			if (is64Bit()) {
				return "64";
			} else {
				return "32";
			}
		}
		
		public static boolean is64Bit() {
			return System.getProperty("sun.arch.data.model").contains("64");
		}
	}
}