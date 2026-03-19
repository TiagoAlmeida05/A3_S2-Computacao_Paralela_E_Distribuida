#!/bin/bash

g++ -O2 -fopenmp part2.cpp -o part2

REPORT="cpd_perf_report.txt"
echo "CPD PROJECT PERFORMANCE REPORT - $(date)" > $REPORT
echo "------------------------------------------------" >> $REPORT

echo ">>> TASK 1: On Mult & Mult LIne PERFORMANCE (4 THREADS)" >> $REPORT
for size in 1024 1536 2048 2560 3072; do

    echo "===============================================" >> $REPORT
    echo "              MATRIX SIZE: $size               " >> $REPORT
    echo "===============================================" >> $REPORT
   
    for op in 1 2 3 4; do
        if [ $op -eq 1 ]; then alg="V1 Standard"; fi
        if [ $op -eq 2 ]; then alg="V1 Nested"; fi
        if [ $op -eq 3 ]; then alg="V2 Line Standard"; fi
        if [ $op -eq 4 ]; then alg="V2 Line Nested"; fi
        
        echo "--- Running $alg ---" >> $REPORT
        perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 $op $size 4 >> $REPORT 2>&1
        echo "" >> $REPORT
    done

done

echo ">>> TASK 2: ON Mult SCALING (SIZE 8192)" >> $REPORT
for threads in 4 8 12 16 20 24; do
    echo "===============================================" >> $REPORT
    echo "                THREADS: $threads              " >> $REPORT
    echo "===============================================" >> $REPORT
    
    for op in 5 6 7; do
        if [ $op -eq 5 ]; then alg="V2 Line Standard (Baseline)"; fi
        if [ $op -eq 6 ]; then alg="V2 Line SIMD"; fi
        if [ $op -eq 7 ]; then alg="V2 Line Collapse(2)"; fi
        
        echo "--- Running $alg ---" >> $REPORT
        perf stat -e mem_load_retired.l1_miss,mem_load_retired.l2_miss,instructions,cycles ./part2 $op 8192 $threads >> $REPORT 2>&1
        echo "" >> $REPORT
    done
done

echo "Done! Data saved to $REPORT"