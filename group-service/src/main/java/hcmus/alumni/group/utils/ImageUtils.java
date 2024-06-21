package hcmus.alumni.group.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageException;

@Service
public class ImageUtils {
	@Autowired
	private GCPConnectionUtils gcp;
	@Autowired
	private ImageCompression imageCompression;

	private final int resizeMaxWidth = 2000;
	private final int resizeMaxHeight = 2000;
	private final String groupPath = "images/groups/";
	public static int saltLength = 16;

	// Save MultipartFile Image
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile, String imageName)
			throws IOException {
		if (imageFile == null) {
			return null;
		}

		String newFilename = uploadDirectory + imageName;

		// Resize then compress image
		BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
		bufferedImage = imageCompression.resizeImage(bufferedImage, resizeMaxWidth, resizeMaxHeight);
		byte[] imageBytes = imageCompression.compressImage(bufferedImage, imageFile.getContentType(), 0.8f);

		BlobInfo blobInfo = BlobInfo.newBuilder(gcp.getBucketName(), newFilename)
				.setContentType(imageFile.getContentType()).setCacheControl("no-cache").build();

		// Upload the image from the local file path
		try {
			gcp.getStorage().create(blobInfo, imageBytes);
		} catch (StorageException e) {
			System.err.println("Error uploading image: " + e.getMessage());
		}

		String imageUrl = gcp.getDomainName() + gcp.getBucketName() + "/" + newFilename;
		return imageUrl;
	}

	public boolean deleteImageFromStorageByUrl(String oldImageUrl) throws FileNotFoundException, IOException {
		if (oldImageUrl == null) {
			return false;
		}
		// Get image name
		String regex = gcp.getDomainName() + gcp.getBucketName() + "/";
		String extractedImageName = oldImageUrl.replaceAll(regex, "");

		Blob blob = gcp.getStorage().get(gcp.getBucketName(), extractedImageName);
		if (blob == null) {
			return false;
		}
		return gcp.getStorage().delete(gcp.getBucketName(), extractedImageName);

	}
	
	public String getGroupPath(String id) {
		return groupPath + id + "/";
	}
	
	public String getPostGroupPath(String groupId, String postId) {
		return groupPath + groupId + "/posts/" + postId + "/";
	}
	
	public String getDefaultCoverUrl() {
		return gcp.getDomainName() + gcp.getBucketName() + "/" + groupPath + "defaultCover";
	}
}
