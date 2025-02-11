#!/bin/bash

dot_file=$1
new_file=$2
dot_dir="dot/"
file_out="${new_file} ${dot_dir}"

head -n 4 $dot_file >> $new_file

lines_str=$(tail -n +5 $dot_file | wc -l | awk ' {print $1} ')
lines_int=$(expr $lines_str - 1)

tail -n +5 $dot_file | head -n $lines_int | sort | uniq >> $new_file 

echo "}" >> $new_file

