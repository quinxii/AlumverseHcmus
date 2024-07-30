package hcmus.alumni.event.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
	private final String eventPath = "images/events/";
	public static int saltLength = 16;

	// Save MultipartFile Image
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile)
			throws IOException {
		if (imageFile == null) {
			return null;
		}

		String newFilename = uploadDirectory;

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

	public String getEventPath(String id) {
		return eventPath + id;
	}
}
