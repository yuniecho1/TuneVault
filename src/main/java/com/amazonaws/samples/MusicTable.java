package com.amazonaws.samples;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;

public class MusicTable {

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = "music";

        try {
            System.out.println("Creating music table");

            // Attribute definitions
            AttributeDefinition artistAttr = new AttributeDefinition("artist", ScalarAttributeType.S);
            AttributeDefinition albumTitleAttr = new AttributeDefinition("album_title", ScalarAttributeType.S);
            AttributeDefinition albumAttr = new AttributeDefinition("album", ScalarAttributeType.S);
            AttributeDefinition yearAttr = new AttributeDefinition("year", ScalarAttributeType.N);

            // Primary Key
            KeySchemaElement artistKey = new KeySchemaElement("artist", KeyType.HASH);
            KeySchemaElement albumTitleKey = new KeySchemaElement("album_title", KeyType.RANGE);

            // LSI
            LocalSecondaryIndex lsi = new LocalSecondaryIndex()
                    .withIndexName("ArtistYearIndex")
                    .withKeySchema(
                            new KeySchemaElement("artist", KeyType.HASH),
                            new KeySchemaElement("year", KeyType.RANGE)
                    )
                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

            // GSI
            GlobalSecondaryIndex gsi = new GlobalSecondaryIndex()
                    .withIndexName("AlbumYearIndex")
                    .withKeySchema(
                            new KeySchemaElement("album", KeyType.HASH),
                            new KeySchemaElement("year", KeyType.RANGE)
                    )
                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                    .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

            // Create table
            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(tableName)
                    .withKeySchema(artistKey, albumTitleKey)
                    .withAttributeDefinitions(artistAttr, albumTitleAttr, albumAttr, yearAttr)
                    .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
                    .withLocalSecondaryIndexes(lsi)
                    .withGlobalSecondaryIndexes(gsi);

            Table table = dynamoDB.createTable(request);
            table.waitForActive();

            System.out.println("Music table created with indexes!");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}