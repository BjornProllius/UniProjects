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


#include "client_logic_p2.h"

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



//function taken from split.c provided on eclass
int split(char inStr[],  char token[][MAXWORD], char fs[])
{
    int    i, count;
    char   *tokenp, inStrCopy[MAXLINE]; 

    // initialize variables
    count= 0;
    memset (inStrCopy, 0, sizeof(inStrCopy));
    for (i=0; i < MAXTOKEN; i++) memset (token[i], 0, sizeof(token[i]));

    // make a backup of inStr in inStrCopy
    strcpy (inStrCopy, inStr);

    if ( (tokenp= strtok(inStr, fs)) == NULL) return(0);
    
    strcpy(token[count],tokenp); count++;

    while ( (tokenp= strtok(NULL, fs)) != NULL)
    {
        strcpy(token[count],tokenp); count++;
    }

     strcpy (inStr, inStrCopy);		// restore inStr
     return(count);	   
}


//this function sends a packet to the server
void client_send_packet(int sockfd, Packet *packet) {

    // Send the packet to the server
    if (send(sockfd, packet, sizeof(Packet), 0) == -1) {
        perror("send");
        return;
    }

    //print what was transmitted
    if(strcmp(packet->type, "gtime") == 0){
        printf("Transmitted (src= %d) %s\n",packet->message.owner, packet->type);
    }
    else if(strcmp(packet->type, "done") == 0){
        printf("Transmitted (src= %d) %s\n",packet->message.owner, packet->type);
    }
    else if(strcmp(packet->type, "hello") == 0){
        printf("Transmitted (src= %d) (%s, idumber= %d)\n",packet->message.owner, packet->type, packet->message.owner);
    }
    else{
        printf("Transmitted (src= %d) (%s) (%s)\n", packet->message.owner, packet->type, packet->message.name); 

        //if there is content, print it
        if(packet->message.content[0] != '\0'){ 
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
}

//this function receives a packet from the server
void client_receive_packet(int sockfd, Packet *packet) {
    
    // Receive the packet from the server
    if (recv(sockfd, packet, sizeof(Packet), 0) == -1) {
        perror("recv");
        return;
    }

    //print what was received
    if(strcmp(packet->type, "Error") == 0) {
        printf("Received (src = %d) (ERROR: %s)\n\n",packet->message.owner, packet->message.content);
    } 
    else if (strcmp(packet->type, "time")==0){
        printf("Received (src = %d) (TIME:   %s)\n\n",packet->message.owner, packet->message.content);
    
    } else if (strcmp(packet->type, "OK")==0){ 
        printf("Received (src = %d) (OK)",packet->message.owner);

        //print the name and handle content
        if(packet->message.name[0] != '\0'){ 
            printf(" (%s)\n",packet->message.name);

            if(packet->message.content[0] != '\0'){
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
                printf("\n");
            }
        } else {
            printf("\n\n");
        }
    }


}

//taken from eclass and modified
int clientConnect (const char *serverName, int portNo)
{
    int                 sfd;
    struct sockaddr_in  server;
    struct hostent      *hp;                    // host entity

    // lookup the specified host
    //
    hp= gethostbyname(serverName);
    if (hp == (struct hostent *) NULL){
        printf("clientConnect: failed gethostbyname '%s'\n", serverName); 
        exit(1);
    }

    // put the host's address, and type into a socket structure
    //
    memset ((char *) &server, 0, sizeof server);
    memcpy ((char *) &server.sin_addr, hp->h_addr, hp->h_length);
    server.sin_family= AF_INET;
    server.sin_port= htons(portNo);

    // create a socket, and initiate a connection
    //
    if ( (sfd= socket(AF_INET, SOCK_STREAM, 0)) < 0){
        printf ("clientConnect: failed to create a socket \n"); 
        exit(1);
    }

    if (connect(sfd, (SA *) &server, sizeof(server)) < 0){
	    printf ("clientConnect: failed to connect \n"); 
        exit(1);
    }

    return sfd;
}

//this function handles the commands from the input file
void handle_command(int client_id, int line_id, char *command, char *args[], FILE *file, int sfd) {
    Packet packet;
    Packet response;
    Object object;

    // Initialize the packet
    packet.type[0] = '\0'; 
    packet.message.name[0] = '\0';
    packet.message.content[0] = '\0';

    // Initialize the object
    object.name[0] = '\0';
    object.content[0] = '\0';

    // Copy in the command
    snprintf(packet.type, sizeof(packet.type), "%s", command);

    // get command
    if (strcmp(command, "get") == 0) { 
        if (client_id == line_id) {
            object.name[0] = '\0'; // Clear object name
            object.content[0] = '\0'; // Clear content
            snprintf(object.name, sizeof(object.name), "%s", args[2]);
            packet.message = object;
            packet.message.owner = line_id; // Set the owner of the packet
            client_send_packet(sfd, &packet);
            client_receive_packet(sfd, &response);
        }
    } 
    //delete command
    else if (strcmp(command, "delete") == 0) { 
        if (client_id == line_id) {
            object.name[0] = '\0'; // Clear object name
            object.content[0] = '\0'; // Clear content
            snprintf(object.name, sizeof(object.name), "%s", args[2]);
            packet.message = object;
            packet.message.owner = line_id; // Set the owner of the packet
            client_send_packet(sfd, &packet);
            client_receive_packet(sfd, &response);
        }
    } 
    //put command
    else if (strcmp(command, "put") == 0) { 
        if (client_id == line_id) {
            char content[3*MAXCONTENTLINE+1]; // Max 3 lines of content
            content[0] = '\0'; // Clear content
            object.name[0] = '\0'; // Clear object name
            object.content[0] = '\0'; // Clear object content
            snprintf(object.name, sizeof(object.name), "%s", args[2]);

            // Read the next 3 lines from the file
            for (int i = 0; i < 5; i++) {
                char temp[MAXCONTENTLINE];

                if (fgets(temp, sizeof(temp), file) != NULL) {
                    if (temp[0] == '{') {
                        continue;
                    }
                    if (temp[0] == '}') {
                        break;
                    }
                    strcat(content, temp); // Append the line to content
                } else {
                    break; // Break the loop if end of file is reached or an error occurred
                }
            }

            snprintf(object.name, sizeof(object.name), "%s", args[2]);
            snprintf(object.content, sizeof(object.content), "%s", content);
            packet.message = object;     
            packet.message.owner = line_id; // Set the owner of the packet         
            client_send_packet(sfd, &packet);
            client_receive_packet(sfd, &response);
            content[0] = '\0'; // Clear content
        }
    } 
    //time command
    else if (strcmp(command, "gtime") == 0) {
        if (client_id == line_id) {
            object.name[0] = '\0'; // Clear object name
            object.content[0] = '\0'; // Clear content
            packet.message = object;
            packet.message.owner = line_id; // Set the owner of the packet
            client_send_packet(sfd, &packet);
            client_receive_packet(sfd, &response);
        }
    } 
    //delay command
    else if (strcmp(command, "delay") == 0) {
        if (client_id == line_id) {
            char *delay = args[2];
            printf("*** Entering delay period of %s msec \n", delay);          
            struct timespec ts;
            ts.tv_sec = atoi(delay) / 1000; // Get number of seconds
            ts.tv_nsec = (atoi(delay) % 1000) * 1000000; // Get number of nanoseconds
            nanosleep(&ts, NULL);
            printf("*** Exiting delay period\n\n");
        }
    } 
    //quit command
    else if (strcmp(command, "quit") == 0) {
        if (client_id == line_id) {
            snprintf(packet.type, sizeof(packet.type), "done");
            packet.message.owner = line_id; // Set the owner of the packet
            client_send_packet(sfd, &packet); // Tell the server that the client quit
            client_receive_packet(sfd, &response);
            close(sfd);
            exit(0);
        }
    }
}

//Client behavior
void client_loop(int client_id, char *inputfile, char *serverAddress, char *portNumber) {
    int line_id;
    char *command;
    int sfd;

    printf("do client (idNumber = %d, inputfile = %s, serverAddress = %s, portNumber = %s)\n\n", client_id, inputfile, serverAddress, portNumber);

    char serverName[MAXWORD];
    strcpy(serverName, serverAddress);

    // Connect to the server
    sfd = clientConnect(serverName, atoi(portNumber));
 

    // Initialize the 'hello' packet
    Packet helloPacket;
    helloPacket.type[0] = '\0'; 
    helloPacket.message.name[0] = '\0';
    helloPacket.message.content[0] = '\0';

    // Set the 'hello' packet's type and owner
    snprintf(helloPacket.type, sizeof(helloPacket.type), "hello");
    helloPacket.message.owner = client_id;

    // Send the 'hello' packet to the server
    client_send_packet(sfd, &helloPacket);

    // Receive a response from the server
    Packet response;
    client_receive_packet(sfd, &response);


    //print what was received
    if(strcmp(response.type, "OK") != 0) {
        fprintf(stderr, "Error: Server did not respond with OK\n");
        exit(1);
    } 

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

        char *args[MAXTOKEN + 1]; // Create an array to hold the arguments
        for (int i = 0; i < token_count; i++) {
            args[i] = token[i]; // Copy the tokens into the array
        }
        for (int i = token_count; i <= MAXTOKEN; i++) {
            args[i] = (char*) NULL; // Set the rest of the array to NULL
        }

        line_id = atoi(args[0]);

        if(line_id != client_id){
            continue;
        }
        command = args[1];

        if (command == NULL) {
            printf("somethings broken\n");
            exit(1);
        }
        handle_command(client_id, line_id, command, args, file, sfd);


    }
    fclose(file);
    close(sfd);
}