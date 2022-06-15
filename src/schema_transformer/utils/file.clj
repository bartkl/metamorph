(ns schema-transformer.utils.file)

(defn ext [f]
  (last
   (re-find #"\.([^.\\/:*\"?<>|\r\n]+)$" (.getName f))))