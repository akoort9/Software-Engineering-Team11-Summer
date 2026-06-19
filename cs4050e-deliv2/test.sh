#!/bin/zsh

truncate -s 0 ./db/listings.db

mvn -q clean
mvn -q compile
mvn test
