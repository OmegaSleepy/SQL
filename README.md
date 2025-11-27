# SQLnow

A lightweight Java library for SQL database interaction.

## Overview

This project is aimed to provide a convenient way to connect, query, and manage SQL databases using Java. It is designed to be simple, efficient, and easily integrable into your own Java applications.

## Features

-   Easy SQL query parsing
-   Support for executing queries and updates
-   Methods for handling result sets and transactions
-   A lot of utility functions for common SQL operations

## Installation

Add the compiled JAR to your project's classpath. (Maven/Gradle support is not currently planned.)

## Usage

```java
// Example: Set up, query and end the program.

        long start = System.nanoTime();
        Log.cleanUp();

        File credentials = new File("credentials.txt");
        sql.Credentials.inputCredentialFile(credentials);
        sql.SqlConnection.initializeConnection();

        Query.getResult("use music");
        Query.fromFile(FileUtil.getScriptFile("initTable.txt"));
        

        Log.error("Something went wrong?");
        Script.endScript(start, System.nanoTime());
```

## Requirements

-   Java 21 or newer
-   A supported SQL driver (e.g., MySQL, PostgreSQL)

## API Reference

-   `SqlConnection` — Main class for inializing a connection
-   `Query` — Class holding multiple type of queries
-   `Log` — Logger class for pretty display of messages and select operations
-   `Script.end()` — Close the program and save logs

## Resources

-   Most of the project is documented with the use of JavaDocs inside of the classes. 
-   The project includes sample scripts that may or may not be deleted in near future.

## Contributing

Contributions are not really welcome. This repo is mostly for me to learn and optimize how to manage databases with Java. You can freely fork the repo and open a pull request..

Oh also don't use Eclipse if you plan on contributing. Use VS Code or IntelliJ.

## License

MIT

---

*Made with ☕ and ❤ by [OmegaSleepy](https://github.com/OmegaSleepy)*