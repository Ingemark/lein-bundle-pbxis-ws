(defproject com.ingemark/lein-bundle-pbxis-ws "0.1.6-SNAPSHOT"
  :description "Creates a .tgz bundle of the pbxis-ws project"
  :url "https://github.com/Inge-mark/bundle-pbxis-ws"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in :leiningen
  :lein-release {:deploy-via :clojars}

  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[com.ingemark/lein-release "2.1.1"]])

