//Bjorn Prollius
//1646013
//Lec B1/EB1

#include <cnet.h>
#include <cnetsupport.h>
#include <string.h>
#include <time.h>
#include <stdbool.h>

#define	DEFAULT_WALKING_SPEED	5.0
#define	DEFAULT_PAUSE_TIME	20

#define NMOBILE 15   //max number of mobile nodes
#define NANCHORS 15  //max number of anchor nodes


#define BEACON_INTERVAL 5000000 // Beacon interval in microseconds


#define	TX_NEXT			(5000000 + rand()%5000000)

#define NFRAME 20
int frameCount = 0; // Counter to keep track of stored frames
int frameStart = 0; // Start of the buffer
int frameEnd = -1; // End of the buffer

typedef struct {
    int			dest;
    int			src;
    int			length;		// length of payload
} WLAN_HEADER;


typedef struct {
    WLAN_HEADER		header;
    char		payload[2304];
    int                 seenBy[NMOBILE];  // Array of node addresses that have seen this frame
    int                 seenByCount;  // Number of nodes that have seen this frame
} WLAN_FRAME;

WLAN_FRAME frameBuffer[NFRAME]; // Buffer to store frames


static	bool		verbose		= true;

static	int		*stats		= NULL;

CnetTime lastHeardFrom[NANCHORS]; // lastHeardFrom[i] is the last time anchor node i was heard from


int mobileNodes[NMOBILE]; // Array of mobile node addresses  
int numMobileNodes; // Number of mobile nodes
int anchors[NANCHORS]; // Array of anchor node addresses     
int numAnchors; // Number of anchor nodes
bool receivedFromAnchor[NMOBILE][NANCHORS]; // receivedFromAnchor[i][j] is true if node i has received a transmission from anchor node j


//checks if the node is a mobile node
bool isMobileNode(int nodeAddress) {
    for(int i = 0; i < numMobileNodes; i++) {
        if(mobileNodes[i] == nodeAddress) {
            return true;
        }
    }
    return false;
}

//checks if the node is an anchor node
bool isAnchorNode(int nodeAddress) {
    for(int i = 0; i < numAnchors; i++) {
        if(anchors[i] == nodeAddress) {
            return true;
        }
    }
    return false;
}


//checks if the node can reach an anchor node
bool canReachAnchorNode(int currentNodeAddress) {
    int currentNodeIndex = -1;
    for(int i = 0; i < numMobileNodes; i++) {
        if(mobileNodes[i] == currentNodeAddress) {
            currentNodeIndex = i;
            break;
        }
    }
    if(currentNodeIndex == -1) {
        return false;
    }
    // Check if the current node has received a transmission from any anchor node in the last 2 beacon intervals
    for(int i = 0; i < numAnchors; i++) {
        if(receivedFromAnchor[currentNodeIndex][i] && nodeinfo.time_in_usec - lastHeardFrom[i] <= 2 * BEACON_INTERVAL) {
            return true;
        }
    }
    return false;
}

//stores the frame in the circular buffer if its not already stored
bool storeFrameForLater(WLAN_FRAME frame) {
    // Iterate over the stored frames
    for(int i = 0; i < frameCount; i++) {
        int index = (frameStart + i) % NFRAME; // Calculate the index of the i-th stored frame
        // If a frame with the same source and destination is already stored, return
        if(frameBuffer[index].header.src == frame.header.src && frameBuffer[index].header.dest == frame.header.dest) {
            return 1; //return 1 if already stored
        }
    }

    // If no frame with the same source and destination is stored, store the new frame
    frameEnd = (frameEnd + 1) % NFRAME; // Move frameEnd to the next position in the buffer
    frameBuffer[frameEnd] = frame; // Store the new frame at frameEnd
    if(frameCount - frameStart >= NFRAME) {
        frameStart = (frameStart + 1) % NFRAME; // Overwrite the oldest frame if full
    }
    if (frameCount < NFRAME){
        frameCount++;
    }
    if(frameStart > frameCount) {
        frameStart = frameCount;
    }
    return 0; //return 0 if stored properly
}

