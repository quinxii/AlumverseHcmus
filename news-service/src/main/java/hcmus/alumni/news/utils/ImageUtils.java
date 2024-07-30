package hcmus.alumni.news.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
	@Autowired
	private ImageCompression imageCompression;

	private final int resizeMaxWidth = 2000;
	private final int resizeMaxHeight = 2000;
	private final String avatarPath = "images/users/avatar/";
	private final String newsPath = "images/news/";
	public static int saltLength = 16;

	// Save MultipartFile Image
	public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile, String imageName)
			throws IOException {
		if (imageFile == null) {
			return null;
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

		BlobInfo blobInfo = BlobInfo.newBuilder(gcp.getBucketName(), newFilename).setContentType(extracted[0])
				.setCacheControl("no-cache").build();

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

		try {
			Blob blob = gcp.getStorage().get(gcp.getBucketName(), extractedImageName);
			if (blob == null) {
				return;
			}
			gcp.getStorage().delete(gcp.getBucketName(), extractedImageName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public String updateImgFromHtmlToStorage(String oldHtml, String newHtml, String id) {
		// Parse the HTML content
		Document oldDoc = Jsoup.parse(oldHtml);
		Document newDoc = Jsoup.parse(newHtml);

		List<String> oldImgs = this.getImgSrcList(oldDoc);
		List<String> newImgs = this.getImgSrcList(newDoc);

		List<String> addedImgs = this.findAddedImg(oldImgs, newImgs);
		List<String> deletedImgs = this.findDeletedImg(oldImgs, newImgs);

		// Delete deleted images
		if (deletedImgs.size() != 0) {
			for (String deletedImg : deletedImgs) {
				this.deleteImageFromStorageByUrl(deletedImg);
				oldImgs.remove(deletedImg);
			}
		}

		// Save added images
		if (addedImgs.size() != 0) {
			List<Integer> contentIdxs = new ArrayList<Integer>();
			for (String img : oldImgs) {
				Integer idx = Integer.valueOf(img.substring(img.lastIndexOf("/") + 1));
				contentIdxs.add(idx);
			}

			try {
				for (String addedImg : addedImgs) {
					if (!isBase64Image(addedImg)) {
						continue;
					}
					int smalletMissingIdx = this.findSmallestMissingContentIdx(contentIdxs);
					String newIdx = String.valueOf(smalletMissingIdx);
					String newSrc = this.saveBase64ImageToStorage(this.getNewsPath(id), addedImg, newIdx);
					newDoc.select("img[src=" + addedImg + "]").attr("src", newSrc);
					contentIdxs.add(smalletMissingIdx);
				}
			} catch (IOException e) {
				System.err.println(e);
				return null;
			}
		}

		newDoc.outputSettings().indentAmount(0).prettyPrint(false);
		return newDoc.body().html();
	}

	public boolean isBase64Image(String base64) {
		String[] strings = base64.split(",");
		switch (strings[0]) {// check image's extension
			case "data:image/jpeg;base64":
				return true;
			case "data:image/png;base64":
				return true;
			default:
				return false;
		}
	}

	public String[] extractContentTypeAndDataFromImageBase64(String base64) {
		String[] strings = base64.split(",");
		switch (strings[0]) {// check image's extension
			case "data:image/jpeg;base64":
				strings[0] = "image/jpeg";
				break;
			case "data:image/png;base64":
				strings[0] = "image/png";
				break;
			default:
				throw new IllegalArgumentException("Unsupported image format: " + strings[0]);
		}
		return strings;
	}

	private List<String> getImgSrcList(Document doc) {
		List<String> imgSrcList = new ArrayList<String>();
		Elements imgTags = doc.select("img");

		for (Element imgTag : imgTags) {
			String src = imgTag.attr("src");
			imgSrcList.add(src);
		}
		return imgSrcList;
	}

	private List<String> findDeletedImg(List<String> oldImgs, List<String> newImgs) {
		List<String> deletedImgs = new ArrayList<String>();
		for (String oldImg : oldImgs) {
			if (!newImgs.contains(oldImg)) {
				deletedImgs.add(oldImg);
			}
		}
		return deletedImgs;
	}

	private List<String> findAddedImg(List<String> oldImgs, List<String> newImgs) {
		List<String> addedImgs = new ArrayList<String>();
		for (String newImg : newImgs) {
			if (!oldImgs.contains(newImg)) {
				addedImgs.add(newImg);
			}
		}
		return addedImgs;
	}

	private int findSmallestMissingContentIdx(List<Integer> contentIdxs) {
		boolean[] flags = new boolean[contentIdxs.size()];
		for (Integer idx : contentIdxs) {
			if (idx < flags.length) {
				flags[idx] = true;
			}
		}
		for (int i = 0; i < flags.length; i++) {
			if (!flags[i]) {
				return i;
			}
		}
		return flags.length;
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

	public String getNewsPath(String id) {
		return this.newsPath + id + "/";
	}
}