package org.jabref.logic.util.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jabref.logic.citationkeypattern.CitationKeyGenerator;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.strings.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CitationKeyBasedFileFinder implements FileFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CitationKeyBasedFileFinder.class);

    private final boolean exactKeyOnly;

    CitationKeyBasedFileFinder(boolean exactKeyOnly) {
        this.exactKeyOnly = exactKeyOnly;
    }

    @Override
    public List<Path> findAssociatedFiles(BibEntry entry, List<Path> directories, List<String> extensions) throws IOException {
        Objects.requireNonNull(directories);
        Objects.requireNonNull(entry);

        Optional<String> citeKeyOptional = entry.getCitationKey();
        if (StringUtil.isBlank(citeKeyOptional)) {
            LOGGER.debug("No citation key found in entry {}", entry);
            return Collections.emptyList();
        }
        String citeKey = citeKeyOptional.get();

        List<Path> result = new ArrayList<>();

        // First scan directories
        Set<Path> filesWithExtension = findFilesByExtension(directories, extensions);

        // Now look for keys
        for (Path file : filesWithExtension) {
            String name = file.getFileName().toString();
            String nameWithoutExtension = FileUtil.getBaseName(name);

            // First, look for exact matches
            if (nameWithoutExtension.equals(citeKey)) {
                LOGGER.debug("Found exact match for key {} in file {}", citeKey, file);
                result.add(file);
                continue;
            }
            // If we get here, we did not find any exact matches. If non-exact matches are allowed, try to find one
            if (!exactKeyOnly && matches(name, citeKey)) {
                LOGGER.debug("Found non-exact match for key {} in file {}", citeKey, file);
                result.add(file);
            }
        }

        return result.stream().sorted().toList();
    }

    private boolean matches(String filename, String citeKey) {
        boolean startsWithKey = filename.startsWith(FileNameCleaner.cleanFileName(citeKey));
        if (startsWithKey) {
            // The file name starts with the key, that's already a good start
            // However, we do not want to match "JabRefa" for "JabRef" since this is probably a file belonging to another entry published in the same time / same name
            char charAfterKey = filename.charAt(citeKey.length());
            return !CitationKeyGenerator.APPENDIX_CHARACTERS.contains(Character.toString(charAfterKey));
        }
        return false;
    }

    /**
     * Returns a list of all files in the given directories which have one of the given extension.
     */
    private Set<Path> findFilesByExtension(List<Path> directories, List<String> extensions) throws IOException {
        Objects.requireNonNull(extensions, "Extensions must not be null!");

        BiPredicate<Path, BasicFileAttributes> isFileWithCorrectExtension = (path, attributes) -> !Files.isDirectory(path)
                && extensions.contains(FileUtil.getFileExtension(path).orElse(""));

        Set<Path> result = new HashSet<>();
        for (Path directory : directories) {
            if (Files.exists(directory)) {
                try (Stream<Path> pathStream = Files.find(directory, Integer.MAX_VALUE, isFileWithCorrectExtension, FileVisitOption.FOLLOW_LINKS)) {
                    result.addAll(pathStream.collect(Collectors.toSet()));
                } catch (UncheckedIOException e) {
                    throw new IOException("Problem in finding files", e);
                }
            }
        }
        return result;
    }
}
