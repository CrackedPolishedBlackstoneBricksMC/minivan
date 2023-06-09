package agency.highlysuspect.minivan;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("CanBeFinal") //google gson
public class ManifestIndex {
	public static ManifestIndex read(Path path) throws IOException {
		try(BufferedReader reader = Files.newBufferedReader(path)) {
			ManifestIndex manifestIndex = new Gson().fromJson(reader, ManifestIndex.class);
			
			manifestIndex.versionList.forEach(v -> manifestIndex.versions.put(v.id, v));
			
			return manifestIndex;
		}
	}
	
	@SerializedName("versions") public List<VersionData> versionList = new ArrayList<>();
	public transient Map<String, VersionData> versions = new HashMap<>();
	
	public static class VersionData {
		public String id, url;
	}
}