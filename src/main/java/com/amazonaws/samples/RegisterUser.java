package com.amazonaws.samples;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.*;

public class RegisterUser {

    public static void main(String[] args) {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        String tableName = "login";

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        try {
            // Step 1: Check if user already exists
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("email", new AttributeValue(email));

            GetItemRequest getRequest = new GetItemRequest()
                    .withTableName(tableName)
                    .withKey(key);

            Map<String, AttributeValue> result = client.getItem(getRequest).getItem();

            if (result != null) {
                System.out.println("User already exists. Registration failed.");
                return;
            }

            // Step 2: Insert new user
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("email", new AttributeValue(email));
            item.put("user_name", new AttributeValue(username));
            item.put("password", new AttributeValue(password));

            PutItemRequest putRequest = new PutItemRequest()
                    .withTableName(tableName)
                    .withItem(item);

            client.putItem(putRequest);

            System.out.println("User registered successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }

        sc.close();
    }
}