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
     * Return ten most recent posts of type given.
     * 
     * @param type Post type
     * @return Posts
     */
    public Collection<Post> getPosts(Post.Type type) {
        return getPosts(type, DEFAULT_RESULTS_SIZE);
    }

    /**
     * Return hundred most recent posts of type given from group given.
     * 
     * Note: results must be between 10 - 100. Any number out of these bounds returns 10 posts.
     * 
     * @param type    Post type
     * @param results Maximum results to return
     * @return Posts
     */
    public Collection<Post> getPosts(Post.Type type, int results) {
        ArrayList<Post> posts = new ArrayList<>();

        final String url = buildURL(type, results);
        try {
            Document doc = Jsoup.connect(url).get();
            Element table = doc.getElementById("group_posts_table");

            // table isn't in DOM, therefore no results
            if (table == null)
                return posts;
            
            Elements tableRow = table.getElementsByTag("tr");
            tableRow.forEach(x -> {
                Post post = parsePostsFromTableRow(x, DEFAULT_DATE_FORMAT);
                posts.add(post);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return posts;
    }

    /**
     * Create freecycle.org results page url.
     */
    private String buildURL(Post.Type type, int results) {
        return new StringBuilder().append("http://groups.freecycle.org/group/").append(groupId).append("/posts/")
                .append(type.name().toLowerCase()).append("?resultsperpage=").append(results).toString();
    }

    private Post parsePostsFromTableRow(Element row, SimpleDateFormat formatter) {
        // ☠️ Beware of the String/DOM manipulation filth that follows ☠️
        // Table column 1 contains post type (OFFER/WANTED), date/time, post id
        String column1 = row.child(0).text();
        String[] splitCol1 = column1.split(" ");
        String type = splitCol1[1];
        String datetime = column1.substring((splitCol1[0].length() + splitCol1[1].length() + 2),
                column1.length() - (splitCol1[7].length() + 1));
        long postId = Long
                .parseLong(splitCol1[splitCol1.length - 1].substring(2, splitCol1[splitCol1.length - 1].length() - 1));
        // Table column 2 contains the title and location
        String column2 = row.child(1).text();
        String title = row.child(1).child(0).text();
        String location = column2.substring(title.length() + 2, column2.length() - 13);

        Post post = new Post();
        if (type.equals("OFFER")) {
            post.setType(Post.Type.OFFER);
        } else {
            post.setType(Post.Type.WANTED);
        }
        post.setId(postId);
        post.setTitle(title);
        post.setLocation(location);

        try {
            post.setDate(formatter.parse(datetime));
        } catch (java.text.ParseException ex) {
            // Don't care atm
        }

        return post;
    }
}