![GitHub](https://img.shields.io/github/license/pskenny/libjfreecycle)

# libjfreecycle

__NOTE:__ In alpha stage, do not use

libjfreecycle is a Java library for scraping [freecycle.org](https://www.freecycle.org/).

libjfreecycle lazy loads group and post information i.e. only getting information when specific methods for retrieving them are called. Not getting all information on Group object creation. Example:

```java
// Creating a group does not retrieve any information from freecycle.org
Group group = new Group("GalwayIE");

// Retrieves 10 most recent posts now
ArrayList<Post> posts = group.getPosts();
```
