package com.example.user.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class JsonMapper {
    private final ObjectMapper objectMapper;

    public JsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }



    public List<Map<String, Object>> parseJsonResponse(String jsonResponse) {
        try {

            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            //JsonNode categoriesNode = jsonNode;

            List<Map<String, Object>> nodes = new ArrayList<>();

            if (jsonNode != null) {
                Iterator<JsonNode> iterator = jsonNode.elements();

                while (iterator.hasNext()) {
                    JsonNode objectNode = iterator.next();

                    // Convert JSON node to a Map
                    Map<String, Object> nodeMap = objectMapper.convertValue(objectNode, Map.class);

                    nodes.add(nodeMap);
                }

            }

            return nodes;
        } catch (Exception e) {
            // Handle exception (e.g., log it) and return an empty list or throw an exception
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Map<String, Object> parseJsonObjectResponse(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // Handle exception (e.g., log it) and return an empty map or throw an exception
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public List<String> parseJsonArrayToStringList(String jsonArray) {
        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // Handle exception (e.g., log it) and return an empty list or throw an exception
            e.printStackTrace();
            return Collections.emptyList();
        }
    }






}
