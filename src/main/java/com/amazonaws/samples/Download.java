package com.amazonaws.samples;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

public class Download {

    public static void main(String[] args) {

        String tableName = "music";
        String bucketName = "a2-assignment-yuniecho-2026";

        try {
            AmazonDynamoDB dynamoClient = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .build();

            DynamoDB dynamoDB = new DynamoDB(dynamoClient);

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .build();

            Table table = dynamoDB.getTable(tableName);

            ItemCollection<ScanOutcome> items = table.scan(new ScanSpec());
            Iterator<Item> iter = items.iterator();

            // Fix 1: avoid duplicate uploads
            Set<String> uploadedArtists = new HashSet<>();

            while (iter.hasNext()) {

                Item item = iter.next();

                String artist = item.getString("artist");
                String imageUrl = item.getString("image_url");

                // Skip if already uploaded
                if (uploadedArtists.contains(artist)) continue;
                uploadedArtists.add(artist);

                System.out.println("Processing artist: " + artist);
                System.out.println("URL: " + imageUrl);

                try {
                    URL url = new URL(imageUrl);
                    InputStream inputStream = url.openStream();

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int nRead;

                    while ((nRead = inputStream.read(data)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    byte[] bytes = buffer.toByteArray();
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);

                    String safeArtist = artist.replaceAll("[^a-zA-Z0-9]", "");
                    String keyName = safeArtist + ".jpg";

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(bytes.length);
                    metadata.setContentType("image/jpeg");

                    s3Client.putObject(bucketName, keyName, byteStream, metadata);

                    System.out.println("Uploaded: " + keyName);

                    // Close streams
                    inputStream.close();
                    buffer.close();
                    byteStream.close();

                } catch (Exception e) {
                    System.err.println("Failed for artist: " + artist);
                    e.printStackTrace();
                }
            }

            System.out.println("All images uploaded");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}