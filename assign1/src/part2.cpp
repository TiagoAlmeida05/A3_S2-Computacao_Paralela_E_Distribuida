#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <cstdlib>
#include <omp.h>
#include <algorithm>

using namespace std;

// Helper to calculate GFlops
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

void OnMult_Standard(int n, int threads) {
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
    printf("On Mult| Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}


void OnMult_Nested(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    #pragma omp parallel 
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            double temp = 0;
            #pragma omp for 
            for (int k = 0; k < n; k++) {
                temp += pha[i * n + k] * phb[k * n + j];
            }
            phc[i * n + j] = temp;
        }
    }
    double end = omp_get_wtime();
    printf("On Mult Nested    | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

void OnMultLine_Standard(int n, int threads) {
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
    printf("On Line | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

void OnMultLine_Nested(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    
    #pragma omp parallel
    for (int i = 0; i < n; i++) {
        for (int k = 0; k < n; k++) {
            double temp_a = pha[i * n + k];
            #pragma omp for 
            for (int j = 0; j < n; j++) {
                phc[i * n + j] += temp_a * phb[k * n + j];
            }
        }
    }
    
    double end = omp_get_wtime();
    printf("Line Mult Nested    | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

//Exploration of SIMD
void OnMultLine_SIMD(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();
    #pragma omp parallel
    for (int i = 0; i < n; i++) {
        for (int k = 0; k < n; k++) {
            double temp_a = pha[i * n + k];
            #pragma omp for simd 
            for (int j = 0; j < n; j++) {
                phc[i * n + j] += temp_a * phb[k * n + j];
            }
        }
    }
    double end = omp_get_wtime();
    printf("Line Mult SIMD      | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

void OnMultLine_Collapse(int n, int threads) {
    double *pha = (double *)malloc(n * n * sizeof(double));
    double *phb = (double *)malloc(n * n * sizeof(double));
    double *phc = (double *)malloc(n * n * sizeof(double));
    initMatrices(pha, phb, phc, n);

    omp_set_num_threads(threads);
    double start = omp_get_wtime();

    #pragma omp parallel for collapse(2)
    for (int i = 0; i < n; i++) {
        for (int k = 0; k < n; k++) {
            double temp_a = pha[i * n + k];
            for (int j = 0; j < n; j++) {
                phc[i * n + j] += temp_a * phb[k * n + j];
            }
        }
    }
    double end = omp_get_wtime();
    printf("Line Mult Collapse  | Threads: %2d | Time: %3.3fs | GFlops: %3.2f\n", threads, end-start, calculateGFlops(n, end-start));
    free(pha); free(phb); free(phc);
}

int main(int argc, char *argv[]) {
    int op, lin, threads, blockSize = 64;

    if (argc >= 4) {
        op = atoi(argv[1]);
        lin = atoi(argv[2]);
        threads = atoi(argv[3]);

        // TASK 1
        if (op == 1)      OnMult_Standard(lin, threads); 
        else if (op == 2) OnMult_Nested(lin, threads);   
        else if (op == 3) OnMultLine_Standard(lin, threads); 
        else if (op == 4) OnMultLine_Nested(lin, threads); 
        
        // TASK 2 
        else if (op == 5) OnMultLine_Standard(lin, threads); 
        else if (op == 6) OnMultLine_SIMD(lin, threads);         
        else if (op == 7) OnMultLine_Collapse(lin, threads);     
        
        return 0;
        
        return 0;
    }

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

        if (op == 1) { 
            OnMult_Standard(lin, threads); 
            OnMult_Nested(lin, threads);
            OnMultLine_Standard(lin, threads); 
            OnMultLine_Nested(lin, threads); 
        }
        if (op == 2) { OnMultLine_Standard(lin, threads); OnMultLine_SIMD(lin, threads); }
        
    } while (op != 0);

    return 0;
}