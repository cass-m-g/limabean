#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
psql -p 1063 $DB_NAME < $DIR/../src/create_tables.sql
psql -p 1063 $DB_NAME < $DIR/../src/create_indexes.sql
psql -p 1063 $DB_NAME < $DIR/../src/load_data.sql
