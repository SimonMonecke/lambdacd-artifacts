(ns lambdacd-artifacts.sample-pipeline
  (:use [compojure.core]
        [lambdacd.steps.control-flow])
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.steps.manualtrigger :as manualtrigger]
            [lambdacd.core :as lambdacd]
            [ring.server.standalone :as ring-server]
            [lambdacd.util :as util]
            [lambdacd.ui.ui-server :as ui]
            [lambdacd.runners :as runners]
            [ring.util.response :as resp]))

(defn some-failing-step [args ctx]
  (shell/bash ctx "/"
              "exit 1"))

(defn some-successful-step [args ctx]
  (shell/bash ctx "/"
              "echo yay"
              "exit 0"))

(defn wait-for-interaction [args ctx]
  (manualtrigger/wait-for-manual-trigger nil ctx))

(def pipeline-structure `(
 (run wait-for-interaction)
  (either
    some-failing-step
    some-successful-step)))


(defn mk-routes [ pipeline-routes]
  (routes
    (GET "/" [] (resp/redirect "pipeline/"))
    (context "/pipeline" [] pipeline-routes)))

(defn -main [& args]
  (let [home-dir (if (not (empty? args)) (first args) (util/create-temp-dir))
        config { :home-dir home-dir }
        pipeline (lambdacd/assemble-pipeline pipeline-structure config)]
    (runners/start-one-run-after-another pipeline)
    (ring-server/serve (mk-routes (ui/ui-for pipeline))
                                  {:open-browser? true
                                   :port 8081})))