package io.github.pskenny.libjfreecycle.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.pskenny.libjfreecycle.model.Post;

public class PostUtil {

    // Default amount of results to retrieve
    private static final int DEFAULT_RESULTS_SIZE = 100;

    private PostUtil() {
    }

    /**
     * Return up to hundred most recent posts from the given group.
     * 
     * @param groupId Group ID to search
     * @return Posts
     */
    public static Collection<Post> getPosts(String groupId) {
        return getPosts(groupId, Post.Type.ALL);
    }

    /**
     * Return up to hundred most recent posts of type given from group given.
     * 
     * @param groupId Name of group to retrieve posts
     * @param type    Post type
     * @return Posts
     */
    public static Collection<Post> getPosts(String groupId, Post.Type type) {
        return getPosts(groupId, type, DEFAULT_RESULTS_SIZE);
    }

    /**
     * Return hundred most recent posts of type given from group given.
     * 
     * @param groupId Name of group to retrieve posts
     * @param type    Post type
     * @param results Maximum results to return (1 - 100)
     * @return Posts
     */
    public static Collection<Post> getPosts(String groupId, Post.Type type, int results) {
        ArrayList<Post> posts = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);

        final String url = new StringBuilder().append("http://groups.freecycle.org/group/").append(groupId)
                .append("/posts/").append(type.name().toLowerCase()).append("?resultsperpage=").append(results)
                .toString();
        try {
            Document doc = Jsoup.connect(url).get();
            Element table = doc.getElementById("group_posts_table");

            // no results
            if (table == null)
                return posts;
            
            Elements tableRow = table.getElementsByTag("tr");
            tableRow.forEach(x -> {
                Post p = parsePostsFromTableRow(x, formatter);
                posts.add(p);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return posts;
    }

    private static Post parsePostsFromTableRow(Element row, SimpleDateFormat formatter) {
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