/*
# ------------------------------
# a4p1.c -- a program that can simulates a shared resource system with multiple tasks
#
#
#  Arguments:
#       a3p1 inputfile monitorTime NITER
#       -inputfile is the file that contains the resources and tasks
#       -monitorTime is the time in milliseconds that the monitor thread will wait between printing the state of the tasks
#       -NITER is the number of iterations that each task will run
#
#  Method:
#       -The program reads the input file and creates a monitor thread that will print the state of the tasks every monitorTime milliseconds
#       -The program then creates a thread for each task in the input file
#       -Each task will run niter times and will acquire the resources it needs to run before running
#       -The program will print the state of the tasks and the resources at the end of the simulation
#
#  Author: Bjorn Prollius Lec B1/EB1
# ------------------------------
*/



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/time.h> 
#include <fcntl.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <time.h>
#include <poll.h>
#include <errno.h>
#include <pthread.h>

#define MAXLINE 256 // Max # of characters in an input line 
#define MAXTOKEN 32 // Max # of tokens in any input line    
#define MAXWORD 32 // Max # of characters in any token  
#define NRES_TYPES 10 //Max # of resource types
#define NTASKS 25 //Max # of tasks

// Define the structure for a resource. Each has a type and a value
typedef struct {
    char type[MAXWORD];
    int value;
    int amountHeld;
    int maxAvail;
} Resource;

// Define the structure for a task
typedef struct {
    char taskName[MAXWORD];
    int taskID;
    int busyTime;
    int idleTime;
    Resource resourcesNeeded[NRES_TYPES]; // Array of resources needed to do the task
    int niter; // Number of iterations
    int numRuns;
    int waitTime;
} TaskData;

//global variables to hold the shared resources
Resource sharedResources[NRES_TYPES];

//mutex to protect the shared resources
pthread_mutex_t resourceMutex = PTHREAD_MUTEX_INITIALIZER;

// Global variable to hold the start and end time
struct timeval startTime, endTime;

// Global variables to hold the task states
char* taskStates[NTASKS];

//mutex to protect the task states
pthread_mutex_t stateMutex = PTHREAD_MUTEX_INITIALIZER;

// global variable to hold the thread identifiers
pthread_t threads[NTASKS];
int threadCount = 0;

//mutex to protect the thread creation
pthread_mutex_t threadCreationMutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t threadCreationCond = PTHREAD_COND_INITIALIZER;
int threadReady = 0;

//global variable to hold the number of tasks done
int allTasksDone = 0;

//array to hold the task data
TaskData* tasks[NTASKS] = {NULL};


// Function to split a string into tokens
int split(char *inStr, char token[MAXTOKEN][MAXWORD], char *fs) {
    int i = 0;
    char *tok = strtok(inStr, fs);

    while (tok != NULL) {
        strncpy(token[i], tok, MAXWORD);
        i++;
        tok = strtok(NULL, fs);
    }

    return i;
}

// Function to acquire resources
int acquire_resources(TaskData* data) {
    // Lock the resource mutex
    pthread_mutex_lock(&resourceMutex);

    // Try to acquire all resources
    for (int i = 0; i < NRES_TYPES; i++) {
        Resource needed = data->resourcesNeeded[i];
        for (int j = 0; j < NRES_TYPES; j++) {
            if (strcmp(needed.type, sharedResources[j].type) == 0) {

                // Not enough resources available, release all and return 0
                if (sharedResources[j].value < needed.value) {
                    for (int k = 0; k < i; k++) {
                        Resource toRelease = data->resourcesNeeded[k];
                        for (int l = 0; l < NRES_TYPES; l++) {
                            if (strcmp(toRelease.type, sharedResources[l].type) == 0) {
                                sharedResources[l].value += toRelease.value;
                                break;
                            }
                        }
                    }
                    pthread_mutex_unlock(&resourceMutex);
                    return 0;

                // Enough resources available, acquire them
                } else {
                    sharedResources[j].value -= needed.value;
                    data->resourcesNeeded[i].amountHeld = needed.value;
                    break;
                }
            }
        }
    }

    // All resources acquired, return 1 and unlock the resource mutex
    pthread_mutex_unlock(&resourceMutex);
    return 1; 
}




// Function to release resources
void release_resources(TaskData* data) {
    // Lock the resource mutex
    pthread_mutex_lock(&resourceMutex);

    // Release all resources
    for (int i = 0; i < NRES_TYPES; i++) {
        Resource toRelease = data->resourcesNeeded[i];
        for (int j = 0; j < NRES_TYPES; j++) {
            if (strcmp(toRelease.type, sharedResources[j].type) == 0) {
                sharedResources[j].value += toRelease.value;
                data->resourcesNeeded[i].amountHeld = 0;
                break;
            }
        }
    }
    // Unlock the resource mutex
    pthread_mutex_unlock(&resourceMutex);
}

