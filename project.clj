(defproject sign "0.0.1-SNAPSHOT"
  :description "sign"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [http-kit "2.1.19"]
                 [ring/ring-core "1.3.1" :exclusions [org.clojure/tools.reader clj-time joda-time]]
                 [metosin/compojure-api "0.16.2" :exclusions [com.fasterxml.jackson.core/jackson-core org.clojure/tools.macro]]
                 [com.fasterxml.jackson.core/jackson-core "2.3.2"]
                 [org.clojure/tools.macro "0.1.1"]
                 [hiccup "1.0.5"]
                 [prismatic/schema "0.3.0"]
                 [commons-codec/commons-codec "1.9"]
                 [org.clojure/tools.nrepl "0.2.6"]
                 [slingshot "0.11.0"]
                 [org.clojure/clojurescript "0.0-2356" :scope "provided"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [om "0.7.3" :scope "provided"]
                 [prismatic/om-tools "0.3.3" :exclusions [org.clojure/clojure com.cemerick/piggieback] :scope "provided"]
                 [cljs-ajax "0.3.2" :scope "provided"]
                 [im.chit/purnam.test "0.4.4" :scope "provided"]
                 [sablono "0.2.22" :scope "provided"]]
  :profiles {:dev {:source-paths ["generated/clj" "src/clj"]
                   :dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-cljsbuild "1.0.3"]
                             [com.keminglabs/cljx "0.4.0"]]
                   :jvm-opts ["-Xverify:none"]
                   :cljx {:builds [{:rules :clj
                                    :source-paths ["src/cljx"]
                                    :output-path "generated/clj"}
                                   {:rules :cljs
                                    :source-paths ["src/cljx"]
                                    :output-path "generated/cljs"}]}}
             :uberjar {:cljx {:builds [{:rules :clj
                                        :source-paths ["src/cljx"]
                                        :output-path "generated/clj"}
                                       {:rules :cljs
                                        :source-paths ["src/cljx"]
                                        :output-path "generated/cljs"}]}
                       :source-paths ^:replace ["generated/clj" "src/clj" "src/clj-main"]
                       :main sign.main
                       :aot [sign.main]}}
  :clean-targets ["generated"]
  :cljsbuild {:builds {:dev {:source-paths ["generated/cljs" "src/cljs" "src/cljs-main"]
                             :compiler {:output-to "resources/public/sign.js"
                                        :output-dir "resources/public/out"
                                        :source-map "resources/public/sign.js.map"
                                        :optimizations :none
                                        :pretty-print true
                                        :preamble ["react/react.js"]
                                        :externs ["react/externs/react.js"]}}
                       :uberjar {:source-paths ["generated/cljs" "src/cljs" "src/cljs-main"]
                                 :compiler {:output-to "resources/public/sign.js"
                                            :output-dir "target/js/out-prod"
                                            :optimizations :advanced
                                            :pretty-print false
                                            :elide-asserts true
                                            :preamble ["react/react.min.js"]
                                            :externs ["react/externs/react.js"]}}}}
  :uberjar-name "sign.jar"
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :aliases {"uberjar" ["do" ["cljx" "once"] ["cljsbuild" "clean"] ["cljsbuild" "once" "uberjar"] ["uberjar"]]}
  :min-lein-version "2.3.4")
