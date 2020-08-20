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

## Maven

To use this you must package and install this library to your local Maven repository using the following commands:

```bash
> git clone https://github.com/pskenny/libjfreecycle # Clone libjfreecycle
> cd libjfreecycle
> mvn package # Package libjfreecycle
> mvn install # Install to local Maven repository
```

Then add to your `pom.xml` dependencies:

```xml
    <dependency>
      <groupId>io.github.pskenny</groupId>
      <artifactId>libjfreecycle</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>
```
