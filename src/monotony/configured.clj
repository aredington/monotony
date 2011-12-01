(ns ^{:doc "Namespace for working with Monotony using with-config"
      :author "Alex Redington"}
  monotony.configured
  (:require [monotony.core :as m]
            [fidjet.core :as f]))

(f/remap-ns-with-arg monotony.core config)
