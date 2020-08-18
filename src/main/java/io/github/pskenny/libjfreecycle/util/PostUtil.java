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

    private PostUtil(){}

    public static Collection<Post> getPosts(String groupId) {
        return getPosts(groupId, Post.Type.ALL);
    }

    public static Collection<Post> getPosts(String groupId, Post.Type type) {
        ArrayList<Post> posts = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);

        final String url = new StringBuilder().append("http://groups.freecycle.org/group/").append(groupId)
                .append("/posts/").append(type.name().toLowerCase()).toString();
        try {
            Document doc = Jsoup.connect(url).get();
            Element table = doc.getElementById("group_posts_table");
            Elements tableRow = table.getElementsByTag("tr");
            
            tableRow.forEach(x -> {
                Post p = parsePostFromTableRow(x, formatter);
                posts.add(p);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return posts;
    }

    private static Post parsePostFromTableRow(Element row, SimpleDateFormat formatter) {
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