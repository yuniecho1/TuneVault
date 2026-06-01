package com.amazonaws.samples;

import java.io.File;
import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoadMusicData {

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1").build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("music");

        JsonParser parser = new JsonFactory().createParser(new File("2026a2_songs.json"));

        JsonNode rootNode = new ObjectMapper().readTree(parser);
        Iterator<JsonNode> iter = rootNode.path("songs").elements();

        while (iter.hasNext()) {
            JsonNode currentNode = iter.next();

            String artist = currentNode.path("artist").asText();
            String title = currentNode.path("title").asText();
            int year = Integer.parseInt(currentNode.path("year").asText());
            String album = currentNode.path("album").asText();

            // correct field name (your JSON uses img_url)
            String image_url = currentNode.path("img_url").asText();

            // critical fix: composite sort key
            String albumTitle = album + "#" + title;

            try {
                table.putItem(new Item()
                        .withPrimaryKey("artist", artist, "album_title", albumTitle)
                        .withString("title", title)
                        .withString("album", album)
                        .withNumber("year", year)
                        .withString("image_url", image_url));

                System.out.println("Inserted: " + artist + " | " + title + " | " + album);

            } catch (Exception e) {
                System.err.println("Error inserting: " + title);
                System.err.println(e.getMessage());
            }
        }

        parser.close();
        System.out.println("Music data loaded!");
    }
}