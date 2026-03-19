#!/bin/bash

g++ -O2 -fopenmp part2.cpp -o part2

REPORT="cpd_perf_report.txt"
echo "CPD PROJECT PERFORMANCE REPORT - $(date)" > $REPORT
echo "------------------------------------------------" >> $REPORT

echo ">>> TASK 1: On Mult & Mult LIne PERFORMANCE (4 THREADS)" >> $REPORT
for size in 1024 1536 2048 2560 3072; do
    echo "--- Size: $size ---" >> $REPORT
   
    perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 1 $size 4 2>> $REPORT

done

echo ">>> TASK 2: ON Mult SCALING (SIZE 8192)" >> $REPORT
for threads in 4 8 12 16 20 24; do
    echo "--- Threads: $threads ---" >> $REPORT
    perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 2 8192 $threads 2>> $REPORT
done

echo "Done! Data saved to $REPORT"