//removes the frame from the circular buffer
void removeFrame(int index) {
    // Move the last frame to the removed frame's position
    frameBuffer[index % NFRAME] = frameBuffer[frameEnd % NFRAME];

    // Decrease the frame count and adjust frameEnd
    frameCount--;
    frameEnd = (frameEnd - 1 + NFRAME) % NFRAME; // Ensure frameEnd wraps around to the start of the buffer if necessary

    // Adjust frameStart if necessary
    if(frameStart > frameCount) {
        frameStart = frameCount;
    }
}



static EVENT_HANDLER(transmit)
{
    // If the current node is an anchor node, do not generate a packet
    if(isAnchorNode(nodeinfo.address)) {
        return;
    }

    WLAN_FRAME	frame;
    int		link	= 1;
 
    // Generate a random mobile node desitnation
    do {
        int destIndex = rand() % numMobileNodes;
        frame.header.dest = mobileNodes[destIndex];
    } while(frame.header.dest == nodeinfo.address);

    frame.header.src = nodeinfo.address;

   
    frame.header.length	= strlen(frame.payload) + 1; // send NUL too

    size_t len	= sizeof(WLAN_HEADER) + frame.header.length;
    CHECK(CNET_write_physical_reliable(link, &frame, &len));
    ++stats[0];

    CnetPosition pos;
    CHECK(CNET_get_position(&pos, NULL));
    if(verbose) {
        fprintf(stdout, "mobile [%d](x=%f,y=%f): pkt transmitted (src=%d, dest=%d)\n",
            nodeinfo.address, pos.x, pos.y, frame.header.src, frame.header.dest);
    }

    CNET_start_timer(EV_TIMER1, TX_NEXT, 0);
}

