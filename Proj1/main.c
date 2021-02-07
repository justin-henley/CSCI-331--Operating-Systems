#include <stdio.h>
#include <stdlib.h>
#include <time.h>

// Used to signify an empty entry in a PCB
#define EMPTY_PCB -1

// * Version One *
//     • All PCBs are implemented as an array of size n.
//     • Each process is referred to by the PCB index, 0 through n-1.
//     • Each PCB is a structure consisting of only the two fields:
//      ◦ parent: a PCB index corresponding to the process's creator
//      ◦ children: a pointer to a linked list, where each list element contains the PCB index of one child process
struct Node {
    int index;
    struct Node *next;
};

struct linkedPCB {
    int parent;
    struct Node *children;
};

void linkedCreate(struct linkedPCB pcbArray[], int arraySize, int parentIndex);


void linkedDestroy(struct linkedPCB pcbArray[], int arraySize, int pcbIndex);

long linkedTest(int numPCBs, long rounds) {
    // Create and initialize the empty array of PCBs
    struct linkedPCB pcbArray[numPCBs];
    pcbArray[0].parent = 0;
    pcbArray[0].children = NULL;
    for (int i = 1; i < numPCBs; i++) {
        pcbArray[i].parent = EMPTY_PCB;
        pcbArray[i].children = NULL;
    }

    // record start time (clicks)
    clock_t t;
    t = clock();

    // Run the test
    for (int i = 0; i < rounds; i++) {
        // Create children
        linkedCreate(pcbArray, numPCBs, 0);
        linkedCreate(pcbArray, numPCBs, 0);
        linkedCreate(pcbArray, numPCBs, 2);
        linkedCreate(pcbArray, numPCBs, 3);
        linkedCreate(pcbArray, numPCBs, 0);

        linkedDestroy(pcbArray, numPCBs, 2); // Destroy all children
        linkedDestroy(pcbArray, numPCBs, 1);
        linkedDestroy(pcbArray, numPCBs, 5);
    }

    // Record clicks elapsed
    t = clock() - t;
    // Convert clicks to milliseconds
    long millis = (long) ((((float) t) / CLOCKS_PER_SEC) * 1000);

    return (long)t;
}

// * Version Two *
// Version 2 of the same process creation hierarchy uses no linked lists. Instead, each PCB contains the 4 integer
//   fields parent, first_child, younger_sibling, and older_sibling, as described in the subsection "Avoiding linked lists".
struct unlinkedPCB {
    int parent;
    int first_child;
    int younger_sibling;
    int older_sibling;
};

void unlinkedCreate(struct unlinkedPCB pcbArray[], int arraySize, int parentIndex);

void unlinkedDestroy(struct unlinkedPCB pcbArray[], int arraySize, int pcbIndex);

long unlinkedTest(int numPCBs, long rounds) {
    return 3;
}

int main() {
    //printf("Hello, World!\n");
    //return 0;

    // Get number of PCBs to create, must be at least 5 to satisfy test requirements
    int numPCBs = 0;
    while (numPCBs < 6) {
        printf("Enter number of PCBs to create for both tests (n>=6): ");
        scanf("%d", &numPCBs);
    }

    // Get number of cycles of creation/destruction to run in each test
    long rounds = 0;
    while (rounds < 1) {
        printf("Enter number of rounds of creation/destruction to run for each test (n>0): ");
        scanf("%ld", &rounds);
    }

    // Call test functions with given size and store the time taken
    long linkedTestTime, unlinkedTestTime;
    linkedTestTime = linkedTest(numPCBs, rounds);
    unlinkedTestTime = unlinkedTest(numPCBs, rounds);
    long difference = linkedTestTime - unlinkedTestTime;

    // Print execution times for both methods
    printf("\nVersion 1 used %ld milliseconds", linkedTestTime);
    printf("\nVersion 2 used %ld milliseconds", unlinkedTestTime);

    // Print difference in execution times
    if (difference > 0)
        printf("\nVersion 1 is %ld milliseconds slower\n", difference);
    else if (difference < 0)
        printf("\nVersion 2 is %ld milliseconds slower\n", -difference);
    else
        printf("\nBoth versions executed in exactly the same amount of time. What are the odds?\n");

    return 0;
}

void linkedCreate(struct linkedPCB *pcbArray, int arraySize, int parentIndex) {
    //  allocate a free PCB[q]
    // find an empty slot in the PCB array
    int newPCBIndex = 0;
    while (pcbArray[newPCBIndex].parent != EMPTY_PCB && newPCBIndex < arraySize) {
        newPCBIndex++;
    }
    //  record the parent's index, p, in PCB[q]
    pcbArray[newPCBIndex].parent = parentIndex;
    //  initialize the list of children of PCB[q] as empty
    pcbArray[newPCBIndex].children = NULL;

    //  create a new link containing the child's index q and appends the link to the linked list of PCB[p]
    // Points to the end of the parent's children list
    struct Node *tail = pcbArray[parentIndex].children;
    // New node to be inserted in the parent's children list
    struct Node *newNode = (struct Node *) malloc(sizeof(struct Node));
    newNode->index = newPCBIndex;
    newNode->next = NULL;

    // If parent node has no children, set children to the new node
    if (tail == NULL) {
        pcbArray[parentIndex].children = newNode;
    }
    else {  // parent has at least one child
        // Find end of linked list
        while (tail->next != NULL) {
            tail = tail->next;
        }
        // Append new node to end of linked list
        tail->next = newNode;
    }
}

