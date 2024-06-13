// Description: This file contains the server logic for the server in part 2 of the assignment.


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
#include <netinet/in.h>
#include <sys/socket.h>
#include <netdb.h>
#include <poll.h>
#include <errno.h>

#include "server_logic_p2.h"

#define MAXOBJECTNAME 32 //max # of characters in an object name
#define MAXCONTENTLINE 80 //max # of characters in a content line
#define MAX_OBJECTS 16 //max # of objects in the server
#define MAXLINE 256 // Max # of characters in an input line 
#define MAXTOKEN 4 // Max # of tokens in any input line    
#define MAXWORD 32 // Max # of characters in any token   
#define NCLIENT  3  // client limit
#define _REENTRANT


typedef struct sockaddr  SA;

typedef struct {
    char name[MAXOBJECTNAME+1]; 
    char content[3*MAXCONTENTLINE+1];
    int is_used;
    int owner;
} Object;

typedef struct {
    char type[MAXWORD];
    Object message;
}Packet; 



//this function receives a packet from the client
int server_receive_packet(int newsockfd, Packet *packet) {

    // Receive the packet from the client
    int ret = recv(newsockfd, packet, sizeof(Packet), 0);

    
    if (ret == -1) {
        perror("recv");
        return ret;
    }

    if(ret == 0){
        return ret;
    }

    //print what was received
    if(strcmp(packet->type, "gtime") == 0){
        printf("Received (src= %d) %s\n", packet->message.owner, packet->type);
    }
    else if(strcmp(packet->type, "done") == 0){
        printf("Received (src= %d) %s\n", packet->message.owner, packet->type);
    }
    else{
        printf("Received (src= %d) (%s) (%s)\n",packet->message.owner, packet->type, packet->message.name); 

        if(packet->message.content[0] != '\0'){ //if there is content, print it
            char *line;
            char *contentCopy = strdup(packet->message.content);
            int i = 0;
            line = strtok(contentCopy, "\n");

            while(line != NULL) {
                printf("[%d]: %s\n", i, line);
                i++;
                line = strtok(NULL, "\n");
            }
            free(contentCopy);
        }
    }
    return ret;
}

//this function sends a packet to the client
void server_send_packet(int newsockfd, Packet *packet) {

    packet->message.owner = 0; //set the owner of the transmitted packet as server
    // Send the packet to the client
    if (send(newsockfd, packet, sizeof(Packet), 0) == -1) {
        perror("send");
        return;
    }
    
    //print what was transmitted
    if(strcmp(packet->type, "Error") == 0) {
        printf("Transmitted (src = %d) (ERROR: %s)\n\n",packet->message.owner, packet->message.content);

    } else if (strcmp(packet->type, "time")==0){
        printf("Transmitted (src = %d) (TIME:   %s)\n\n",packet->message.owner,packet->message.content);
    
    } else if (strcmp(packet->type, "OK")==0){ 
        printf("Transmitted (src = %d) (OK)",packet->message.owner);

        //print the name and handle content
        if(packet->message.name[0] != '\0'){ 
            printf(" (%s)\n\n",packet->message.name);

        } else {
            printf("\n\n");
        }    
    } 
}


