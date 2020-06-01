package eu.doppel_helix.github.action.apache_commit_info;

import eu.doppel_helix.github.action.apache_commit_info.icla.ICLAInfo;
import eu.doppel_helix.github.action.apache_commit_info.icla.ICLAInfoNoId;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Check the ICLA sigature status of a given person identified by a real name
 */
public class ICLAChecker {

    private static final String COMMITTER_SOURCE = "https://whimsy.apache.org/public/icla-info.json";
    private static final String NON_COMMITTER_SOURCE = "https://whimsy.apache.org/public/icla-info_noid.json";

    private Set<String> committerSet = new HashSet<>();
    private Set<String> nonCommitterSet = new HashSet<>();

    public ICLAChecker() throws IOException {
        init();
    }

    public boolean isSignee(String name) {
        String normalizedName = normalize(name);
        return committerSet.contains(normalizedName)
            || nonCommitterSet.contains(normalizedName);
    }

    private static String normalize(String input) {
        return input
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s*", "");
    }

    private void init() throws IOException {
        try {
            ObjectMapper om = new ObjectMapper();
            try (InputStream is = new URL(COMMITTER_SOURCE).openStream()) {
                ICLAInfo info = om.readValue(is, ICLAInfo.class);
                committerSet = info.getCommitters()
                    .values()
                    .stream()
                    .map(ICLAChecker::normalize)
                    .collect(Collectors.toUnmodifiableSet());
            }
            try (InputStream is = new URL(NON_COMMITTER_SOURCE).openStream()) {
                ICLAInfoNoId info = om.readValue(is, ICLAInfoNoId.class);
                nonCommitterSet = info.getNonCommitters()
                    .stream()
                    .map(ICLAChecker::normalize)
                    .collect(Collectors.toUnmodifiableSet());
            }
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
    }
}
