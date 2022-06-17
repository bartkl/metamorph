# TODO

## General
[ ] Make `supported-file-exts` into a parameter of the RDF read functions.
[ ] Is there perhaps some use for `:a/owns` for blank nodes?
[ ] Write a macro or function for the `condp` hash-map logic you use with properties. Or maybe `reverse-args` on `get` would be sufficient.
[ ] Does `sh:and` work well with self-references? Probably infinite recursion.

## AVRO
[ ] Look into the `required` hack again. Take Chad Herrington's response into account on the [GitHub issue](https://github.com/deercreeklabs/lancaster/issues/20).


## SQL
[ ] Focus first on getting one input node shape converted to DDL.
    [ ] Primitives, primary keys, foreign keys
    [ ] Derivations using `sh:and`
    [ ] Cardinality: many-many
    [ ] Enums
[ ] Then, make sure all the tables are done and reduce it to a single DDL script.
    - Only create tables for those node shapes which aren't referenced in any `sh:and`.