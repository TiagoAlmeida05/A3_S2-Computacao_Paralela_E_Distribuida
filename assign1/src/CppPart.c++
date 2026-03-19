#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>

using namespace std;

#define SYSTEMTIME clock_t


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
 
void OnMult(int m_ar, int m_br) 
{
    SYSTEMTIME Time1, Time2;
    
    char st[100];
    double temp;
    int i, j, k;

    double *pha, *phb, *phc;

    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    initMatrices(pha, phb, phc, m_ar);

    Time1 = clock();

    for(i = 0; i < m_ar; i++)
    {
        for(j = 0; j < m_br; j++)
        {
            temp = 0;
            for(k = 0; k < m_ar; k++)
            {    
                temp += pha[i*m_ar + k] * phb[k*m_br + j];
            }
            phc[i*m_ar + j] = temp;
        }
    }

    Time2 = clock();
    snprintf(st, sizeof(st), "Lines/Cols: %2d  |  Line Time: %3.3f seconds  |",
         m_ar, (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    printf("  GFLOPS: %3.2f\n", calculateGFlops(m_ar, (double)(Time2 - Time1) / CLOCKS_PER_SEC));

    free(pha);
    free(phb);
    free(phc);
}


// Line-by-line matrix multiplication
void OnMultLine(int m_ar, int m_br)
{
   SYSTEMTIME Time1, Time2;

   char st[100];
   int i, j, k;

   double *pha, *phb, *phc;

    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));


    initMatrices(pha, phb, phc, m_ar);

    
    Time1 = clock();

    for(i = 0; i < m_ar; i++) 
    {
        for(k = 0; k < m_ar; k++) 
        {
            double temp_a = pha[i * m_ar + k]; 
            
            for(j = 0; j < m_br; j++) 
            {
                phc[i * m_ar + j] += temp_a * phb[k * m_br + j];
            }
        }
    }

    Time2 = clock();

    snprintf(st, sizeof(st), "Lines/Cols: %2d  |  Line Time: %3.3f seconds  |",
         m_ar, (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    printf("  GFLOPS: %3.2f\n", calculateGFlops(m_ar, (double)(Time2 - Time1) / CLOCKS_PER_SEC));

    free(pha);
    free(phb);
    free(phc);
    
    
}


// Block matrix multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    SYSTEMTIME Time1, Time2;
    char st[100];
    int bi, bj, bk, i, j, k;

    double *pha, *phb, *phc;

    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    initMatrices(pha, phb, phc, m_ar);

    
    Time1 = clock();

    for (bi = 0; bi < m_ar; bi += bkSize) { 
        for (bk = 0; bk < m_ar; bk += bkSize) { 
            for (bj = 0; bj < m_br; bj += bkSize) { 
                
                for (i = bi; i < bi + bkSize && i < m_ar; i++) {
                    for (k = bk; k < bk + bkSize && k < m_ar; k++) {
                        
                        double temp_a = pha[i * m_ar + k]; 
                        
                        for (j = bj; j < bj + bkSize && j < m_br; j++) {
                            phc[i * m_ar + j] += temp_a * phb[k * m_br + j];
                        }
                    }
                }
            }
        }
    }

    Time2 = clock();

    snprintf(st, sizeof(st), "Lines/Cols: %2d  |  Blocks: %2d  |  Line Time: %3.3f seconds  |",
         m_ar, bkSize, (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    printf("  GFLOPS: %3.2f\n", calculateGFlops(m_ar, (double)(Time2 - Time1) / CLOCKS_PER_SEC));

    free(pha);
    free(phb);
    free(phc);
}


int main(int argc, char *argv[])
{
    int lin, col, blockSize;
    int op;

    if (argc >=3) {
        op = atoi(argv[1]);
        lin = atoi(argv[2]);
        col = lin;
        if (op == 1) { 
            OnMult(lin,col); 
        }
        else if (op == 2) { 
            OnMultLine(lin, col); 
        }
        else if (op == 3) { 
            blockSize = atoi(argv[3]);
            OnMultBlock(lin, col, blockSize); 
        }
        return 0;
    }
    return 0;
}