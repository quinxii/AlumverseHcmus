package hcmus.alumni.news.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class GCPConnectionUtils {

	@Value("${gcp.serviceAccountKeyPath}")
	private String serviceAccountKeyPath;
	@Value("${gcp.bucketName}")
	private String bucketName;
	@Value("${gcp.domainName}")
	private String domainName;
	private Storage storage = null;


	public Storage getStorage() throws FileNotFoundException, IOException {
		if (this.storage != null) {
			return this.storage;
		}
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyPath));
		this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
		return this.storage;
	}

	public String getBucketName() {
		return this.bucketName;
	}

	public String getDomainName() {
		return domainName;
	}
}
