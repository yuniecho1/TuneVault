package com.amazonaws.samples;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.regions.Regions;

public class QuerySongs {

    public static void main(String[] args) {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("music");

        // ============================
        // QUERY BY ARTIST (PRIMARY KEY)
        // ============================

        System.out.println("\n===== QUERY BY ARTIST (PRIMARY KEY) =====");

        String artist = "Taylor Swift";
        int yearFilter = -1;
        boolean sortDescending = true;
        boolean sortByYear = true;

        try {

            ValueMap values = new ValueMap().withString(":a", artist);

            if (yearFilter != -1) {
                values.withNumber(":y", yearFilter);
            }

            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("artist = :a")
                    .withValueMap(values)
                    .withScanIndexForward(!sortDescending);

            // Optional filter (still works even though "year" is reserved here)
            if (yearFilter != -1) {
                spec.withFilterExpression("#yr = :y")
                        .withNameMap(new NameMap().with("#yr", "year"));
            }

            ItemCollection<QueryOutcome> items;

            if (sortByYear) {
                Index lsi = table.getIndex("ArtistYearIndex");
                items = lsi.query(spec);
                System.out.println("Using LSI (sorted by year)");
            } else {
                items = table.query(spec);
                System.out.println("Using main table (sorted by album_title)");
            }

            for (Item item : items) {
                System.out.println(item.toJSONPretty());
            }

        } catch (Exception e) {
            System.err.println("Error querying by artist");
            e.printStackTrace();
        }

        // ============================
        // QUERY BY ALBUM (GSI)
        // ============================

        System.out.println("\n===== QUERY BY ALBUM (GSI) =====");

        try {

            String album = "1989";

            QuerySpec gsiSpec = new QuerySpec()
                    .withKeyConditionExpression("album = :al")
                    .withValueMap(new ValueMap().withString(":al", album))
                    .withScanIndexForward(false);

            Index gsi = table.getIndex("AlbumYearIndex");
            ItemCollection<QueryOutcome> gsiItems = gsi.query(gsiSpec);

            for (Item item : gsiItems) {
                System.out.println(item.toJSONPretty());
            }

        } catch (Exception e) {
            System.err.println("Error querying by album");
            e.printStackTrace();
        }

        // ============================
        // SCAN BY TITLE (NON-KEY)
        // ============================

        System.out.println("\n===== SCAN BY TITLE =====");

        try {
            // "title" is NOT a key → must use Scan

            ScanSpec titleScan = new ScanSpec()
                    .withFilterExpression("contains(title, :t)")
                    .withValueMap(new ValueMap().withString(":t", "Love"));

            ItemCollection<ScanOutcome> titleResults = table.scan(titleScan);

            for (Item item : titleResults) {
                System.out.println(item.toJSONPretty());
            }

        } catch (Exception e) {
            System.err.println("Error scanning by title");
            e.printStackTrace();
        }

        // ============================
        // SCAN BY YEAR (NON-KEY) — FIXED
        // ============================

        System.out.println("\n===== SCAN BY YEAR =====");

        try {
            // "year" is a reserved keyword → must use alias (#yr)

            ScanSpec yearScan = new ScanSpec()
                    .withFilterExpression("#yr = :y")
                    .withNameMap(new NameMap().with("#yr", "year"))
                    .withValueMap(new ValueMap().withNumber(":y", 2017));

            ItemCollection<ScanOutcome> yearResults = table.scan(yearScan);

            for (Item item : yearResults) {
                System.out.println(item.toJSONPretty());
            }

        } catch (Exception e) {
            System.err.println("Error scanning by year");
            e.printStackTrace();
        }
    }
}