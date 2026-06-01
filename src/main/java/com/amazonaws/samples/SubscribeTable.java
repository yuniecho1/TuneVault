package com.amazonaws.samples;

import java.util.Arrays;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;

public class SubscribeTable {

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = "subscribe";

        try {
            System.out.println("Creating subscribe table...");

            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                            new KeySchemaElement("email", KeyType.HASH),
                            new KeySchemaElement("song_id", KeyType.RANGE)
                    ),
                    Arrays.asList(
                            new AttributeDefinition("email", ScalarAttributeType.S),
                            new AttributeDefinition("song_id", ScalarAttributeType.S)
                    ),
                    new ProvisionedThroughput(5L, 5L));

            table.waitForActive();
            System.out.println("Subscribe table created!");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}