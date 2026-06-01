package com.amazonaws.samples;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.regions.Regions;

import java.util.Iterator;

public class countSongs {

    public static void main(String[] args) {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("music");

        int totalCount = 0;

        try {
            // Scan entire table
            ItemCollection<ScanOutcome> items = table.scan(new ScanSpec());

            Iterator<Item> iter = items.iterator();

            while (iter.hasNext()) {
                Item item = iter.next();

                totalCount++;

                // Display each song
                System.out.println(item.toJSONPretty());
            }

            // Display total count
            System.out.println("Total songs: " + totalCount);

        } catch (Exception e) {
            System.err.println("Error scanning table");
            e.printStackTrace();
        }
    }
}