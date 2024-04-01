package hcmus.alumni.news.utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageException;

import lombok.Getter;

@Service
@Getter
public class ImageUtils {
	@Autowired
	private GCPConnectionUtils gcp;
	private final String avatarPath = "images/users/avatar/";
	private final String noneAvatar = "none";
	private final String newsPath = "images/news/";
	public static int saltLength = 16;

	// Save MultipartFile Image
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile, String imageName)
			throws IOException {
		if (imageFile == null) {
			return null;
		}

		String newFilename = uploadDirectory + imageName;

		// Convert MultipartFile to byte array
		byte[] imageBytes = imageFile.getBytes();

		BlobInfo blobInfo = BlobInfo.newBuilder(gcp.getBucketName(), newFilename)
				.setContentType(imageFile.getContentType()).build();

		// Upload the image from the local file path
		try {
			gcp.getStorage().create(blobInfo, imageBytes);
		} catch (StorageException e) {
			System.err.println("Error uploading image: " + e.getMessage());
		}

		String imageUrl = gcp.getDomainName() + gcp.getBucketName() + "/" + newFilename;
		return imageUrl;
	}
	
	// Save Base 64 image
	public String saveBase64ImageToStorage(String uploadDirectory, String base64Image, String imageName)
			throws IOException {
		if (base64Image == null || base64Image.equals("")) {
			return null;
		}

		String newFilename = uploadDirectory + imageName;

	    // Decode Base64 string to byte array
		String[] extracted = extractContentTypeAndDataFromImageBase64(base64Image);
	    byte[] decodedBytes = Base64.getDecoder().decode(extracted[1]);

		BlobInfo blobInfo = BlobInfo.newBuilder(gcp.getBucketName(), newFilename)
				.setContentType(extracted[0]).build();

		// Upload the image from the local file path
		try {
			gcp.getStorage().create(blobInfo, decodedBytes);
		} catch (StorageException e) {
			System.err.println("Error uploading image: " + e.getMessage());
		}

		String imageUrl = gcp.getDomainName() + gcp.getBucketName() + "/" + newFilename;
		return imageUrl;
	}

	public void deleteImageFromStorageByUrl(String oldImageUrl) {
		if (oldImageUrl == null) {
			return;
		}
		// Get image name
		String regex = gcp.getDomainName() + gcp.getBucketName() + "/";
		String extractedImageName = oldImageUrl.replaceAll(regex, "");

		// No delete if no avatar
		if (extractedImageName.contains(noneAvatar)) {
			return;
		}

		try {
			Blob blob = gcp.getStorage().get(gcp.getBucketName(), extractedImageName);
			if (blob == null) {
				System.out.println("null");
				return;
			}
			gcp.getStorage().delete(gcp.getBucketName(), extractedImageName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	public static String hashImageName(String imageName) throws NoSuchAlgorithmException {
		// Generate a random salt using a cryptographically secure random number
		// generator
		byte[] salt = new byte[saltLength]; // Adjust salt length as needed (16 bytes is common)
		new SecureRandom().nextBytes(salt);

		// Combine image name and salt for hashing
		String dataToHash = imageName + Base64.getUrlEncoder().encodeToString(salt);

		// Hash the combined data using a strong algorithm like SHA-256
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(dataToHash.getBytes());

		// Convert digest bytes to a hexadecimal string
		StringBuilder hashedName = new StringBuilder();
		for (byte b : digest.digest()) {
			hashedName.append(String.format("%02x", b));
		}

		// Prepend the encoded salt to the hashed name for storage
		return hashedName.toString();
	}
	
	public String[] extractContentTypeAndDataFromImageBase64(String base64) {
		String[] strings = base64.split(",");
		switch (strings[0]) {//check image's extension
		    case "data:image/jpeg;base64":
		    	strings[0] = "image/jpeg";
		        break;
		    case "data:image/png;base64":
		    	strings[0] = "image/png";
		        break;
		    default:
		    	strings[0] = null;
		        break;
		}
		return strings;
	}

	public String getNewsPath(String id) {
		return this.newsPath + id + "/";
	}
}
