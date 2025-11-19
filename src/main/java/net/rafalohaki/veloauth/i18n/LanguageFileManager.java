package net.rafalohaki.veloauth.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Manages external language files for VeloAuth.
 * Handles initialization, loading, and validation of language files from the filesystem.
 */
public final class LanguageFileManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageFileManager.class);
    private final Path langDirectory;
    
    /**
     * Creates a new LanguageFileManager.
     *
     * @param dataDirectory The plugin's data directory (plugins/VeloAuth/)
     */
    public LanguageFileManager(Path dataDirectory) {
        this.langDirectory = dataDirectory.resolve("lang");
    }
    
    /**
     * Initializes the language file system.
     * Creates the lang directory if it doesn't exist and copies default language files from JAR.
     *
     * @throws IOException if directory creation or file copying fails
     */
    public void initializeLanguageFiles() throws IOException {
        // Create lang directory if it doesn't exist
        if (!Files.exists(langDirectory)) {
            Files.createDirectories(langDirectory);
            logger.info("Created language directory: {}", langDirectory);
        }
        
        // Copy only built-in default language files from JAR if they don't exist
        copyDefaultLanguageFile("messages_en.properties");
        copyDefaultLanguageFile("messages_pl.properties");
        // Users can add custom language files (e.g., messages_de.properties) to this directory
    }
    
    /**
     * Copies a default language file from the JAR to the external lang directory.
     * Only copies if the file doesn't already exist externally.
     *
     * @param filename The language file name (e.g., "messages_en.properties")
     * @throws IOException if file copying fails
     */
    private void copyDefaultLanguageFile(String filename) throws IOException {
        Path targetFile = langDirectory.resolve(filename);
        
        if (!Files.exists(targetFile)) {
            try (InputStream is = getClass().getResourceAsStream("/lang/" + filename)) {
                if (is == null) {
                    logger.error("Default language file not found in JAR: {}", filename);
                    return;
                }
                Files.copy(is, targetFile);
                logger.info("Copied default language file: {}", filename);
            }
        } else {
            logger.debug("Language file already exists: {}", filename);
        }
    }
    
    /**
     * Loads a language bundle from the external lang directory.
     * Falls back to English if the requested language is not found.
     *
     * @param language The language code (e.g., "en", "pl")
     * @return ResourceBundle containing the language strings
     * @throws IOException if the language file cannot be loaded
     */
    public ResourceBundle loadLanguageBundle(String language) throws IOException {
        String filename = "messages_" + language + ".properties";
        Path languageFile = langDirectory.resolve(filename);
        
        if (!Files.exists(languageFile)) {
            logger.warn("Language file not found: {}, falling back to English", filename);
            languageFile = langDirectory.resolve("messages_en.properties");
        }
        
        if (!Files.exists(languageFile)) {
            throw new IOException("English fallback language file not found at: " + languageFile);
        }
        
        try (InputStream is = Files.newInputStream(languageFile);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }
    
    /**
     * Validates that a language file can be loaded successfully.
     * Logs validation results.
     *
     * @param language The language code to validate
     */
    public void validateLanguageFile(String language) {
        try {
            ResourceBundle bundle = loadLanguageBundle(language);
            logger.info("Validated language file for: {}", language);
            logger.debug("Language file contains {} keys", bundle.keySet().size());
        } catch (IOException e) {
            logger.error("Failed to validate language file for: {}", language, e);
        }
    }
}
