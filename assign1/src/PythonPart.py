import time

def calculate_gflops(n, exec_time):
    if exec_time <= 0: return 0
    return (2.0 * n * n * n) / (exec_time * 1e9)

def on_mult(m_ar, m_br):
   
    pha = [1.0] * (m_ar * m_ar)
    phb = [0.0] * (m_br * m_br)
    phc = [0.0] * (m_ar * m_ar)

    for i in range(m_br):
        for j in range(m_br):
            phb[i * m_br + j] = float(i + 1)

    start_time = time.time()

    for i in range(m_ar):
        for j in range(m_br):
            temp = 0.0
            for k in range(m_ar):
                temp += pha[i * m_ar + k] * phb[k * m_br + j]
            phc[i * m_ar + j] = temp

    end_time = time.time()

    print(f"Time: {end_time - start_time:.3f} seconds")
    
    print("Result matrix: ")

    limit = min(10, m_br)
    for j in range(limit):
        print(int(phc[j]), end=" ")

    print()

def on_mult_line(m_ar, m_br):

    pha = [1.0] * (m_ar * m_ar)
    phb = [0.0] * (m_br * m_br)
    phc = [0.0] * (m_ar * m_ar)

    for i in range(m_br):
        for j in range(m_br):
            phb[i * m_br + j] = float(i + 1)

    start_time = time.time()

    for i in range(m_ar):
        for k in range(m_ar):
            temp = pha[i * m_ar + k]
            for j in range(m_br):
                phc[i*m_ar + j] += temp * phb[k*m_br + j]

    end_time = time.time()

    print(f"Time: {end_time - start_time:.3f} seconds")
    
    print("Result matrix: ")

    limit = min(10, m_br)
    for j in range(limit):
        print(int(phc[j]), end=" ")

    print()


def on_mult_block(m_ar, m_br, block_size):
    pha = [1.0] * (m_ar * m_ar)
    phb = [0.0] * (m_br * m_br)
    phc = [0.0] * (m_ar * m_ar)

    for i in range(m_br):
        for j in range(m_br):
            phb[i * m_br + j] = float(i + 1)

    start_time = time.time()

    for bi in range(0, m_ar, block_size):
        for bk in range(0, m_ar, block_size):
            for bj in range(0, m_br, block_size):
                
                # Inner loops (OnMultLine style)
                for i in range(bi, min(bi + block_size, m_ar)):
                    for k in range(bk, min(bk + block_size, m_ar)):
                        
                        # Get temp_a using 1D indexing: [row * width + col]
                        temp_a = pha[i * m_ar + k]
                        
                        for j in range(bj, min(bj + block_size, m_br)):
                            # Update phc using 1D indexing
                            phc[i * m_ar + j] += temp_a * phb[k * m_br + j]
        
    end_time = time.time()
    print(f"Block Time: {end_time - start_time:.3f} seconds")
    
    # Print result snippet
    limit = min(10, m_br)
    print("Result matrix (first row): ", [int(phc[x]) for x in range(limit)])

def main():
    while True:
        print("\n1. Multiplication")
        print("2. Line Multiplication")
        print("3. Block Multiplication")
        print("0. Exit")
        
        op = int(input("Selection?: "))

        if op == 0:
            break

        if op in [1, 2, 3]:
            
            lin = int(input("Dimensions: lins=cols ? "))
            col = lin

            if op == 1:
                on_mult(lin, col)
            elif op == 2:
                on_mult_line(lin, col)
            elif op == 3:
                block_size = int(input("Block Size? "))
                on_mult_block(lin, col, block_size)
        else:
            print("Invalid selection.")

if __name__ == "__main__":
    main()