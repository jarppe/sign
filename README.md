# foozzaa

## Install

```
lein uberjar
```

## Development run

- `lein cljx auto` to start and watch cljx cross compilation
- `lein cljsbuild auto` to start and watch ClojureScript compilation
- `lein repl` & `(run)` to start the server
- http://localhost:8080

## Code organization

|Purpose                   |Folder                    |Notes:                     |
|--------------------------|--------------------------|---------------------------|
|Clojure sources           |`src/clj`                 |                           |
|ClojureScript sources     |`src/cljs`                |                           |
|common clj/cljs sources   |`src/cljx`                |                           |
|Überjar main              |`src/clj-main`            |                           |
|UI main                   |`src/cljs-main`           |                           |
|Generated resources       |`resources/public`        |                           |
|cljx compiled clj srcs    |`target/generated/clj`    |                           |
|cljx compiled cljs srcs   |`target/generated/cljs`   |                           |
