package com.test;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

@javax.ws.rs.Path("/save")
public class ImageResource {

    @POST
    public Response save(ImageRequest request) {
        InputStream inputStream = decodeBase64(request.getContent());
        inputStream = resizeImage(inputStream, request.getProperties().getHeight());
        save(inputStream, request.path);

        return Response.status(201).build();
    }

    private InputStream decodeBase64(String base64Content) {
        try {
            if(base64Content.startsWith("data:image")) {
                base64Content = base64Content.split(",", 2)[1];
            }
            byte[] bytes = Base64.getDecoder().decode(base64Content);
            return new ByteArrayInputStream(bytes);
    	} catch (IllegalArgumentException e) {
    		throw new RuntimeException("Failed to decode base64", e);
    	}
    }

    private InputStream resizeImage(InputStream inputStream, int height) {
        System.out.println("HEADLESS: " + GraphicsEnvironment.isHeadless());
        BufferedImage inputImage;
        try {
            inputImage = ImageIO.read(inputStream);
        } catch (IOException io) {
            throw new RuntimeException("Cannot resize image, failed to read image", io);
        }

        int currentW = inputImage.getWidth();
        int currentH = inputImage.getHeight();
        int width = currentW * height / currentH;
        if (currentH < height) {
           width = currentW;
           height = currentH;
        }
        
        Image originalImage = inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(resizedImage, "png", outputStream);
        } catch (IOException io) {
            throw new RuntimeException("Cannot resize image, failed to write resized data to oputut stream", io);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
        
    }

    private void save(InputStream inputStream, String savePath) {
        java.nio.file.Path path = Paths.get(savePath);
        // create direcotry structure before saving file so that we dont get filentofoundexception
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            // in case direcotry already exists
        }
        // create file
        try {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Problem while saving file", e);
        }
    }

    public static class ImageRequest {
        private String content;
        private String path;
        private ImageProperties properties;

        public String getContent() {
            return this.content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public ImageProperties getProperties() {
            return this.properties;
        }

        public void setProperties(ImageProperties properties) {
            this.properties = properties;
        }

    }

    public static class ImageProperties {
        private Integer height;

        public Integer getHeight() {
            return this.height;
        }
    
        public void setHeight(int height) {
            this.height = height;
        }
    }
}