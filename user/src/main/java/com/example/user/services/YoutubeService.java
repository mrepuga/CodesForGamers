package com.example.user.services;


import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonParser;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;


import javax.imageio.spi.IIORegistry;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

@Service
public class YoutubeService {
    private final YouTube youtube;

    private final String youtubeApiKey;

    public YoutubeService(@Value("${youtube.api.key}") String youtubeApiKey) throws IOException {
        this.youtubeApiKey = youtubeApiKey;
        this.youtube = initializeYouTube();
    }

    private YouTube initializeYouTube() {

        // Create the YouTube object using the API key.
        return new YouTube.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), request -> {})
                .setApplicationName("UserApplication")
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(youtubeApiKey))
                .build();
    }

    public String getVideoIdByGameName(String gameName) throws IOException {
        YouTube.Search.List search = youtube.search().list(Collections.singletonList("id"));
        search.setQ(gameName + " trailer");
        search.setType(Collections.singletonList("video"));
        search.setMaxResults(1L);

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResults = searchResponse.getItems();

        if (searchResults != null && !searchResults.isEmpty()) {
            return searchResults.get(0).getId().getVideoId();
        }

        return null;
    }
}