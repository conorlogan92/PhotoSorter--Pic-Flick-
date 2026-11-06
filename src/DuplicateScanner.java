import javafx.concurrent.Task;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateScanner extends Task<List<DuplicateScanner.Match>> {
    public static class Match {
        private final Path original;
        private final Path duplicate;
        private final String fingerprint;

        public Match(Path original, Path duplicate, String fingerprint) {
            this.original = original;
            this.duplicate = duplicate;
            this.fingerprint = fingerprint;
        }

        public Path getOriginal() { return original; }
        public Path getDuplicate() { return duplicate; }
        public String getFingerprint() { return fingerprint; }

        @Override
        public String toString() {
            return duplicate.getFileName() + "  (matches " + original.getFileName() + ")";
        }
    }

    private final List<Path> photos;

    public DuplicateScanner(List<Path> photos) {
        this.photos = new ArrayList<>(photos);
    }

    @Override
    protected List<Match> call() throws Exception {
        updateMessage("Checking file sizes...");
        Map<Long, List<Path>> bySize = new HashMap<>();
        for (Path path : photos) {
            if (isCancelled()) return new ArrayList<>();
            try {
                if (Files.isRegularFile(path)) {
                    bySize.computeIfAbsent(Files.size(path), k -> new ArrayList<>()).add(path);
                }
            } catch (IOException ignored) {
                // The user may have moved this photo while the scan was running.
            }
        }

        List<Path> candidates = new ArrayList<>();
        for (List<Path> group : bySize.values()) {
            if (group.size() > 1) candidates.addAll(group);
        }
        candidates.sort(Comparator.comparing(Path::toString));

        Map<String, Path> firstByFingerprint = new HashMap<>();
        List<Match> matches = new ArrayList<>();
        int processed = 0;
        updateProgress(0, Math.max(1, candidates.size()));
        for (Path path : candidates) {
            if (isCancelled()) return matches;
            try {
                String hash = sha256(path);
                Path first = firstByFingerprint.putIfAbsent(hash, path);
                if (first != null) matches.add(new Match(first, path, hash));
            } catch (IOException ignored) {
                // A photo moved while hashing is skipped safely.
            }
            processed++;
            updateProgress(processed, Math.max(1, candidates.size()));
            updateMessage("Checked " + processed + " of " + candidates.size() + " candidates");
        }
        updateMessage("Scan complete");
        return matches;
    }

    public static String sha256(Path path) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported", e);
        }
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new IOException("Scan interrupted");
                }
                digest.update(buffer, 0, read);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) result.append(String.format("%02x", b & 0xff));
        return result.toString();
    }
}
