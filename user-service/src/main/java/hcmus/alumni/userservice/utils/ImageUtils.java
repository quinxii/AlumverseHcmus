package hcmus.alumni.userservice.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

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
	private final String avatarPath = "images/users/avatar/";
	private final String coverPath = "images/users/cover/";
	private final String noneAvatar = "none";
	public static int saltLength = 16;

	// Save image in a local directory
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile, String imageName)
			throws IOException {
		if (imageFile == null) {
			String imageUrl = gcp.getDomainName() + gcp.getBucketName() + "/" + uploadDirectory + noneAvatar;
			return imageUrl;
		}

		String newFilename = uploadDirectory + imageName;

		// Convert to jpeg (if png), resize then compress image
		BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
		if (isPng(imageFile)) {
			bufferedImage = convertPngToJpeg(bufferedImage);
		}
		bufferedImage = imageCompression.resizeImage(bufferedImage, resizeMaxWidth, resizeMaxHeight);
		byte[] imageBytes = imageCompression.compressImage(bufferedImage, "image/jpeg", 0.8f);

		BlobInfo blobInfo = BlobInfo.newBuilder(gcp.getBucketName(), newFilename)
				.setContentType("image/jpeg").setCacheControl("no-cache").build();

		// Upload the image from the local file path
		try {
			gcp.getStorage().create(blobInfo, imageBytes);
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

	public BufferedImage convertPngToJpeg(BufferedImage pngImage) {
		BufferedImage jpegImage = new BufferedImage(pngImage.getWidth(), pngImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = jpegImage.createGraphics();
		g.drawImage(pngImage, 0, 0, Color.WHITE, null);
		g.dispose();
		return jpegImage;
	}

	public static boolean isJpegOrPng(MultipartFile imageFile) {
		String contentType = imageFile.getContentType();
		return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
	}

	public static boolean isPng(MultipartFile imageFile) {
		String contentType = imageFile.getContentType();
		return contentType != null && contentType.equals("image/png");
	}

	public String getAvatarPath() {
		return this.avatarPath;
	}
	
	public String getCoverPath() {
		return this.coverPath;
	}
	
	public String getAvatarPath(String id) {
		return this.avatarPath + id + "/";
	}
	
	public String getCoverPath(String id) {
		return this.coverPath + id + "/";
	}

}