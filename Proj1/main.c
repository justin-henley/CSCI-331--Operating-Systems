#include <stdio.h>
#include <stdlib.h>


// TODO I think my use of null in the PCB array is going to fail, but I can't remember how to do it
// TODO switched for the macro below, might work?
// Number of PCBs in each array of PCBs
#define MAX_PCBS 100 // TODO you stopped using this?
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
    int parentIndex;
    struct Node *children;
};

void linkedCreate(struct linkedPCB pcbArray[], int arraySize, int parentIndex) {
    //  allocate a free PCB[q]
    // find an empty slot in the PCB array
    int newPCBIndex = 0;
    while (pcbArray[newPCBIndex].parentIndex != EMPTY_PCB && newPCBIndex < arraySize) {
        newPCBIndex++;
    }
    //  record the parent's index, p, in PCB[q]
    pcbArray[newPCBIndex].parentIndex = parentIndex;
    //  initialize the list of children of PCB[q] as empty
    pcbArray[newPCBIndex].children = NULL;

    //  create a new link containing the child's index q and appends the link to the linked list of PCB[p]
    struct Node *tail = pcbArray[parentIndex].children;
    while (tail->next != NULL) {
        tail = tail->next;
    }
    tail->next = (struct Node*) malloc(sizeof(struct Node));
    tail->next->index = newPCBIndex;
    tail->next->next = NULL;
}

void linkedDestroy(struct linkedPCB pcbArray[MAX_PCBS - 1], int pcbIndex) {
    // Recursively destroy all progeny
    struct Node *childrenHead = pcbArray[pcbIndex].children;

    // If there are children, recursively destroy all children
    if (childrenHead != NULL) {
        struct Node *curr = childrenHead, *next = childrenHead->next;
        // Traverse the list of children
        while (curr != NULL) {
            // Destroy the child
            linkedDestroy(pcbArray, curr->index);
            // Free the list node
            free (curr);
            // Increment to next node
            curr = next;
            next = curr->next;
        }

        // Clear the PCB pointer to the list of (now deallocated) children
        pcbArray[pcbIndex].children = NULL;
    }

    // Clear the PCB entry, freeing it for later use
    pcbArray[pcbIndex].parentIndex = EMPTY_PCB;
}

void recursiveLinkedDestroy() {}

// * Version Two *
// Version 2 of the same process creation hierarchy uses no linked lists. Instead, each PCB contains the 4 integer
//   fields parent, first_child, younger_sibling, and older_sibling, as described in the subsection "Avoiding linked lists".
struct unlinkedPCB {
    int parent;
    int first_child;
    int younger_sibling;
    int older_sibling;
};

void unlinkedCreate() {

}

void unlinkedDestroy() {

}

int main() {
    //printf("Hello, World!\n");
    //return 0;

}
