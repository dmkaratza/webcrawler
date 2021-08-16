## Webcrawler using Akka Streams

1. Configure an absolute path at `output-path` in the application.conf for the downloaded (crawled) pages

2. Build the fat jar with:
```
sbt assembly
```

3. Run the application as a Java process:
```
java -jar "-Dstart-url=https://en.wikipedia.org/wiki/Akka_(toolkit)" -Ddepth=2 -Dlinks-limit=3 $jar_path/webcrawler.jar
````
