(set-env!
 :source-paths    #{"sass" "src/cljs"}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs          "2.0.0"      :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.3"      :scope "test"]
                 [adzerk/boot-reload        "0.5.1"      :scope "test"]
                 [pandeiro/boot-http        "0.8.3"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.13"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]
                 [org.clojure/clojurescript "1.9.562"]
                 [org.omcljs/om "1.0.0-beta1"]
                 [deraen/boot-sass  "0.3.0" :scope "test"]
                 [org.slf4j/slf4j-nop  "1.7.21" :scope "test"]
                 [binaryage/devtools "0.9.4" :scope "test"]
                 [binaryage/dirac "1.2.9" :scope "test"]
                 [powerlaces/boot-cljs-devtools "0.2.0" :scope "test"]

                 [cljsjs/react-motion "0.5.0-0"]
                 [cljsjs/react-draggable "3.0.3-0"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[deraen.boot-sass :refer [sass]]
 '[powerlaces.boot-cljs-devtools :refer [cljs-devtools dirac]])

(deftask build
  "This task contains all the necessary steps to produce a build
   You can use 'profile-tasks' like `production` and `development`
   to change parameters (like optimizations level of the cljs compiler)"
  []
  (comp (speak)
        
        (cljs)
        
        (sass)))

(deftask run
  "The `run` task wraps the building of your application in some
   useful tools for local development: an http server, a file watcher
   a ClojureScript REPL and a hot reloading mechanism"
  []
  (comp (serve)
        (watch)
        (cljs-repl)
        
        (cljs-devtools)
        (dirac)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                      sass   {:output-style :compressed})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none}
                 reload {:on-jsload 'om-svg-toy.app/init})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))


