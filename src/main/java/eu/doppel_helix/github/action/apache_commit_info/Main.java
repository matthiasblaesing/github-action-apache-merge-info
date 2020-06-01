
package eu.doppel_helix.github.action.apache_commit_info;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    // GitHub requires new lines in comments to be windows style
    private static final String NL = "\r\n";
    // Mark the comments we create
    private static final String MARKER_COMMENT = "<!-- Autocomment Commit Summary Bot -->";

    public static void main(String[] args) throws IOException {
        // The workflow definition must make the workflow access token available
        // via the environment variable GITHUB_TOKEN (it comes from
        // secrets.GUTHUB_TOKEN and must be mapped to an environment variable by
        // the workflow definition)
        //
        // Get access to the root object for the github bindings in online mode
        // authenticated with the workflow token
        GitHub github = new GitHubBuilder()
            .withJwtToken(System.getenv("GITHUB_TOKEN"))
            .build();

        // The environment variable GITHUB_EVENT_PATH is made available by
        // default and contains the full event data of the pull request
        GHEventPayload.PullRequest pullRequestEvent;
        try (Reader r = Files.newBufferedReader(Path.of(System.getenv("GITHUB_EVENT_PATH")), UTF_8)) {
            pullRequestEvent = github.parseEventPayload(r, GHEventPayload.PullRequest.class);
        }

        // Ensure the action sees the current data of the PR, not the data at
        // the time the event snapshot was created
        GHPullRequest pullRequest = pullRequestEvent.getPullRequest();
        pullRequest.refresh();

        // create a text summarizing the PR data
        String newComment = formatInformationComment(pullRequest);

        // Check the existing comments of the PR, which are marked to come
        // from this bot (relying on a html comment at the end of the comment
        // body
        List<GHIssueComment> botcomments = new ArrayList<>();
        for(GHIssueComment ic: pullRequest.listComments()) {
            if(ic.getBody().endsWith(MARKER_COMMENT)) {
                botcomments.add(ic);
            }
        }

        // Remove all comments from this bot (will cause the summary to be
        // placed after the current comments, if commented out, the first
        // bot comment will be updated).
////        Disabled (update existing comment to keep notifications to a minimum)
//        for(GHIssueComment ic: new ArrayList<>(botcomments)) {
//            ic.delete();
//            botcomments.remove(ic);
//        }

        if(botcomments.isEmpty()) {
            // If there is not existing bot comment, add a new comment with
            // the summary
            pullRequest.comment(newComment);
        } else {
            // If there is already one bot comment, the first bot comment is
            // updated and the further bot comments are removed
            botcomments.get(0).update(newComment);
            for(int i = 1; i < botcomments.size(); i++) {
                botcomments.get(i).delete();
            }
        }
    }

    // Generate a summary of the PR:
    // - show the public data of the account that created the PR
    // - list the commits with SHA, _real_ author information and abbreviated description
    // - list a summary of the authors of with their ICLA signee state
    // - create a list of hints a committer should take into account
    // - the current timestamp
    static String formatInformationComment(GHPullRequest pullRequest) throws IOException {
        GHUser ghUser = pullRequest.getUser();
        StringBuilder sb = new StringBuilder();
        sb.append("Public Information PR requestor: ");
        sb.append(ghUser.getName());
        sb.append(" \\<");
        sb.append(ghUser.getEmail());
        sb.append("\\>");
        sb.append(NL);
        sb.append(NL);
        sb.append(NL);
        // This creates a table for the commit data
        sb.append("|SHA|Author|Short description|");
        sb.append(NL);
        sb.append("| --- | --- | --- |");
        sb.append(NL);
        Set<Author> authors = new HashSet<>();
        Set<String> authorNames = new HashSet<>();
        for (GHPullRequestCommitDetail c : pullRequest.listCommits()) {
            authorNames.add(c.getCommit().getAuthor().getName());
            authors.add(new Author(c.getCommit().getAuthor().getName(), c.getCommit().getAuthor().getEmail()));
            sb.append(String.format("| %1$s | %2$s<br />\\<%3$s\\> | %4$.50s |", c.getSha(), c.getCommit().getAuthor().getName(), c.getCommit().getAuthor().getEmail(), extractCommitSummary(c.getCommit().getMessage()), c.getCommit().getAuthor().getDate()));
            sb.append(NL);
        }

        boolean nonIclaAuthor = false;
        // List the authors of the commits and their ICLA signee status
        sb.append(NL);
        sb.append("| Author | ICLA status |");
        sb.append(NL);
        sb.append("| --- | --- |");
        sb.append(NL);
        ICLAChecker iclaChecker = new ICLAChecker();
        for(String author: authorNames) {
            boolean isICLASignee = iclaChecker.isSignee(author);
            sb.append(String.format("| %1$s | %2$s |", author, isICLASignee ? "found" : "-"));
            sb.append(NL);
            nonIclaAuthor |= (! isICLASignee);
        }
        sb.append(NL);

        // Check for situations where advises can be given to a committer
        Set<String> hints = new HashSet<>();

        if(authors.size() > 1) {
            hints.add("Multiple authors found - squashing is discouraged.");
        }
        for(Author a: authors) {
            if(! (a.getEmail().equals(ghUser.getEmail()) &&
                a.getName().equals(ghUser.getName()))) {
                hints.add("Author information is inconsistent with PR requestor - squashing is discouraged.");
            }
        }

        if(nonIclaAuthor) {
            hints.add("For at least one author no ICLA could be found, please ensure, that an ICLA is on file for all authors or they are aware that they donate the code to the ASF");
        }

        if(! hints.isEmpty()) {
            sb.append(NL);
            sb.append("**Hints**");
            sb.append(NL);
            for(String hint: hints) {
                sb.append("- ");
                sb.append(hint);
                sb.append(NL);
            }
        }

        // Report the time this commment was last updated
        sb.append(NL);
        sb.append("State: ");
        sb.append(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        sb.append(NL);
        // ensure our comment can be identified as created by the bot
        sb.append(MARKER_COMMENT);
        String newComment = sb.toString();
        return newComment;
    }


    // Extract the first line from the commit message
    private static final Pattern firstlineExtractor = Pattern.compile("^([^\n\r]*).*", Pattern.DOTALL);
    static String extractCommitSummary(String commitMessage) {
        if(commitMessage == null) {
            return "";
        }
        Matcher lineMatcher = firstlineExtractor.matcher(commitMessage);
        if(lineMatcher.matches()) {
            return lineMatcher.group(1);
        } else {
            return commitMessage;
        }
    }
}
