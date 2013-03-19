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

(defn sh! [& args]
  (apply println "$" args)
  (let [res (apply sh/sh args)]
    (print (:out res)) (print (:err res)) (flush)
    (when-not (zero? (:exit res)) (abort "Command failed with exit code %s: %s" (:exit res) args))))

(defn bundle-pbxis-ws [project dest-name]
  (let [tarfile (format "%s-%s.tgz" (:name project) (:version project))
        jarfile "pbxis-ws-standalone.jar"
        dest (io/file dest-name)
        dest-name (if (.isDirectory dest)
                    (str (.getPath dest) "/")
                    (abort "Destination %s is not a directory" dest-name))]
    (println "$ lein uberjar")
    (sh! "mv" (uberjar project) jarfile)
    #_(sh! "touch" jarfile)
    (sh! "tar" "cvfz" tarfile
         jarfile
         "pbxis-config.clj.template"
         "logback.xml")
    (sh! "mv" tarfile dest-name)
    (let [repo (second (leiningen.deploy/repo-for project "bundle"))
          repo-obj (Repository. "bundle" (:url repo))]
      (println "Upload" tarfile "==>" (:url repo))
      (doto (.lookup (PomegranateWagonProvider.) (.getProtocol repo-obj))
        (.connect repo-obj (doto (AuthenticationInfo.)
                             (.setUserName (:username repo))
                             (.setPassword (:password repo))
                             (.setPassphrase (:passphrase repo))))
        (.put (io/file dest tarfile) (str "bundle/" tarfile))))))

#_(let [files {[(symbol (:group project) (:name project)) (:version project) :extension "tgz"]
               tarfile}]
    (aether/deploy-artifacts
     :artifacts (keys files)
     :files files
     :transfer-listener :stdout
     :repository [(leiningen.deploy/repo-for project "releases")]))
