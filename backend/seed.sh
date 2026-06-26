#!/bin/bash -ex

truncate -s 0 ./db/listings.db

mvn -q clean
mvn -q compile
mvn exec:java -Dexec.mainClass=cs4050e.ces.db.Seeder