// function to run the tasks
void* run_task(void* arg) {

    // Signal that this thread is ready
    pthread_mutex_lock(&threadCreationMutex);
    threadReady = 1;
    pthread_cond_signal(&threadCreationCond);
    pthread_mutex_unlock(&threadCreationMutex);

    
    TaskData* data = (TaskData*) arg;
    pthread_t tid = pthread_self();

    //do the task niter times
    for (int i = 0; i < data->niter; i++) {

        // Set state to WAIT
        pthread_mutex_lock(&stateMutex);
        taskStates[data->taskID] = "WAIT";
        pthread_mutex_unlock(&stateMutex);

        // Acquire resources (busy wait)
        while (!acquire_resources(data)) {
            // Resources not available, wait and try again
            data->waitTime += 10; //add 10 milliseconds to the wait time
            usleep(10000); // Wait for 10 milliseconds
        }

        
        // Set state to RUN
        pthread_mutex_lock(&stateMutex);
        taskStates[data->taskID] = "RUN";
        pthread_mutex_unlock(&stateMutex);

        // Simulate busy time then release resources
        usleep(data->busyTime * 1000); // usleep takes microseconds
        release_resources(data);


        // Set state to IDLE
        pthread_mutex_lock(&stateMutex);
        taskStates[data->taskID] = "IDLE";
        pthread_mutex_unlock(&stateMutex);

        // Simulate idle time
        usleep(data->idleTime * 1000); // usleep takes microseconds


        // Calculate the time taken relative to the start time
        struct timeval currentTime;
        gettimeofday(&currentTime, NULL);
        long time = (currentTime.tv_sec - startTime.tv_sec) * 1000 + (currentTime.tv_usec - startTime.tv_usec) / 1000;
        data->numRuns++;
        
        // Print task info
        printf("task: %s (tid= 0x%lx, iter= %d, time= %ld msec)\n", data->taskName, (unsigned long)tid, i+1, time); 
    }
    return NULL;
}

//the monitor thread
void* monitor(void* arg) {
    int monitorTime = *(int*) arg;

    //monitor the tasks until all tasks are done
    while (!allTasksDone) {

        // Lock the state mutex
        pthread_mutex_lock(&stateMutex);

        // Print the state of each task
        printf("\nmonitor:[WAIT] ");
        for (int i = 0; i < NTASKS; i++) {
            if ((strcmp(taskStates[i], "WAIT") == 0) && (tasks[i] != NULL)){
                printf("%s ", tasks[i]->taskName);
            }
        }
        printf("\n");

        printf("\t[RUN] ");
        for (int i = 0; i < NTASKS; i++) {
            if ((strcmp(taskStates[i], "RUN") == 0) && (tasks[i] != NULL)){
                printf("%s ", tasks[i]->taskName);
            }
        }
        printf("\n");

        printf("\t[IDLE] ");
        for (int i = 0; i < NTASKS; i++) {
            if ((strcmp(taskStates[i], "IDLE") == 0) && (tasks[i] != NULL)){
                printf("%s ", tasks[i]->taskName);
            }
        }
        printf("\n \n");

        // Unlock the state mutex
        pthread_mutex_unlock(&stateMutex);

        usleep(monitorTime * 1000); // Wait for monitorTime milliseconds
    }

    return NULL;
}



