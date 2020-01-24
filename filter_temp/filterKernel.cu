#include <stdio.h>
#include <stdlib.h>
#include <time.h>

// #define START_LENGTH 134217728
#define START_LENGTH 16384
// #define START_LENGTH 8192
// #define START_LENGTH 4096
// #define START_LENGTH 2048
// #define START_LENGTH 100
#define MAX_BLOCK_SIZE 1024

// universal
int predicate(int value) {
    if( (value > 7) && (value % 2) && !(value % 3) ) {
        return 1;
    } else {
        return 0;
    }
}

// cpu
int* check_array(int *array, int length) {
    int *array_out = (int*)malloc(sizeof(int) * length);
    for(int i=0; i<length; i++) {
        array_out[i] = predicate(array[i]);
    }
    return array_out;
}

// cpu
int* prefix_sum(int *array, int length) {
    int *array_out = (int*)malloc(sizeof(int) * length);
    array_out[0] = array[0];
    for(int i=1; i<length; i++) {
        array_out[i] = array[i] + array_out[i-1];
    }
    return array_out;
}

// cpu
int* sieve_out(int *values, int *booleans, int *indexes, int *length) {
    int new_length = 0;
    for(int i=0; i<*length; i++) {
        if(booleans[i]) {
            new_length++;
        }
    }

    int *array_out = (int*)malloc(sizeof(int) * new_length);
    for(int i=0; i<*length; i++) {
        if(booleans[i]) {
            array_out[indexes[i]-1] = values[i];
        }
    }

    *length = new_length;
    return array_out;
}

void print_array(int *array, int length) {
    int *ptr = array;
    int counter = 0;
    while(ptr < array+length) {
        printf("array[%d] = %d\n", counter, *ptr);
        counter++;
        ptr++;
    }
}

// cpu
int* seq_filter(int *array, int *length) {
    int *checked = check_array(array, *length);
    int *aggregated = prefix_sum(checked, *length);
    int *filtered = sieve_out(array, checked, aggregated, length);

    return filtered;
}

// #pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable

__global__ void filterKernel(int *values, int *bools, int *prefix_sum, int *output, int *array_length, int *semaphore) {
    int threads = *array_length / 2 + *array_length % 2;
    int blocks = threads / MAX_BLOCK_SIZE + (threads % MAX_BLOCK_SIZE > 0);
    int threads_per_block = (threads + blocks-1) / blocks;
    int idx = 2*threadIdx.x + (1 - threads%2) + (threads % blocks > 0);
    int modulo = 2;

    for(int i=0; i<threads; i+=threads_per_block) {
        if(i+idx < *array_length) {
            if((values[i+idx] > 7) && (values[i+idx] % 2) && !(values[i+idx] % 3)) {
                bools[i+idx] = 1;
                prefix_sum[i+idx] = 1;
            } else {
                bools[i+idx] = 0;
                prefix_sum[i+idx] = 0;
            }

            if(i+idx > 0) {
                if((values[i+idx-1] > 7) && (values[i+idx-1] % 2) && !(values[i+idx-1] % 3)) {
                    bools[i+idx-1] = 1;
                    prefix_sum[i+idx-1] = 1;
                } else {
                    bools[i+idx-1] = 0;
                    prefix_sum[i+idx-1] = 0;
                }
            }
        }
    }

    __syncthreads();  // <---------<< 

    while(modulo < *array_length) {
        for(int i=0; i<threads; i+=threads_per_block) {
            if((i+idx % modulo) == ((*array_length-1) % modulo)) {
                if((i+idx - modulo/2) >= 0) {
                    prefix_sum[i+idx] += prefix_sum[i+idx-modulo/2];
                }
            }
        }
        __syncthreads();  // <---------<< 
        modulo *= 2;
    }

    if(threadIdx.x == 0) {  // <---------<< 
        prefix_sum[*array_length-1] = 0;
    }

    while(modulo >= 2) {
        for(int i=0; i<threads; i+=threads_per_block) {
            if((i+idx % modulo) == ((*array_length-1) % modulo)) {
                if((i+idx - modulo/2) >= 0) {
                    int temp = prefix_sum[i+idx];
                    prefix_sum[i+idx] += prefix_sum[i+idx - modulo/2];
                    prefix_sum[i+idx - modulo/2] = temp;
                }
            }
        }
        __syncthreads();  // <---------<< 
        modulo /= 2;
    }

    for(int i=0; i<threads; i+=threads_per_block) {
        if(bools[i+idx]) {
            prefix_sum[i+idx] += 1;
            output[prefix_sum[i+idx]-1] = values[i+idx];
        }
        if(((i+idx - 1) >= 0) && (bools[i+idx-1])) {
            prefix_sum[i+idx-1] += 1;
            output[prefix_sum[i+idx-1]-1] = values[i+idx-1];
        }
        __syncthreads();  // <---------<< 
    }

    if(threadIdx.x == 0) {  // <---------<< 
        *array_length = prefix_sum[*array_length-1];
    }

}

