# lambdacd-artifacts

Provides a way to access build artifacts generated by a step in [LambdaCD](https://github.com/flosell/lambdacd),
similar "archive artifacts" in Jenkins.
Use this if you want access build artifacts like test-reports or screenshots from failed selenium tests from your
browser.

## Status

[![Build Status](https://travis-ci.org/flosell/lambdacd-artifacts.svg)](https://travis-ci.org/flosell/lambdacd-artifacts)

[![Clojars Project](http://clojars.org/lambdacd-artifacts/latest-version.svg)](http://clojars.org/lambdacd-artifacts)

## Usage

```clojure
; some step that produces artifacts
(defn- produce-output [args ctx]
  (shell/bash ctx (:cwd args) "./produceSomeFiles.sh"))

; the build-step you put in your pipeline: first build artifacts, then publish them
(defn some-build-step [args ctx]
  (step-support/chain args ctx
    (produce-output)
    (artifacts/publish-artifacts (:cwd args) [#"report-folder/.*"
                                              "some-folder/someBinary.jar"])))

; url prefix where your artifacts will be available
(def artifacts-path-context "/artifacts")

(defn -main [& args]
  (let [; ...
        config {:home-dir home-dir ; this is where artifacts are stored
                :artifacts-path-context artifacts-path-context}]
    ; ...
    ; set up routes to your pipeline and to your artifacts
    (ring-server/serve (routes
                         (context "/pipeline" [] (ui/ui-for pipeline))
                         (context artifacts-path-context [] (artifacts/artifact-handler-for pipeline)))))
```

You'll find the paths to the published artifacts in the detailed step-result. The next version of LambdaCD will provide
a proper UI to display those links (see [flosell/lambdacd#37](https://github.com/flosell/lambdacd/issues/37)).

For a full example, see [test/lambdacd_artifacts/sample_pipeline.clj](test/lambdacd_artifacts/sample_pipeline.clj)

## Development

Call `./go`

## License

Copyright © 2015 Florian Sellmayr

Distributed under the Apache License 2.0
