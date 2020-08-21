package io.github.pskenny.libjfreecycle.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.pskenny.libjfreecycle.Post;

/**
 * Post utility methods and values.
 * 
 * @author Paul Kenny
 */
public class PostsUtil {
    // Default amount of results to retrieve
    public static final int DEFAULT_RESULTS_SIZE = 10;
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",
            Locale.ENGLISH);

    private PostsUtil() {

    }

    /**
     * Creates freecycle.org results page URL.
     * 
     * @return freecycle.org results page URL
     */
    public static String buildPostsURL(String groupId, Post.Type type, int page, int results) {
        return new StringBuilder().append("http://groups.freecycle.org/group/").append(groupId).append("/posts/")
                .append(type.name().toLowerCase()).append("?").append("page=").append(page).append("&resultsperpage=")
                .append(results).toString();
    }
}