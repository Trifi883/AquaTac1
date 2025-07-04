package com.example.aquatac1;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 1. Prevent duplicate initialization
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        // 2. More robust resource loading
        ClassPathResource resource = new ClassPathResource("aquatac-48bd3-firebase-adminsdk-fbsvc-0a5cd67402.json");

        // 3. Explicitly close the stream using try-with-resources
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://aquatac-48bd3-default-rtdb.firebaseio.com") // Removed trailing slash
                    .build();

            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase() throws IOException {
        // 4. Configure database instance properly
        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseApp());
        database.setPersistenceEnabled(false); // Enable if offline access needed
        return database;
    }
}