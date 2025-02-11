#!/bin/bash

dot_file=$1
file_name=${1%.*} # removing the extension
new_file="${file_name}-clean.dot"

head -n 4 "$dot_file" >> "$new_file"

lines_str=$(tail -n +5 "$dot_file" | wc -l | awk ' {print $1} ')
lines_int=$(expr "$lines_str" - 1)

tail -n +5 "$dot_file" | head -n "$lines_int" | sort | uniq >> "$new_file"

echo "}" >> "$new_file"