static EVENT_HANDLER(receive)
{
    WLAN_FRAME	frame;
    size_t	len;
    int		link;

    len	= sizeof(frame);
    CHECK(CNET_read_physical(&link, &frame, &len));


    // if the source of the frame is an anchor node and the current node is a mobile node, update the last heard time
    if(isAnchorNode(frame.header.src) && isMobileNode(nodeinfo.address)) {
        int srcIndex = -1;
        for(int i = 0; i < numAnchors; i++) {
            if(anchors[i] == frame.header.src) {
                srcIndex = i;
                break;
            }
        }
        if(srcIndex != -1) {
            // Update the last heard time for this anchor node
            lastHeardFrom[srcIndex] = nodeinfo.time_in_usec;

            int destIndex = -1;
            for(int i = 0; i < numMobileNodes; i++) {
                if(mobileNodes[i] == nodeinfo.address) {
                    destIndex = i;
                    break;
                }
            }
            // If the current node is a mobile node and the frame is from an anchor node, update the received from list
            if(destIndex != -1) {
                receivedFromAnchor[destIndex][srcIndex] = true;
            }
        }
    }

    // Check if the current node is an anchor node and the received frame is a request for packets
    if(isAnchorNode(nodeinfo.address) && strstr(frame.payload, "Request from") != NULL) {
        int requestingNodeAddress = atoi(frame.payload + 13); // Extract the node address from the payload and convert to int
        if(verbose) {
            fprintf(stdout, "anchor [%d]: download request (dest=%d)\n",
                nodeinfo.address, requestingNodeAddress);
        }
        // Iterate over the stored frames
        int i = 0;
        bool frameFound = false;
        while(i < frameCount) {
            // If the frame is meant for the requesting node and it's still in the buffer, send it
            if(frameBuffer[i].header.dest == requestingNodeAddress) {
                size_t len = sizeof(WLAN_HEADER) + frameBuffer[i].header.length;
                CHECK(CNET_write_physical_reliable(link, &frameBuffer[i], &len));

                if(verbose) {
                    fprintf(stdout, "anchor [%d]: download reply (src=%d, dest=%d)\n",
                            nodeinfo.address, frameBuffer[i].header.src, frameBuffer[i].header.dest);
                }

                removeFrame(i);
                // Do not increment i here, as the next frame has shifted to the current index
                frameFound = true;
            } else {
                // Only increment i if the current frame was not sent
                i++;
            }
        }
        // If the frame was not found, print a message
        if(!frameFound) {
            fprintf(stdout, "anchor [%d]: Frame for node %d not found, it may have been overwritten.\n",nodeinfo.address, requestingNodeAddress);
        }
    }

    // Check if the current node is a mobile node, the received frame is a beacon, and request packets if the current node is a destination in the beacon
    if(isMobileNode(nodeinfo.address) && frame.header.dest == -1 && strstr(frame.payload, "Beacon from") != NULL) {
        char srcStr[20]; // Buffer to hold the string representation of frame.header.src
        snprintf(srcStr, sizeof(srcStr), "%d", frame.header.src); // Convert the integer to a string

        char* destAddresses = frame.payload + strlen("Beacon from ") + strlen(srcStr) + strlen(", Destinations: ");
        char* token = strtok(destAddresses, ",");
        while(token != NULL) {
            // If the current node is a destination in the beacon
            if(nodeinfo.address == atoi(token)) {
                // Create a new frame to request packets
                WLAN_FRAME requestFrame;
                requestFrame.header.dest = frame.header.src; // Send the request to the anchor node
                requestFrame.header.src = nodeinfo.address;
                sprintf(requestFrame.payload, "Request from %d", nodeinfo.address);
                requestFrame.header.length = strlen(requestFrame.payload) + 1; // send NUL too

                // Send the request
                size_t len = sizeof(WLAN_HEADER) + requestFrame.header.length;
                CHECK(CNET_write_physical_reliable(link, &requestFrame, &len));
                if(verbose) {
                    fprintf(stdout, "mobile [%d]: requesting packets from %d\n",
                            nodeinfo.address, requestFrame.header.dest);
                }
                break;
            }
            token = strtok(NULL, ",");
        }
    }

    // Check if the destination of the frame is the current node, if it is, increment the received counter
    if(frame.header.dest == nodeinfo.address) {
        ++stats[1];
        if(verbose && isMobileNode(nodeinfo.address)) {
            CnetPosition pos;
            CHECK(CNET_get_position(&pos, NULL));
            fprintf(stdout, "mobile [%d](x=%f,y=%f): pkt received (src=%d, dest=%d)\n",
                nodeinfo.address, pos.x, pos.y, frame.header.src, frame.header.dest);
        }
    }


    // Check if the current node is a mobile node and the destination of the frame is another mobile node, if so relay it
    else if(isMobileNode(nodeinfo.address) && frame.header.src != nodeinfo.address && isMobileNode(frame.header.dest) && canReachAnchorNode(nodeinfo.address)) {
        // Check if this node has already seen this frame
        for(int i = 0; i < frame.seenByCount; i++) {
            if(frame.seenBy[i] == nodeinfo.address) {
                return;  // This node has already seen this frame, so don't relay it again
            }
        }
        // This node hasn't seen this frame yet, so add its address to the seenBy field
        frame.seenBy[frame.seenByCount] = nodeinfo.address;
        frame.seenByCount++;

        len = sizeof(WLAN_HEADER) + frame.header.length;
        CHECK(CNET_write_physical_reliable(link, &frame, &len));
        if(verbose) {
            CnetPosition pos;
            CHECK(CNET_get_position(&pos, NULL));
            fprintf(stdout, "mobile [%d](x=%f,y=%f): pkt relayed (src=%d, dest=%d)\n",
                nodeinfo.address, pos.x, pos.y, frame.header.src, frame.header.dest);
        }
    }

    // If the current node is an anchor node,the destination is a mobile node, and the signal is not a beacon, store the frame
    if(isAnchorNode(nodeinfo.address) && isMobileNode(frame.header.dest) && frame.header.dest != -1) {
        int alreadyStored = 0;
        alreadyStored= storeFrameForLater(frame);
        if(verbose && !alreadyStored) { //only print if actually stored.
            fprintf(stdout, "anchor [%d]: pkt received and stored (src=%d, dest=%d)\n",
                    nodeinfo.address, frame.header.src, frame.header.dest);
        }
    }

}

static EVENT_HANDLER(report)
{
    fprintf(stdout, "messages generated:\t%d\n", stats[0]);
    fprintf(stdout, "messages received:\t%d\n", stats[1]);
    if(stats[0] > 0)
    fprintf(stdout, "delivery ratio:\t\t%.1f%%\n",
        (stats[0] > 0) ? (100.0*stats[1]/stats[0]) : 0);
}


