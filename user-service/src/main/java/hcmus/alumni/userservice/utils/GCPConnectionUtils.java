package hcmus.alumni.userservice.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class GCPConnectionUtils {

	@Value("${gcp.serviceAccountKeyPath}")
	private String serviceAccountKeyPath;

	@Value("${gcp.bucketName}") // Optional: Specify default bucket name
	private String bucketName;

	@Bean
	public Storage getStorage() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyPath));
		return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
	}
	
	@Bean
	public String getBucketName() {
		return bucketName;
	}
}
