# Scala [http4s](https://http4s.org/) [htmx](https://htmx.org/) Demo

A very simple demonstration of the use of [htmx](https://http4s.org/) with a [http4s](https://http4s.org/) server together 
with [twirl](https://github.com/playframework/twirl) templates.

This application implements the classic 'To Do' application with a database (JDBC) backend. 

## Using this Project

Once the program is up and running: 
```
2024-01-17 19:50:25.464 INFO  [io-compute-5] n.m.d.HtmxDemoServer - Ember-Server service bound to address: [::]:8080
```

point your browser to `http://localhost:8080` and you should see the following page: -

![todo app](todo-app.png)

## Logging

Change the [logback.xml](src/main/resources/logback.xml) file to see/hide debug messages.

## Database
To 

## Details
The database integration is implemented with [Doobie](https://tpolecat.github.io/doobie/)  a pure functional
JDBC layer for Scala and [Cats](https://typelevel.org/cats-effect/)