//transmit beacon
static EVENT_HANDLER(transmit_beacon)
{
    WLAN_FRAME frame;
    int link = 1;

    // Set the destination to -1 to indicate a broadcast
    frame.header.dest = -1;
    frame.header.src = nodeinfo.address;

    char destAddresses[2304] = "";
    for(int i = 0; i < numMobileNodes; i++) {
        if(!isAnchorNode(mobileNodes[i])) { //only send to mobile nodes
            char dest[10];
            sprintf(dest, "%d,", mobileNodes[i]);
            strcat(destAddresses, dest);
        }
    }

    //broadcast node position and packet buffer
    size_t maxPayloadLength = sizeof(frame.payload) - 1;  // Determine the maximum length for frame.payload
    snprintf(frame.payload, maxPayloadLength, "Beacon from %d, Destinations: %s", nodeinfo.address, destAddresses);  
    frame.header.length = strlen(frame.payload) + 1; // send NUL too

    size_t len = sizeof(WLAN_HEADER) + frame.header.length;
    CHECK(CNET_write_physical_reliable(link, &frame, &len));

    if(verbose) {
        fprintf(stdout, "\n%s: transmitting beacon '%s'\n",
                nodeinfo.nodename, frame.payload);
    }

    // Schedule the next beacon transmission
    CNET_start_timer(EV_TIMER2, BEACON_INTERVAL, 0);
}

EVENT_HANDLER(reboot_node)
{
    extern void init_mobility(double walkspeed_m_per_sec, int pausetime_secs);

    char	*env;
    double	value	= 0.0;

    if(NNODES == 0) {
    fprintf(stderr, "simulation must be invoked with the -N switch\n");
    exit(EXIT_FAILURE);
    }

    // Initialize the number of anchors and mobile nodes
    numAnchors = 0;
    numMobileNodes = 0;
    
    // Get the anchor and mobile node addresses from the topology file
    char* anchorsData;
    if((anchorsData = CNET_getvar("anchors")) != NULL) {
        char* anchorsStr = anchorsData;
        char* token = strtok(anchorsStr, ", ");
        int i = 0;
        while(token != NULL) {
            anchors[i] = atoi(token);  // Convert string to int
            i++;
            token = strtok(NULL, ", ");
        }
        
        numAnchors = i;
    }

    // Get the mobile node addresses from the topology file
    char* mobilesData;
    if((mobilesData = CNET_getvar("mobiles")) != NULL) {
        char* mobilesStr = mobilesData;
        char* token = strtok(mobilesStr, ", ");
        int i = 0;
        while(token != NULL) {
            mobileNodes[i] = atoi(token);  // Convert string to int
            i++;
            token = strtok(NULL, ", ");
        }
        
        numMobileNodes = i;
    }

    //initialize lastHeardFrom array
    for(int i = 0; i < NANCHORS; i++) {
        lastHeardFrom[i] = -2 * BEACON_INTERVAL;
    }

    // Initialize receivedFromAnchor array
    for(int i = 0; i < numMobileNodes; i++) {
        for(int j = 0; j < numAnchors; j++) {
            receivedFromAnchor[i][j] = false;
        }
    }
    CNET_check_version(CNET_VERSION);
    srand(time(NULL) + nodeinfo.address);

    env = getenv("WALKING_SPEED");
    if(env)
    value	= atof(env);
    double WALKING_SPEED    = (value > 0.0) ? value : DEFAULT_WALKING_SPEED;

    env = getenv("PAUSE_TIME");
    if(env)
    value	= atof(env);
    double PAUSE_TIME       = (value > 0.0) ? value : DEFAULT_PAUSE_TIME;

    if(isMobileNode(nodeinfo.address)) {
        init_mobility(WALKING_SPEED, PAUSE_TIME);
    }
    stats	= CNET_shmem2("s", 2*sizeof(int));

    CHECK(CNET_set_handler(EV_TIMER1, transmit, 0));
    CNET_start_timer(EV_TIMER1, TX_NEXT, 0);

    CHECK(CNET_set_handler(EV_PHYSICALREADY,  receive, 0));

    if(nodeinfo.address == 0) 
    CHECK(CNET_set_handler(EV_PERIODIC,  report, 0));

    if(isAnchorNode(nodeinfo.address)) {
        CHECK(CNET_set_handler(EV_TIMER2, transmit_beacon, 0));
        CNET_start_timer(EV_TIMER2, BEACON_INTERVAL, 0);
    }
}