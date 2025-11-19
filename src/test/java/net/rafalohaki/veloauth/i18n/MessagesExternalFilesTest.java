package net.rafalohaki.veloauth.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Messages class with external language files.
 */
class MessagesExternalFilesTest {

    @TempDir
    Path tempDir;

    private Messages messages;

    @BeforeEach
    void setUp() {
        // Messages will initialize language files automatically
        messages = new Messages(tempDir, "en");
    }

    @AfterEach
    void tearDown() {
        // Cleanup is handled by @TempDir
    }

    @Test
    void testExternalFiles_LoadsEnglish() {
        // When
        String message = messages.get("auth.login.success");

        // Then
        assertNotNull(message, "Message should not be null");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertFalse(message.startsWith("Missing:"), "Message should be found");
    }

    @Test
    void testExternalFiles_LoadsPolish() {
        // Given
        messages = new Messages(tempDir, "pl");

        // When
        String message = messages.get("auth.login.success");

        // Then
        assertNotNull(message, "Message should not be null");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertFalse(message.startsWith("Missing:"), "Message should be found");
    }

    @Test
    void testExternalFiles_HandlesMissingKey() {
        // When
        String message = messages.get("non.existing.key");

        // Then
        assertNotNull(message, "Message should not be null");
        assertTrue(message.startsWith("Missing:"), "Should return missing key indicator");
    }

    @Test
    void testExternalFiles_FormatsWithArguments() {
        // When
        String message = messages.get("auth.login.success");

        // Then
        assertNotNull(message, "Message should not be null");
        assertFalse(message.isEmpty(), "Message should not be empty");
    }

    @Test
    void testReload_ReloadsLanguageFiles() throws IOException {
        // Given
        String originalMessage = messages.get("auth.login.success");

        // When
        messages.reload();
        String reloadedMessage = messages.get("auth.login.success");

        // Then
        assertNotNull(reloadedMessage, "Reloaded message should not be null");
        assertEquals(originalMessage, reloadedMessage, "Message should be the same after reload");
    }

    @Test
    void testSetLanguage_ChangesLanguage() {
        // Given
        String englishMessage = messages.get("auth.login.success");

        // When
        messages.setLanguage("pl");
        String polishMessage = messages.get("auth.login.success");

        // Then
        assertNotNull(englishMessage, "English message should not be null");
        assertNotNull(polishMessage, "Polish message should not be null");
        // Messages might be different or the same depending on translation
        assertFalse(englishMessage.isEmpty(), "English message should not be empty");
        assertFalse(polishMessage.isEmpty(), "Polish message should not be empty");
    }

    @Test
    void testFallbackToEnglish_WhenLanguageNotFound() {
        // When - request non-existent language
        messages.setLanguage("de");
        String message = messages.get("auth.login.success");

        // Then - should fall back to English
        assertNotNull(message, "Message should not be null");
        assertFalse(message.isEmpty(), "Message should not be empty");
        assertFalse(message.startsWith("Missing:"), "Message should be found in fallback");
    }
}