void linkedDestroy(struct linkedPCB *pcbArray, int arraySize, int pcbIndex) {
    // Guard clause for invalid pcbIndex
    if (pcbIndex >= arraySize) {
        printf("\nlinkedDestroy called on invalid index. pcbIndex: %d, arraySize: %d", pcbIndex, arraySize);
        return;
    }

    // Recursively destroy all progeny
    struct Node *childrenHead = pcbArray[pcbIndex].children;

    // If there are children, recursively destroy all children
    if (childrenHead != NULL) {
        struct Node *curr = childrenHead, *next;
        // Traverse the list of children
        while (curr != NULL) {
            // Set next here to avoid referencing a null curr pointer at end of this while
            next = curr->next;
            // Destroy the child
            linkedDestroy(pcbArray, arraySize, curr->index);
            // Free the list node
            free (curr);
            // Increment to next node
            curr = next;
        }

        // Clear the PCB pointer to the list of (now deallocated) children
        pcbArray[pcbIndex].children = NULL;
    }

    // Clear the PCB entry, freeing it for later use
    pcbArray[pcbIndex].parent = EMPTY_PCB;
}

void unlinkedCreate(struct unlinkedPCB *pcbArray, int arraySize, int parentIndex) {
    // allocate a free PCB[q]
    // find an empty slot in the PCB array
    int newPCBIndex = 0;
    while (pcbArray[newPCBIndex].parent != EMPTY_PCB && newPCBIndex < arraySize) {
        newPCBIndex++;
    }
    // Create a pointer to the new PCB entry for readability
    struct unlinkedPCB *newPCB = &pcbArray[newPCBIndex];

    // record the parent's index, p, in PCB[q]
    newPCB->parent = parentIndex;
    // set first_child and younger_sibling to empty
    newPCB->first_child = EMPTY_PCB;
    newPCB->younger_sibling = EMPTY_PCB;

    // record older_sibling
    // Parent has no children
    if (pcbArray[parentIndex].first_child == EMPTY_PCB) {
        // Set first_child of the parent process to the new PCB
        pcbArray[parentIndex].first_child = newPCBIndex;
        // new PCB has no older siblings
        newPCB->older_sibling = EMPTY_PCB;
    }
        // Parent has at least one child
    else {
        int olderSiblingIndex = pcbArray[parentIndex].first_child;
        while (pcbArray[olderSiblingIndex].younger_sibling != EMPTY_PCB) {
            olderSiblingIndex = pcbArray[olderSiblingIndex].younger_sibling;
        }
        // Set younger_sibling of the youngest extant child of the parent process
        pcbArray[olderSiblingIndex].younger_sibling = newPCBIndex;
        // Set older_sibling of the new PCB
        newPCB->older_sibling = olderSiblingIndex;
    }
}

void unlinkedDestroy(struct unlinkedPCB *pcbArray, int arraySize, int pcbIndex) {
    // Guard clause for invalid pcbIndex
    if (pcbIndex >= arraySize) {
        printf("unlinkedDestroy called on invalid index");
        return;
    }

    // Pointer to the current PCB for readability
    struct unlinkedPCB *pcbToDelete = &pcbArray[pcbIndex];

    // Recursively destroy all progeny
    int childrenHead = pcbToDelete->first_child;

    // If there are children, recursively destroy all children
    if (childrenHead != EMPTY_PCB) {
        int curr = childrenHead, next = pcbArray[childrenHead].younger_sibling;
        // Traverse the list of children
        while (curr != EMPTY_PCB) {
            // Destroy the child
            unlinkedDestroy(pcbArray, arraySize, curr);
            // Move to next child
            curr = next;
            next = pcbArray[curr].younger_sibling;
        }
    }

    // Disconnect the older sibling of this PCB if it has one
    if (pcbToDelete->older_sibling != EMPTY_PCB) {
        pcbArray[pcbToDelete->older_sibling].younger_sibling = EMPTY_PCB;
    }

        // Otherwise this PCB is the first child of its parent, remove it from the parent PCB
    else {
        pcbArray[pcbToDelete->parent].first_child = EMPTY_PCB;
    }

    // Clear the PCB entry, freeing it for later use
    pcbToDelete->parent = EMPTY_PCB;
    pcbToDelete->first_child = EMPTY_PCB;
    pcbToDelete->older_sibling = EMPTY_PCB;
    pcbToDelete->younger_sibling = EMPTY_PCB;
}
