package com.example.restapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
public class Controller {

    @Value("${merriam.api.key}")
    private String apiKey;

    private static final List<String> WORDS = List.of(
            "serendipity", "eloquent", "ephemeral", "luminous",
            "benevolent", "melancholy", "labyrinth", "ineffable"
    );

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @GetMapping("/random-word")
    public Map<String, Object> getRandomWordDefinition() {
        // Pick a random word
        String randomWord = WORDS.get(random.nextInt(WORDS.size()));

        // Build the Merriam-Webster API URL
        String url = String.format(
                "https://www.dictionaryapi.com/api/v3/references/collegiate/json/%s?key=%s",
                randomWord, apiKey
        );

        // Call the API
        Object response = restTemplate.getForObject(url, Object.class);

        // Return both the chosen word and the API response
        return Map.of(
                "word", randomWord,
                "definition", response
        );
    }
}
