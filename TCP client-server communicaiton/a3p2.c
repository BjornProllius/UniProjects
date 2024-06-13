/*
# ------------------------------
# a3p2.c -- a program that can be invoked either as a server or a client and communicates using TCP
#
#
#  Arguments for server:
#       a3p2 -s portnumber
#       -portnumber is the port number that the server will listen on
#
#  Arguments for client:
#       a3p2 -c  idnumber inputfile serveraddress portnumber
#       -inputfile is the file that contains the commands for the client
#       -serveraddress is the address of the server
#       -portnumber is the port number that the server is listening on
#
#  Method:
#       Apon starting, this program checks if it is being invoked as a server or a client.
#       If it is being invoked as a server, it enters a loop where it waits for and responds to packets from the clients.
#       If it is being invoked as a client, it reads the input file and sends/recives packets to/from the server.
#       Both client an server print the packets they transmit and recieve
#
#  Author: Bjorn Prollius Lec B1/EB1
# ------------------------------
*/



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "client_logic_p2.h"
#include "server_logic_p2.h"

int main(int argc, char *argv[]){
    char *type; 

    if (argc != 3 && argc != 6){ 
        fprintf(stderr, "Usage: %s (-s portNumber) or (-c 1 inputFile serverAddress portNumber)\n", argv[0]);
        exit(1);
    }
    type = argv[1];
    if (strcmp(type, "-c") != 0 && strcmp(type, "-s") != 0){
        fprintf(stderr, "invalid type\n");
        exit(1);
    }

    if (strcmp(type, "-c") == 0){
        if(argc != 6){
            fprintf(stderr, "Usage: %s -c 1 inputFile serverAddress portNumber\n", argv[0]);
            exit(1);
        }
       
        client_loop(atoi(argv[2]), argv[3], argv[4], argv[5]);
    }
    else{
        if(argc != 3){
            fprintf(stderr, "Usage: %s -s portNumber\n", argv[0]);
            exit(1);
        }
       
        server_loop(argv[2]);
    }
    return 0;
}

//./a3p2 -s 2222
//./a3p2 -c 1 a3p2-w24-ex1 localhost 2222
//./a3p2 -c 2 a3p2-w24-ex1 localhost 2222
