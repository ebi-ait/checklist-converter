#!/bin/bash

DIRECTORY="$1"
TEXT="$2"

for FILE in "$DIRECTORY"/*
do
  if grep -q "$TEXT" "$FILE"; then
    echo "$FILE: contains required validation error"
  else
    echo "$FILE: missing validation error"
  fi
done