int* gpu_filter(int* array, int *length) {
    int *values, *bools, *prefix_sum, *output, *array_length, *semaphore;
    int size = sizeof(int) * *length;
    cudaMalloc(&values, size);  // <---------<< 
    cudaMalloc(&bools, size);  // <---------<< 
    cudaMalloc(&prefix_sum, size);  // <---------<< 
    cudaMalloc(&output, size);  // <---------<< 
    cudaMalloc(&array_length, sizeof(int));  // <---------<< 
    cudaMalloc(&semaphore, sizeof(int)*2);  // <---------<< 
    cudaMemcpy(values, array, size, cudaMemcpyHostToDevice);  // <---------<< 
    cudaMemcpy(array_length, length, sizeof(int), cudaMemcpyHostToDevice);  // <---------<< 

    
    int threads = *length / 2 + *length % 2;
    int blocks = threads / MAX_BLOCK_SIZE + (threads % MAX_BLOCK_SIZE > 0);
    int threads_per_block = (threads + blocks-1) / blocks;
    // printf("length:%d -> threads:%d -> blocks:%d -> threads_per_block:%d\n", *length, threads, blocks, threads_per_block);

    filterKernel<<<1, threads_per_block>>>(values, bools, prefix_sum, output, array_length, semaphore);  // <---------<< 

    cudaMemcpy(length, array_length, sizeof(int), cudaMemcpyDeviceToHost);  // <---------<< 
    int new_size = *length * sizeof(int);  // <---------<< 
    int *h_output = (int*)malloc(new_size);
    cudaMemcpy(h_output, output, new_size, cudaMemcpyDeviceToHost);  // <---------<< 
    cudaFree(&values);  // <---------<< 
    cudaFree(&bools);  // <---------<< 
    cudaFree(&prefix_sum);  // <---------<< 
    cudaFree(&output);  // <---------<< 
    cudaFree(&array_length);  // <---------<< 
    cudaFree(&semaphore);  // <---------<< 

    return h_output;
}

int main() {
    srand(1920);
    int length = START_LENGTH;
    int repeats = 10000;

    int *array = (int*)malloc(sizeof(int) * length);
    for(int i=0, *ptr=array; i<length; i++, ptr++) {
        *ptr = rand()%length;
    }

    int *filtered = seq_filter(array, &length);
    // print_array(filtered, length);
    printf("\x1b[1;40;34mCPU done\x1b[0m\n");

    // printf("\n-------------------- array[0] = %d --------------------\n\n", array[0]);

    int i = 0;
    bool broken = false;

    while(!broken && (i < repeats)) {
        length = START_LENGTH;
        int* gpu_filtered = gpu_filter(array, &length);
        // print_array(gpu_filtered, length);
    
        // check
        bool coherent = true;
        int iter = 0;
        while(coherent && (iter < length)) {
            if(filtered[iter] != gpu_filtered[iter]) {
                coherent = false;
            }
            iter++;
        }
        // if(coherent)
        //     printf("\x1b[1;40;32mCorrect :)\x1b[0m\n");
        // else {
        if(!coherent) {
            broken = true;
            printf("\x1b[1;40;31mIncorrect!\x1b[0m\npassed correctly %d times\n", i);
        }
        i++;
    }
    if(i == repeats) {
        printf("\x1b[1;40;33mSuccess!\x1b[0m\n");
    }
    
}