package org.jabref.logic.search;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.importer.fileformat.BibtexImporter;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.search.rules.SearchRules;
import org.jabref.model.util.DummyFileUpdateMonitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class SearchFunctionalityTest {

    private BibDatabase database;
    BibEntry entry1A = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry1")
            .withField(StandardField.AUTHOR, "Test")
            .withField(StandardField.TITLE, "cASe");
    BibEntry entry2A = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry2")
            .withField(StandardField.AUTHOR, "test")
            .withField(StandardField.TITLE, "casE");
    BibEntry entry3A = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry3")
            .withField(StandardField.AUTHOR, "tESt")
            .withField(StandardField.TITLE, "Case");
    BibEntry entry4A = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry4")
            .withField(StandardField.AUTHOR, "tesT")
            .withField(StandardField.TITLE, "CASE");
    BibEntry entry5A = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry5")
            .withField(StandardField.AUTHOR, "TEST")
            .withField(StandardField.TITLE, "case");
    BibEntry entry1B = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry1")
            .withField(StandardField.AUTHOR, "Test")
            .withField(StandardField.TITLE, "Case");
    BibEntry entry2B = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry2")
            .withField(StandardField.AUTHOR, "User")
            .withField(StandardField.TITLE, "case");
    BibEntry entry3B = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry3")
            .withField(StandardField.AUTHOR, "test")
            .withField(StandardField.TITLE, "text");
    BibEntry entry4B = new BibEntry(StandardEntryType.Misc)
            .withCitationKey("entry4")
            .withField(StandardField.AUTHOR, "Special")
            .withField(StandardField.TITLE, "192? title.");
    @BeforeEach
    public void setUp() {
        database = new BibDatabase();
    }

    private void initializeDatabaseFromPath(Path testFile) throws IOException {
        ParserResult result = new BibtexImporter(mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS), new DummyFileUpdateMonitor()).importDatabase(testFile);
        BibDatabaseContext context = new BibDatabaseContext(result.getDatabase(), result.getMetaData());

        database = context.getDatabase();
    }

    @Test
    public void testEmptyLibrarySearch() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("empty.bib").toURI()));

        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("Test", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testUpperAndLowerWordSearch() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-A.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("Test", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(List.of(entry1A, entry2A, entry3A, entry4A, entry5A), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("Best", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testSimpleSingleFieldSearch() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-A.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("author=Test", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(List.of(entry1A, entry2A, entry3A, entry4A, entry5A), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("author=Case", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testSimpleMultipleFieldSearch() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-A.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("author=Test and title=Case", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(List.of(entry1A, entry2A, entry3A, entry4A, entry5A), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("author=Test and title=Test", EnumSet.noneOf(SearchRules.SearchFlags.class)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testSensitiveWordSearch() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-A.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("Test", EnumSet.of(SearchRules.SearchFlags.CASE_SENSITIVE)), database).getMatches();
        assertEquals(List.of(entry1A), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("TesT", EnumSet.of(SearchRules.SearchFlags.CASE_SENSITIVE)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testSensitiveMultipleFieldSearch() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-A.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("author=Test and title=cASe", EnumSet.of(SearchRules.SearchFlags.CASE_SENSITIVE)), database).getMatches();
        assertEquals(List.of(entry1A), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("author=Test and title=case", EnumSet.of(SearchRules.SearchFlags.CASE_SENSITIVE)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testSimpleRegularExpression() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-B.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("[/9]", EnumSet.of(SearchRules.SearchFlags.REGULAR_EXPRESSION)), database).getMatches();
        assertEquals(List.of(entry4B), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("[/8]", EnumSet.of(SearchRules.SearchFlags.REGULAR_EXPRESSION)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }

    @Test
    public void testSensitiveRegularExpression() throws IOException, URISyntaxException {
        initializeDatabaseFromPath(Path.of(SearchFunctionalityTest.class.getResource("test-library-B.bib").toURI()));

        //Positive search test
        List<BibEntry> matches = new DatabaseSearcher(new SearchQuery("\\bCase\\b", EnumSet.of(SearchRules.SearchFlags.REGULAR_EXPRESSION, SearchRules.SearchFlags.CASE_SENSITIVE)), database).getMatches();
        assertEquals(List.of(entry1B), matches);
        //Negative search test
        matches = new DatabaseSearcher(new SearchQuery("\\bCas\\b", EnumSet.of(SearchRules.SearchFlags.REGULAR_EXPRESSION, SearchRules.SearchFlags.CASE_SENSITIVE)), database).getMatches();
        assertEquals(Collections.emptyList(), matches);
    }
}


