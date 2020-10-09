#!/usr/bin/env bb

(def cli-options
  ;; An option with a required argument
  [["-p" "--port PORT" "Port number"
    :default 8000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   [nil "--host HOSTNAME" "Host name"
    :default "localhost"]

   ["-h" "--help"]])

(defn process [& {:keys [host port output-dir]}]
  (let [dir (io/file output-dir)]
    (if (.exists dir)
      (do
        (println (format "Error: %s exists" (str dir)))
        (System/exit 1))
      (let [{:keys [exit out err]}
            (shell/sh "wget"
                      "--recursive"
                      "--timestamping"
                      "--no-directories"
                      "--convert-links"
                      (format "--directory-prefix=%s" (str dir))
                      (format "http://%s:%d/index.html" host port))]
        (if (not= exit 0)
          (do
            (println err)
            (System/exit exit))
          (println out))))))

(defn main [& args]
  (let [{:keys [options arguments errors summary]} (tools.cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (do
        (println summary)
        (System/exit 0))

      errors
      (do
        (println (str/join \newline errors))
        (System/exit 1))

      (not (seq arguments))
      (do
        (println "Argument expected")
        (System/exit 1))

      (> (count arguments) 1)
      (do
        (println "Single argument expected: " (str/join " " arguments))
        (System/exit 1))

      :else
      (do
        (process :host (:host options)
                 :port (:port options)
                 :output-dir (first arguments))))))

(apply main *command-line-args*)
