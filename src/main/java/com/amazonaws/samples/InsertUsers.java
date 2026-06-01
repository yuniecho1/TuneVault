package com.amazonaws.samples;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.*;

public class InsertUsers {

    public static void main(String[] args) {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        String tableName = "Login";

        // Fixed password sequence
        String[] passwords = {
                "012345","123456","234567","345678","456789",
                "567890","678901","789012","890123","901234"
        };

        for (int i = 0; i < 10; i++) {

            // Replace with your real student ID
            String email = "s4106619" + i + "@student.rmit.edu.au";

            // Replace with your actual name
            String username = "Ashwin Patrick" + i;

            String password = passwords[i];

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("email", new AttributeValue(email));
            item.put("user_name", new AttributeValue(username));
            item.put("password", new AttributeValue(password));

            PutItemRequest request = new PutItemRequest()
                    .withTableName(tableName)
                    .withItem(item);

            client.putItem(request);

            System.out.println("Inserted: " + email +
                    " | username: " + username +
                    " | password: " + password);
        }
    }
}