//taken and modified from a3
// Function to read the input file and handle the assigment of tasks
void taksmaster(char *inputfile, int niter) {

    //open the input file
    FILE *file = fopen(inputfile, "r");
    if (file == NULL) {
        fprintf(stderr, "Error opening file\n");
        exit(1);
    }
    char line[MAXLINE];

    // Read the file line by line and handle the commands
    while (fgets(line, sizeof(line), file) != NULL) {

        if (line[0] == '\n' || line[0] == '#') continue; // Skip empty and comment lines

        // Section of code taken from eclass
        size_t len = strlen(line);
        if (len > 0 && line[len-1] == '\n') {
            line[len-1] = '\0'; // Remove newline character from line
        }

        // Tokenize the line
        char token[MAXTOKEN][MAXWORD];
        char fs[] = "\t \n";
        int token_count = split(line, token, fs);



        // If the line is a resource line, collect the available resources
        if (strcmp(token[0], "resources") == 0) {
            // Initialize resource counter
            int resourceCount = 0;

            for (int i = 1; i < token_count; i++) {
                char *type = strtok(token[i], ":");
                char *value_str = strtok(NULL, ":");
                int value = atoi(value_str);

                // Create new Resource and store in sharedResources
                Resource newResource;
                strncpy(newResource.type, type, MAXWORD);
                newResource.value = value;
                newResource.maxAvail = value;
                memcpy(&sharedResources[resourceCount++], &newResource, sizeof(Resource));
            }
        }

        // If the line is a task line, assign the task to a thread
        if (strcmp(token[0], "task") == 0) {
            TaskData* data = malloc(sizeof(TaskData));

            // Initialize all bytes in the TaskData structure to zero
            memset(data, 0, sizeof(TaskData));

            // Store the task data
            strncpy(data->taskName, token[1], MAXWORD);
            data->busyTime = atoi(token[2]);
            data->idleTime = atoi(token[3]);
            data->niter = niter;
            data->taskID = threadCount;
            data->numRuns = 0;
            data->waitTime = 0;

            // Initialize resource counter
            int resourceCount = 0;

            // Get resource type and number of units needed
            for (int i = 4; i < token_count; i++) {
                
                char *type = strtok(token[i], ":");
                char *value_str = strtok(NULL, ":");
                int value = atoi(value_str);

                // Create new Resource and store in resourcesNeeded
                Resource newResource;
                strncpy(newResource.type, type, MAXWORD);
                newResource.value = value;
                newResource.amountHeld = 0;
                memcpy(&data->resourcesNeeded[resourceCount++], &newResource, sizeof(Resource)); 
            }

            // Store the task data in the tasks array
            tasks[threadCount] = data;

            // Create a new thread for the task, ensuring that its ready to run before continuing            
            pthread_mutex_lock(&threadCreationMutex);
            threadReady = 0;
            pthread_create(&threads[threadCount++], NULL, run_task, data);
            while (!threadReady) {
                pthread_cond_wait(&threadCreationCond, &threadCreationMutex);
            }
            pthread_mutex_unlock(&threadCreationMutex);
        }    
    }
    fclose(file);
    
}





int main(int argc, char *argv[]){


    if (argc != 4){ 
        fprintf(stderr, "Usage: %s inputFile monitorTime NITER\n", argv[0]);
        exit(1);
    }

    int niter = atoi(argv[3]);
    int monitorTime = atoi(argv[2]);

    // Record the start time
    gettimeofday(&startTime, NULL);

    //initialize all states to wait
    for (int i = 0; i < NTASKS; i++) {
        taskStates[i] = "WAIT";
    }

    //initialize all tasks to NULL
    for( int i = 0; i < NTASKS; i++){
        tasks[i] = NULL;
    }

    // Create a monitor thread
    pthread_t monitorThread;
    pthread_create(&monitorThread, NULL, monitor, &monitorTime);
  
    // Read from the input file and create the tasks
    taksmaster(argv[1], niter);

    // Wait for all threads to finish and set the done flag to 1.
    for (int i = 0; i < threadCount; i++) {
        pthread_join(threads[i], NULL);
    }
    allTasksDone = 1;

    pthread_join(monitorThread, NULL);

    // Print the shared resources
    printf("\nSystem Resources:\n");
    for (int i = 0; i < NRES_TYPES; i++) {
        if(sharedResources[i].maxAvail != 0)
        printf("%s: (maxAvail= %d, held= %d)\n", sharedResources[i].type, sharedResources[i].maxAvail, sharedResources[i].maxAvail-sharedResources[i].value);
    }

    // print and free all tasks
    printf("\nSystem Tasks:");
    for (int i = 0; i < NTASKS; i++) {
        if (tasks[i] != NULL) {
            //basic task info
            printf("\n[%d] %s: (%s, runtime= %d msec, idletime= %d msec):\n",i, tasks[i]->taskName, taskStates[i], tasks[i]->busyTime, tasks[i]->idleTime);
            printf("\t(tid= 0x%lx)\n", (unsigned long)threads[i]);

            //resources needed info
            for (int j = 0; j < NRES_TYPES; j++) {
                if(tasks[i]->resourcesNeeded[j].value != 0){
                    printf("\t%s: (needed= %d, held= %d)\n", tasks[i]->resourcesNeeded[j].type, tasks[i]->resourcesNeeded[j].value, tasks[i]->resourcesNeeded[j].amountHeld);
                }
             }
            //time and runs info
            printf("\t(RUN: %d times, WAIT: %d msec)\n", tasks[i]->numRuns, tasks[i]->waitTime);
    
        }
        free(tasks[i]);
    }

    // Record the end time and calculate the running time
    gettimeofday(&endTime, NULL);
    long time = (endTime.tv_sec - startTime.tv_sec) * 1000 + (endTime.tv_usec - startTime.tv_usec) / 1000;
    printf("\nRunning time: %ld msec\n", time);

    return 0;
}
