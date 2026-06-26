#!/bin/bash -ex

mvn -q clean
mvn -q compile
mvn exec:java -Dexec.mainClass=cs4050e.ces.App
