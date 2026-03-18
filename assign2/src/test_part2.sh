#!/bin/bash

# Compile with optimization flag -O2 [cite: 9]
g++ -O2 -fopenmp part2.cpp -o part2

REPORT="cpd_perf_report.txt"
echo "CPD PROJECT PERFORMANCE REPORT - $(date)" > $REPORT
echo "------------------------------------------------" >> $REPORT

# PART 2, TASK 1: 4 Threads, Sizes 1024 to 3072, increment 512 [cite: 34]
echo ">>> TASK 1: V1 & V2 PERFORMANCE (4 THREADS)" >> $REPORT
for size in 1024 1536 2048 2560 3072; do
    echo "--- Size: $size ---" >> $REPORT
    # Profile V1 versions
    perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 1 $size 4 2>> $REPORT
    # Profile V2 versions
    perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 2 $size 4 2>> $REPORT
done

# PART 2, TASK 2: Size 8192, Variable Threads [cite: 64]
echo ">>> TASK 2: V2 SCALING (SIZE 8192)" >> $REPORT
for threads in 4 8 12 16 20 24; do
    echo "--- Threads: $threads ---" >> $REPORT
    perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 2 8192 $threads 2>> $REPORT
done

echo "Done! Data saved to $REPORT"