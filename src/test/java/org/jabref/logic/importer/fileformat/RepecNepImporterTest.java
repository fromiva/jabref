package org.jabref.logic.importer.fileformat;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jabref.logic.importer.ImportFormatPreferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepecNepImporterTest {

    private static final String FILE_ENDING = ".txt";

    private RepecNepImporter testImporter;

    @BeforeEach
    public void setUp() {
        ImportFormatPreferences importFormatPreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
        when(importFormatPreferences.bibEntryPreferences().getKeywordSeparator()).thenReturn(',');

        testImporter = new RepecNepImporter(importFormatPreferences);
    }

    private static Stream<String> fileNames() throws IOException {
        Predicate<String> fileName = name -> name.startsWith("RepecNepImporter")
                && name.endsWith(FILE_ENDING);
        return ImporterTestEngine.getTestFiles(fileName).stream();
    }

    private static Stream<String> invalidFileNames() throws IOException {
        Predicate<String> fileName = name -> !name.contains("RepecNepImporter");
        return ImporterTestEngine.getTestFiles(fileName).stream();
    }

    @ParameterizedTest
    @MethodSource("fileNames")
    public void isRecognizedFormat(String fileName) throws IOException {
        ImporterTestEngine.testIsRecognizedFormat(testImporter, fileName);
    }

    @ParameterizedTest
    @MethodSource("invalidFileNames")
    public void isNotRecognizedFormat(String fileName) throws IOException {
        ImporterTestEngine.testIsNotRecognizedFormat(testImporter, fileName);
    }

    @ParameterizedTest
    @MethodSource("fileNames")
    public void importEntries(String fileName) throws Exception {
        ImporterTestEngine.testImportEntries(testImporter, fileName, FILE_ENDING);
    }
}
