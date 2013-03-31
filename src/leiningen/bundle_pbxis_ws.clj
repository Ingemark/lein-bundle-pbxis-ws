(ns leiningen.bundle-pbxis-ws
  (require (leiningen [uberjar :refer [uberjar]] [deploy :refer [repo-for]])
           [leiningen.core.main :as main]
           (clojure.java [shell :as sh] [io :as io])
           [clojure.string :as s]
           [cemerick.pomegranate.aether :as aether])
  (import cemerick.pomegranate.aether.PomegranateWagonProvider
          org.apache.maven.wagon.repository.Repository
          org.apache.maven.wagon.authentication.AuthenticationInfo))

(set! *warn-on-reflection* true)

(defn abort [fmt & args] (main/abort (apply format fmt args)))

(defn sh! [& cmd]
  (apply println "$" cmd)
  (let [res (eval/sh cmd)]
    (when-not (zero? res) (abort "Command failed with exit code %s: %s" res cmd))
    res))

(defn bundle-pbxis-ws
  "Bundle the pbxis-ws project"
  ([project] (bundle-pbxis-ws project "."))
  ([project dest-name]
     (let [tgz-path (let [dest-dir (io/file dest-name)]
                      (if (.isDirectory dest-dir)
                        (-> dest-dir
                            (io/file (format "%s-%s.tgz" (:name project) (:version project)))
                            .getPath)
                        (abort "Destination %s is not a directory" dest-name)))
           jarfile (do (println "$ lein uberjar") (io/file (uberjar project)))
           moved-jarfile (io/file (.getName jarfile))
           config-file (io/file "pbxis-config.clj")]
       (.renameTo jarfile moved-jarfile)
       (.renameTo (io/file "pbxis-config.clj.template") config-file)
       (sh! "tar" "cvfz" tgz-path
            (.getPath moved-jarfile)
            (.getPath config-file)
            "logback.xml"
            "README.md")
       tgz-path)))
