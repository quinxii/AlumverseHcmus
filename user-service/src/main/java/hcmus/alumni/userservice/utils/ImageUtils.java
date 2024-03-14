package hcmus.alumni.userservice.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;

@Service
public class ImageUtils {
	@Autowired
	private GCPConnectionUtils gcp;
	private final String avatarPath = "images/users/avatar/";
	private final String noneAvatar = "none.png";
	
	// Save image in a local directory
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile, String imageName)
			throws IOException {
		if (imageFile.isEmpty()) {
			String imageUrl = gcp.getDomainName() + gcp.getBucketName() + "/" + uploadDirectory + noneAvatar;
			return imageUrl;
		}
		
		String extension = StringUtils.getFilenameExtension(imageFile.getOriginalFilename());
		String newFilename = uploadDirectory + imageName + "." + extension;
		
		// Convert MultipartFile to byte array
		byte[] imageBytes = imageFile.getBytes();

		BlobInfo blobInfo = BlobInfo.newBuilder(gcp.getBucketName(), newFilename)
				.setContentType(imageFile.getContentType())
				.build();

		// Upload the image from the local file path
		try {
			gcp.getStorage().create(blobInfo, imageBytes);
		} catch (StorageException e) {
			System.err.println("Error uploading image: " + e.getMessage());
		}

		String imageUrl = gcp.getDomainName() + gcp.getBucketName() + "/" + newFilename;
		return imageUrl;
	}

	public String getAvatarPath() {
		return avatarPath;
	}
}
