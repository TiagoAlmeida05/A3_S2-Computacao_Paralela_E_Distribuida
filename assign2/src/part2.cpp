#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <cstdlib>
#include <omp.h>
#include <algorithm>

using namespace std;

// Helper to calculate GFlops [cite: 18, 65]
double calculateGFlops(int n, double time) {
    if (time <= 0) return 0;
    return (2.0 * (double)n * n * n) / (time * 1e9);
}

void initMatrices(double* a, double* b, double* c, int n) {
    for (int i = 0; i < n * n; i++) {
        a[i] = 1.0;
        b[i] = (double)(i / n + 1);
        c[i] = 0.0; 
    }
}

// 1. STANDARD MULTIPLICATION (V1)
// Task 1: Approach A - Parallel Outer Loop 
void OnMultV1_Standard(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    #pragma omp parallel for
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            double temp = 0;
            for (int k = 0; k < n; k++) {
                temp += pha[i * n + k] * phb[k * n + j];
            }
            phc[i * n + j] = temp;
        }
    }
    double end = omp_get_wtime();
    printf("V1 Outer Par | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

// Task 1: Approach B - Nested Parallelism [cite: 47, 52]
void OnMultV1_Nested(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    #pragma omp parallel // Parallel region starts here [cite: 47]
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            double temp = 0;
            #pragma omp for // Work-sharing on the inner loop 
            for (int k = 0; k < n; k++) {
                temp += pha[i * n + k] * phb[k * n + j];
            }
            phc[i * n + j] = temp;
        }
    }
    double end = omp_get_wtime();
    printf("V1 Nested    | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

// 2. LINE-BY-LINE MULTIPLICATION (V2)
// Task 2: Standard Parallel [cite: 59]
void OnMultV2_Standard(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    #pragma omp parallel for
    for (int i = 0; i < n; i++) {
        for (int k = 0; k < n; k++) {
            for (int j = 0; j < n; j++) {
                phc[i * n + j] += pha[i * n + k] * phb[k * n + j];
            }
        }
    }
    double end = omp_get_wtime();
    printf("V2 Standard  | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

// Task 2: Exploration of SIMD [cite: 69]
void OnMultV2_SIMD(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    #pragma omp parallel for
    for (int i = 0; i < n; i++) {
        for (int k = 0; k < n; k++) {
            double temp_a = pha[i * n + k];
            #pragma omp simd // Vectorize the innermost loop [cite: 69]
            for (int j = 0; j < n; j++) {
                phc[i * n + j] += temp_a * phb[k * n + j];
            }
        }
    }
    double end = omp_get_wtime();
    printf("V2 SIMD      | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

// 3. BLOCK-ORIENTED MULTIPLICATION (V3)
void OnMultV3_Parallel(int n, int bkSize, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    // Only parallelize the block-row loop to avoid race conditions on phc
    #pragma omp parallel for 
    for (int bi = 0; bi < n; bi += bkSize) {
        for (int bk = 0; bk < n; bk += bkSize) {
            for (int bj = 0; bj < n; bj += bkSize) {
                for (int i = bi; i < min(bi + bkSize, n); i++) {
                    for (int k = bk; k < min(bk + bkSize, n); k++) {
                        double temp_a = pha[i * n + k];
                        for (int j = bj; j < min(bj + bkSize, n); j++) {
                            phc[i * n + j] += temp_a * phb[k * n + j];
                        }
                    }
                }
            }
        }
    }
    double end = omp_get_wtime();
    printf("V3 Parallel  | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}


int main(int argc, char *argv[]) {
    int op, lin, threads, blockSize = 64;

    if (argc >= 4) {
        op = atoi(argv[1]);
        lin = atoi(argv[2]);
        threads = atoi(argv[3]);
        if (argc == 5) blockSize = atoi(argv[4]);

        if (op == 1) { 
            OnMultV1_Standard(lin, threads); // Parallel outer loop [cite: 35]
            OnMultV1_Nested(lin, threads);   // Parallel inner loop [cite: 52]
        }
        else if (op == 2) { 
            OnMultV2_Standard(lin, threads); // Standard i,k,j [cite: 58]
            OnMultV2_SIMD(lin, threads);     // Using SIMD [cite: 69]
        }
        else if (op == 3) { 
            OnMultV3_Parallel(lin, blockSize, threads); // Block version [cite: 13]
        }
        return 0;
    }

    // Your original menu for manual testing
    do {
        cout << endl << "--- CPD Project 1 Menu ---" << endl;
        cout << "1. V1 (Outer vs Nested)" << endl;
        cout << "2. V2 (Standard vs SIMD)" << endl;
        cout << "3. V3 (Block Oriented)" << endl;
        cout << "0. Exit" << endl;
        cout << "Selection: "; cin >> op;
        if (op == 0) break;
        cout << "Size: "; cin >> lin;
        cout << "Threads: "; cin >> threads;
        if (op == 3) { cout << "Block Size: "; cin >> blockSize; }

        if (op == 1) { OnMultV1_Standard(lin, threads); OnMultV1_Nested(lin, threads); }
        if (op == 2) { OnMultV2_Standard(lin, threads); OnMultV2_SIMD(lin, threads); }
        if (op == 3) { OnMultV3_Parallel(lin, blockSize, threads); }
    } while (op != 0);

    return 0;
}