#!/bin/bash
g++ -O2 -fopenmp CppPart.c++ -o CppPart

REPORT="part1_report.txt"
# Use > ONLY for the very first line to create/clear the file
echo "--- CPD PROJECT 1 PART 1 DATA COLLECTION ---" > $REPORT
echo "--- ALL C++ TIMES ---"  >> $REPORT 

# ---------------------------------------------------------
# C++ SECTION
# ---------------------------------------------------------

# On Mult
echo ">>> COLLECTING C++ On Mult" >> $REPORT
for lines in 1024 1536 2048 2560 3072; do
    echo "Running V1 Size: $lines"
    printf "1\n$lines\n0\n" | perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,cycles,instructions ./CppPart >> $REPORT 2>&1
done

# Mult Line
echo ">>> COLLECTING C++ Line Mult (Up to 3072)" >> $REPORT
for lines in 1024 1536 2048 2560 3072; do
    echo "Running V2 Size: $lines"
    printf "2\n$lines\n0\n" | perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,cycles,instructions ./CppPart >> $REPORT 2>&1
done

echo ">>> COLLECTING C++ Line Mult (Huge Sizes)" >> $REPORT
for lines in 4096 6144 8192 10240; do
    echo "Running V2 Huge Size: $lines"
    printf "2\n$lines\n0\n" | perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,cycles,instructions ./CppPart >> $REPORT 2>&1
done

# Block Mult
echo ">>> COLLECTING C++ Block Mult" >> $REPORT
for lines in 4096 6144 8192 10240; do
    for block in 128 256 512; do
        echo "Running V3 Size: $lines Block: $block"
        printf "3\n$lines\n$block\n0\n" | perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,cycles,instructions ./CppPart >> $REPORT 2>&1
    done
done

# ---------------------------------------------------------
# PYTHON SECTION
# ---------------------------------------------------------

echo "--- ALL PYTHON DATA ---" >> $REPORT 

# onMult
echo ">>> COLLECTING PYTHON VERSION 1" >> $REPORT
for lines in 1024 1536 2048 2560 3072; do
    echo "Running Py V1 Size: $lines (Warning: 2560+ will take hours)"
    printf "1\n$lines\n0\n" | perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,cycles,instructions python3 PythonPart.py >> $REPORT 2>&1
done

# Line Mult

echo ">>> COLLECTING PYTHON VERSION 2" >> $REPORT
for lines in 1024 1536 2048 2560 3072; do
    echo "Running Py V2 Size: $lines"
    printf "2\n$lines\n0\n" | perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,cycles,instructions python3 PythonPart.py >> $REPORT 2>&1
done

echo "All data captured in $REPORT!"