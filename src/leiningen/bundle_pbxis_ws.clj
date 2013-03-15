(ns leiningen.bundle-pbxis-ws
  (require [leiningen.uberjar :refer [uberjar]]
           (clojure.java [shell :as sh] [io :as io])
           [clojure.string :as s]))

(set! *warn-on-reflection* true)

(defn raise [fmt & args] (throw (RuntimeException. ^String (apply format fmt args))))

(defn sh! [& args]
  (apply println "$" args)
  (let [res (apply sh/sh args)]
    (print (:out res)) (print (:err res)) (flush)
    (when-not (zero? (:exit res)) (raise "Command failed with exit code %s: %s" (:exit res) args))))

(defn bundle-pbxis-ws [project dest]
  (let [tarfile (format "pbxis-ws-%s.tgz" (:version project))
        jarfile "pbxis-ws-standalone.jar"
        dest (let [d (io/file dest)] (if (.isDirectory d)
                                       (str (.getPath d) "/")
                                       (raise "Destination %s is not a directory" dest)))]
    (println "lein uberjar")
    (sh! "mv" (uberjar project) jarfile)
    (sh! "tar" "cvfz" tarfile
         jarfile
         "pbxis-config.clj.template"
         "logback.xml")
    (sh! "mv" tarfile dest)))
