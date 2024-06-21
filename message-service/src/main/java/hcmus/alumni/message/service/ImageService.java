package hcmus.alumni.message.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageException;

import lombok.Getter;

@Service
@Getter
public class ImageService {
	@Autowired
	private GCPConnectionService gcp;
	@Autowired
	private ImageCompressionService imageCompression;

	private final int resizeMaxWidth = 2000;
	private final int resizeMaxHeight = 2000;
	public final static String messagesPath = "messages/";
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

	public BufferedImage convertPngToJpeg(BufferedImage pngImage) {
		BufferedImage jpegImage = new BufferedImage(pngImage.getWidth(), pngImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = jpegImage.createGraphics();
		g.drawImage(pngImage, 0, 0, Color.WHITE, null);
		g.dispose();
		return jpegImage;
	}

	public static String generateUniqueImageName(Long inboxId) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		String uniqueName = inboxId + "_" + timestamp + "_" + UUID.randomUUID().toString();
		return uniqueName;
	}

	public static boolean isJpegOrPng(MultipartFile imageFile) {
		String contentType = imageFile.getContentType();
		return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
	}

	public static boolean isPng(MultipartFile imageFile) {
		String contentType = imageFile.getContentType();
		return contentType != null && contentType.equals("image/png");
	}
}