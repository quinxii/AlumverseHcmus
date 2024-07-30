package hcmus.alumni.counsel.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Service
public class ImageCompression {
    public String compressBase64Image(String base64) {
        // Decode Base64 image
        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        // Create a ByteArrayOutputStream to hold the compressed image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create a DeflaterOutputStream to compress the image
        try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream,
                new Deflater(Deflater.BEST_COMPRESSION, true));) {
            // Write the decoded bytes to the DeflaterOutputStream
            deflaterOutputStream.write(decodedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e);
        }

        // Get the compressed image bytes
        byte[] compressedBytes = outputStream.toByteArray();

        // Encode the compressed bytes to Base64
        return Base64.getEncoder().encodeToString(compressedBytes);
    }

    public byte[] compressImage(BufferedImage bufferedImage, String contentType, float quality)
            throws IOException {
        // Determine appropriate writer based on content type or output format
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(contentType.substring(contentType.lastIndexOf("/") + 1));
        if (!imageWriters.hasNext()) {
            throw new IllegalStateException("No suitable image writer found");
        }

        ImageWriter imageWriter = imageWriters.next();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        imageWriter.setOutput(imageOutputStream);

        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(quality);

        imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParam);

        return outputStream.toByteArray();
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Calculate the new dimensions while maintaining the aspect ratio
        double aspectRatio = (double) width / height;
        if (width > maxWidth || height > maxHeight) {
            if (aspectRatio > 1) {
                width = maxWidth;
                height = (int) (width / aspectRatio);
            } else {
                height = maxHeight;
                width = (int) (height * aspectRatio);
            }
        } else {
            return originalImage;
        }

        // Create a new resized image
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }
}
