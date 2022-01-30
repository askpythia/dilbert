# Dilbert

A simple helper to pop up a Dilbert strip dialog

## Usage

To show a Dilbert strip on a dialog call

```java
new Dilbert(username).show(DilbertType.ofTheDay)
```

Possible DilbertType types are:
- `ofTheDay`	todays (or yesterdays if not already published) strip
- `random`	random strip from all published strips
- `nextFavorite`	the next strip marked as favorite (remembers last shown favorite for each user)
- `randomFavorite`	a random strip out of all strips marked as favorites

Each user (determined by username) will only see a strip once a day, to force it use true as second parameter:

```java
new Dilbert(username).show(DilbertType.ofTheDay, true)
```

## Install

To use the component in an application using maven, add the following dependency to your pom.xml:

```
<dependency>
    <groupId>org.vaadin.addons.metainfo</groupId>
    <artifactId>dilbert</artifactId>
    <version>${component.version}</version>
</dependency>
```

## If you want to run the project 

1. Import this maven project to your favourite IDE
2. Run jetty:run, navigate to localhost:8080, and try out the different Dilbert types

## Copyright notice

Dilbert © Andrews McMeel Syndication, <https://dilbert.com>

## License

Apache License 2