//taken from eclass and modified
int serverListen (int portNo, int nClient)
{
    int                 sfd;
    int                 opt = 1;
    struct sockaddr_in  sin;

    memset ((char *) &sin, 0, sizeof(sin));

    // create a managing socket
    //
    if ( (sfd= socket (AF_INET, SOCK_STREAM, 0)) < 0){
        printf ("serverListen: failed to create a socket \n");
        exit(1);
    }

    // Set socket option to reuse address
    if (setsockopt(sfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("setsockopt");
        exit(1);
    }
    // bind the managing socket to a name
    //
    sin.sin_family= AF_INET;
    sin.sin_addr.s_addr= htonl(INADDR_ANY);
    sin.sin_port= htons(portNo);

    if (bind (sfd, (SA *) &sin, sizeof(sin)) < 0){
        printf ("serverListen: bind failed \n"); 
        exit(1);
    }

    // indicate how many connection requests can be queued

    listen (sfd, nClient);
    return sfd;
}

// Handle different packet types
void handle_packet_type(Packet *received_packet, Packet *response_packet, Object *object_table, int client_fd, clock_t start_time, long clktck, int *done) {
    //put packet
    if (strcmp(received_packet->type, "put") == 0) {
        //check if object already exists
        for (int i = 0; i < MAX_OBJECTS; i++) {
            if (object_table[i].is_used && strcmp(object_table[i].name, received_packet->message.name) == 0) {
                snprintf(response_packet->type, sizeof(response_packet->type), "%s", "Error");
                snprintf(response_packet->message.content, sizeof(response_packet->message.content), "%s", "object already exists");
                server_send_packet(client_fd, response_packet);
                return;
            }
        }
        // Add the object to the object table if it does not already exist
        for (int i = 0; i < MAX_OBJECTS; i++) {   
            if (!object_table[i].is_used) {
                object_table[i] = received_packet->message;
                object_table[i].is_used = 1;
                snprintf(response_packet->type, sizeof(response_packet->type), "%s", "OK");
                server_send_packet(client_fd, response_packet);
                return;
            }
        }

    //get packet
    } else if (strcmp(received_packet->type, "get") == 0) {
        int object_found = 0;

        //return the packet if it exists
        for (int i = 0; i < MAX_OBJECTS; i++) {
            if (object_table[i].is_used && strcmp(object_table[i].name, received_packet->message.name) == 0) {
                response_packet->message = object_table[i];
                snprintf(response_packet->type, sizeof(response_packet->type), "%s", "OK");
                server_send_packet(client_fd, response_packet);
                object_found = 1;
                break;
            }
        }
        //return error if object does not exist
        if (!object_found) {
            snprintf(response_packet->type, sizeof(response_packet->type), "%s", "Error");
            snprintf(response_packet->message.content, sizeof(response_packet->message.content), "%s", "object not found");
            server_send_packet(client_fd, response_packet);
        }
    }

    //delete packet
    else if (strcmp(received_packet->type, "delete") == 0) {
        int object_found = 0;

        //delete the object if it exists and the owner is correct
        for (int i = 0; i < MAX_OBJECTS; i++) {
            if (object_table[i].is_used && strcmp(object_table[i].name, received_packet->message.name) == 0) {

                //if the owner is not the right client, return error
                if(object_table[i].owner != received_packet->message.owner){
                    snprintf(response_packet->type, sizeof(response_packet->type), "%s", "Error");
                    snprintf(response_packet->message.content, sizeof(response_packet->message.content), "%s", "Client not owner");
                    server_send_packet(client_fd, response_packet);
                    object_found = 1;
                    break;
                }
                else{ //the owner is correct, delete the object
                    object_table[i].is_used = 0;
                    snprintf(response_packet->type, sizeof(response_packet->type), "%s", "OK");
                    server_send_packet(client_fd, response_packet);
                    object_found = 1;
                    break;
                }
            }
        }
        //return error if object does not exist
        if (!object_found) {
            snprintf(response_packet->type, sizeof(response_packet->type), "%s", "Error");
            snprintf(response_packet->message.content, sizeof(response_packet->message.content), "%s", "object does not exist");
            server_send_packet(client_fd, response_packet);
        }
    }
    //time packet
    else if (strcmp(received_packet->type, "gtime") == 0) {
        clock_t current_time = clock();
        double elapsed_time = ((double) (current_time - start_time)) / clktck;
        snprintf(response_packet->type, sizeof(response_packet->type), "%s", "time");
        snprintf(response_packet->message.content, sizeof(response_packet->message.content), "%.2f", elapsed_time);           
        server_send_packet(client_fd, response_packet);
    }
    //hello packet
    else if(strcmp(received_packet->type, "hello") == 0){
        snprintf(response_packet->type, sizeof(response_packet->type), "%s", "OK");
        server_send_packet(client_fd, response_packet);
    }
    //done packet
    else if (strcmp(received_packet->type, "done") == 0) {
        *done = 1;
        snprintf(response_packet->type, sizeof(response_packet->type), "%s", "OK");
        server_send_packet(client_fd, response_packet);
        close(client_fd);
    }
}

//this function polls user input (taken from a2)
char* poll_user_command() {

    fd_set read_fds;
    struct timeval tv;
    int retval;

    
    FD_ZERO(&read_fds);
    FD_SET(0, &read_fds);

    //dont wait for input
    tv.tv_sec = 0;
    tv.tv_usec = 0;

    retval = select(1, &read_fds, NULL, NULL, &tv);

    static char user_command[100];
    user_command[0] = '\0';  

    if (retval == -1) {
        perror("select()");
    } else if (retval) {
        fgets(user_command, 100, stdin);
    }

    //remove trailing newline if it exists
    int len = strlen(user_command);
    if(len>0 && user_command[len-1]=='\n'){
        user_command[len-1]='\0';
    }

    return user_command;
}

//this function print the object table (taken from a2)
void print_object_table(Object object_table[]) {
    printf("Stored objects:\n");
    for (int i = 0; i < MAX_OBJECTS; i++) {
        if (object_table[i].is_used) {
            printf("(Owner= %d, name= %s)\n", object_table[i].owner,object_table[i].name);
            if(object_table[i].content[0] != '\0'){ //if there is content, print it
                char *line;
                char *contentCopy = strdup(object_table[i].content);
                int j = 0;
                line = strtok(contentCopy, "\n");

                while(line != NULL) {
                    printf("[%d]: %s\n", j, line);
                    j++;
                    line = strtok(NULL, "\n");
                }
                free(contentCopy);
            }
        }
    }
}

// Server behavior
void server_loop(char *portNumber) {
    clock_t start_time;
    long clktck = sysconf(_SC_CLK_TCK);
    start_time = clock();
    Packet received_packet;
    Object object_table[MAX_OBJECTS] = {0}; // Initialize all objects as unused

    int rval, timeout, i;
    int newsock[NCLIENT+1];
    int done[NCLIENT+1];

    int N; //active clients

    struct pollfd pfd[NCLIENT+1];
    struct sockaddr_in from;
    socklen_t fromlen;

    for (i = 0; i <= NCLIENT; i++) done[i] = -1;

    // Prepare for non-blocking I/O polling from the managing socket
    timeout = 0;
    pfd[0].fd = serverListen(atoi(portNumber), NCLIENT);
    pfd[0].events = POLLIN;
    pfd[0].revents = 0;

    printf("do server (portNumber = %s)\n\n", portNumber);
    printf("server is accepting connections (portNumber = %s)\n\n", portNumber);
    N = 1;
    while (1) {
        rval = poll(pfd, N, timeout);
        if (rval < 0) {
            perror("poll");
            exit(1);
        }

        if ((N < NCLIENT+1) && (pfd[0].revents & POLLIN)) {
            // Accept a new connection
            fromlen = sizeof(from);
            newsock[N] = accept(pfd[0].fd, (SA *) &from, &fromlen);

            pfd[N].fd = newsock[N];
            pfd[N].events = POLLIN;
            pfd[N].revents = 0;
            done[N] = 0;
            N++;
        }

        //poll user input
        char* user_command= poll_user_command();
        if(user_command[0]!='\0'){
            //print the stored objects if command is quit
            if(strcmp(user_command,"list")==0){
                print_object_table(object_table);
            }
            //quit if command is quit
            else if(strcmp(user_command,"quit")==0){
                for (int i=1; i<N; i++){
                    close(pfd[i].fd);
                }
                return;
            }
            //print error message if command is invalid
            else{
                printf("Invalid command\n");
            }
        }

        // Check for input from the clients
        for (int i = 1; i < N; i++) {
            if (pfd[i].revents & POLLIN) {
                // Receive a packet from the client
                int ret = server_receive_packet(pfd[i].fd, &received_packet);

                // Check if the client has closed the connection
                if (ret == 0) {
                    printf("Lost connection with client\n");
                    close(pfd[i].fd);
                    for (int j = i; j < N - 1; j++) {
                        pfd[j] = pfd[j + 1];
                        newsock[j] = newsock[j + 1];
                        done[j] = done[j + 1];
                    }
                    N--;

                    if (N==1){ //close the server if there are no active clients
                        printf("Lost connection with last client, terminating server\n");
                        close(pfd[0].fd);
                        exit(0);
                    }
                    continue;
                }    

                // Create a response packet
                Packet response_packet;
                response_packet.message.name[0] = '\0';
                response_packet.message.content[0] = '\0';

                // Handle different packet types
                handle_packet_type(&received_packet, &response_packet, object_table, pfd[i].fd, start_time, clktck, &done[i]);
                
                if(done[i]){
                    for (int j = i; j < N - 1; j++) {
                        pfd[j] = pfd[j + 1];
                        newsock[j] = newsock[j + 1];
                        done[j] = done[j + 1];
                    }    
                    N--;
                }
            }
            if (N==1){ //close the server if there are no active clients
            
                close(pfd[0].fd);
                exit(0);
            }
        }
    }
}