package io.github.pskenny.libjfreecycle;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.pskenny.libjfreecycle.util.PostsUtil;

/**
 * Object to handle groups and posts in a group e.g. retrieving posts from a
 * group.
 * 
 * @author Paul Kenny
 */
public class Group {

    private String groupId;

    public Group(String groupId) {
        this.groupId = groupId;
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
        return getPosts(type, PostsUtil.DEFAULT_RESULTS_SIZE);
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
        int postsSize = getPostsSize(type);

        if (postsSize == 0)
            // no posts
            return posts;

        int pages = getMaxPages(postsSize);
        posts.addAll(loadPages(type, pages));

        return posts;
    }

    /**
     * Returns highest page number needed to traverse freecycle.org pagination.
     * 
     * @param postsSize Number of posts
     * @return Highest page number
     */
    private int getMaxPages(int postsSize) {
        if (postsSize <= PostsUtil.DEFAULT_RESULTS_SIZE) {
            // Fits in one page
            return 1;
        } else {
            double size = (double) postsSize;
            return (int) Math.ceil(size / PostsUtil.DEFAULT_RESULTS_SIZE);
        }
    }

    /**
     * Returns posts of a given type up to maxPages.
     * 
     * @param type     Post type
     * @param maxPages Pages to parse
     * @return Posts from pages
     */
    private ArrayList<Post> loadPages(Post.Type type, int maxPages) {
        ArrayList<Post> posts = new ArrayList<>();

        // Iterate through page numbers, get posts from each and add it to total posts
        IntStream.rangeClosed(1, maxPages).parallel().forEach((page) -> {
            ArrayList<Post> pagePosts = loadPage(type, page);
            posts.addAll(pagePosts);
        });

        return posts;
    }

    /**
     * Get posts from page of given type.
     * 
     * @param type Post type
     * @param page Page to retrieve
     * @return Posts scraped from page
     */
    private ArrayList<Post> loadPage(Post.Type type, int page) {
        ArrayList<Post> posts = new ArrayList<>();

        try {
            final String url = PostsUtil.buildPostsURL(groupId, type, page, PostsUtil.DEFAULT_RESULTS_SIZE);
            Document doc = Jsoup.connect(url).get();
            Element table = doc.getElementById("group_posts_table");

            // table isn't in DOM, therefore no (more) results
            if (table == null)
                return posts;

            Elements tableRow = table.getElementsByTag("tr");
            // Iterate through, get post from each table row and add it to total posts
            tableRow.forEach(x -> {
                Post post = parsePostsFromTableRow(x, PostsUtil.DEFAULT_DATE_FORMAT);
                if (post != null)
                    posts.add(post);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return posts;
    }

    /**
     * Parses group_posts_table table row element into a Post object.
     * 
     * @return Post from table row
     */
    private Post parsePostsFromTableRow(Element row, SimpleDateFormat formatter) {
        String type = row.child(0).child(0).child(0).ownText();
        String dateTime = row.child(0).ownText().substring(0, row.child(0).ownText().lastIndexOf(' '));
        // magic numbers (+ 3 and - 1) in substring removes " (#" and ")"
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
        } catch (Exception ex) {
            return null;
        }

        return post;
    }

    /**
     * Returns number of posts of a given type.
     * 
     * @param type Type of posts to count
     * @return Number of posts
     */
    public int getPostsSize(Post.Type type) {
        int size = 0;
        final String url = PostsUtil.buildPostsURL(groupId, type, 1, PostsUtil.DEFAULT_RESULTS_SIZE);

        try {
            Document doc = Jsoup.connect(url).get();
            Element groupBox = doc.getElementById("group_box");
            size = Integer.parseInt(groupBox.child(7).ownText().split(" ")[5]);
            Element table = doc.getElementById("group_posts_table");

            // table isn't in the DOM, therefore there are no posts
            if (table == null)
                return size;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return size;
    }
}