package hcmus.alumni.userservice.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;

@Service
public class ImageUtils {
	@Autowired
	private Storage storage;

	// Save image in a local directory
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile, String imageName)
			throws IOException {
		String extension = StringUtils.getFilenameExtension(imageFile.getOriginalFilename());
		String newFilename = uploadDirectory + imageName + "." + extension;
		
		// Convert MultipartFile to byte array
		byte[] imageBytes = imageFile.getBytes();

		BlobInfo blobInfo = BlobInfo.newBuilder("hcmus-alumverse", newFilename)
				.setContentType(imageFile.getContentType())
				.build();

		// Upload the image from the local file path
		try {
			storage.create(blobInfo, imageBytes);
		} catch (StorageException e) {
			System.err.println("Error uploading image: " + e.getMessage());
		}

		return newFilename;
	}

	// To view an image
	public byte[] getImage(String imageDirectory, String imageName) throws IOException {
		Path imagePath = Path.of(imageDirectory, imageName);

		if (Files.exists(imagePath)) {
			byte[] imageBytes = Files.readAllBytes(imagePath);
			return imageBytes;
		} else {
			return null; // Handle missing images
		}
	}

	// Delete an image
	public String deleteImage(String imageDirectory, String imageName) throws IOException {
		Path imagePath = Path.of(imageDirectory, imageName);

		if (Files.exists(imagePath)) {
			Files.delete(imagePath);
			return "Success";
		} else {
			return "Failed"; // Handle missing images
		}
	}
}
