src/metamorph/schemas/sql/schema.clj:(defn node-shapes->ddl [ns]
src/metamorph/schemas/sql/schema.clj:   (->> (node-shapes->ddl ns)
src/metamorph/core.clj:(def node-shapes-names
src/metamorph/core.clj:(def node-shapes (->> node-shapes-names
src/metamorph/core.clj:(->> (sql.schema/node-shapes->schema node-shapes)
