package io.github.pskenny.libjfreecycle;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Object to handle groups and posts in a group e.g. retrieving posts from a
 * group.
 * 
 * @author Paul Kenny
 */
public class Group {

    // Default amount of results to retrieve
    private final int DEFAULT_RESULTS_SIZE = 10;
    private SimpleDateFormat DEFAULT_DATE_FORMAT;
    private String groupId;

    public Group(String groupId) {
        this.groupId = groupId;

        DEFAULT_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
    }

    /**
     * Return ten most recent posts if available.
     * 
     * @return Posts
     */
    public Collection<Post> getPosts() {
        return getPosts(Post.Type.ALL);
    }

    /**
     * Return ten most recent posts of type given if available.
     * 
     * @param type Post type
     * @return Posts
     */
    public Collection<Post> getPosts(Post.Type type) {
        return getPosts(type, DEFAULT_RESULTS_SIZE);
    }

    /**
     * Returns number of results given of most recent posts of type given.
     * 
     * @param type    Post type
     * @param results Maximum results to return
     * @return Posts
     */
    public Collection<Post> getPosts(Post.Type type, int results) {
        ArrayList<Post> posts = new ArrayList<>();
        int page = 0;
        int search = results;

        // If results is more than can be displayed on a single page then request
        // maximum amount possible per page (100)
        if (results > 100)
            search = 100;

        do {
            ArrayList<Post> pagePosts = new ArrayList<>();
            final String url = buildURL(type, ++page, search);

            try {
                Document doc = Jsoup.connect(url).get();
                Element table = doc.getElementById("group_posts_table");

                // table isn't in DOM, therefore no (more) results
                if (table == null)
                    return posts;

                Elements tableRow = table.getElementsByTag("tr");
                tableRow.forEach(x -> {
                    Post post = parsePostsFromTableRow(x, DEFAULT_DATE_FORMAT);
                    pagePosts.add(post);
                });

                // Cut end of list off to not excede results argument
                // Note: This is inefficient, it requests more posts than is needed from
                // freecycle.org
                if (results - posts.size() < search) {
                    posts.addAll(pagePosts.subList(0, results - posts.size()));
                } else {
                    posts.addAll(pagePosts);
                }

                // page returned less posts than requested. It's reached the end.
                if (pagePosts.size() < search) {
                    break;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } while (posts.size() < results);

        return posts;
    }

    /**
     * Creates freecycle.org results page URL with resultsperpage attribute.
     * 
     * @return freecycle.org results page URL
     */
    private String buildURL(Post.Type type, int page, int results) {
        return new StringBuilder().append("http://groups.freecycle.org/group/").append(groupId).append("/posts/")
                .append(type.name().toLowerCase()).append("?").append("page=").append(page).append("&resultsperpage=")
                .append(results).toString();
    }

    /**
     * Parses group_posts_table table row element into a Post object.
     * 
     * @return Post
     */
    private Post parsePostsFromTableRow(Element row, SimpleDateFormat formatter) {
        String type = row.child(0).child(0).child(0).ownText();
        String dateTime = row.child(0).ownText().substring(0, row.child(0).ownText().lastIndexOf(' '));
        // magic numbers in substring removes " (#" and ")"
        String postId = row.child(0).ownText().substring(row.child(0).ownText().lastIndexOf(' ') + 3,
                row.child(0).ownText().length() - 1);
        String title = row.child(1).child(0).text();
        String location = row.child(1).ownText();
        // Remove parenthesis from location
        location = location.substring(1, location.length() - 1);

        Post post = new Post();
        if (type.equals("OFFER")) {
            post.setType(Post.Type.OFFER);
        } else {
            post.setType(Post.Type.WANTED);
        }
        post.setId(Long.parseLong(postId));
        post.setTitle(title);
        post.setLocation(location);
        try {
            post.setDate(formatter.parse(dateTime));
        } catch (java.text.ParseException ex) {
            // Don't care atm
        }

        return post;
    }
}