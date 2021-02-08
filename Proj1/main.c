#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define EMPTY_PCB -1    // Used to signify an empty entry in a PCB
#define MIN_PCBS 6      // Used to specify the minimum number of PCBs for the tests to run properly

// * Version One *
//     • All PCBs are implemented as an array of size n.
//     • Each process is referred to by the PCB index, 0 through n-1.
//     • Each PCB is a structure consisting of only the two fields:
//      ◦ parent: a PCB index corresponding to the process's creator
//      ◦ children: a pointer to a linked list, where each list element contains the PCB index of one child process

// Linked list node
struct Node {
    int index;  // Index of the child in the PCB array
    struct Node *next;
};

// Linked-List PCB struct
struct linkedPCB {
    int parent;             // Index of the parent of this PCB in the PCB array
    struct Node *children;  // Pointer to a linked list of this nodes children
};

/* Function:  linkedCreate
 * -----------------------
 * Allocates a new PCB in the given PCB array, and sets it as a child of the provided parent
 *
 * precondition:    pcbArray has been initialized so that each empty PCB has EMPTY_PCB parent and NULL children
 *
 * pcbArray[]:  An initialized array of linkedPCB structs
 * arraySize:   The size of the pcbArray
 * parentIndex: The index in pcbArray of the parent of the new PCB being created
 *
 * returns:         nothing
 * postcondition:   A new PCB entry has been created as a child of the given parent
 */
void linkedCreate(struct linkedPCB pcbArray[], int arraySize, int parentIndex);

/* Function:  linkedDestroy
 * -----------------------
 * Removes the given PCB in the given PCB array, and removes it as a child of its parent
 *
 * precondition:    The pcb at pcbArray[pcbIndex] has been created
 *
 * pcbArray[]:  An initialized array of linkedPCB structs
 * arraySize:   The size of the pcbArray
 * pcbIndex:    The index in pcbArray of the PCB being destroyed
 *
 * returns:         nothing
 * postcondition:   The specified PCB has been cleared and removed as a child of its parent
 */
void linkedDestroy(struct linkedPCB pcbArray[], int arraySize, int pcbIndex);

/* Function:  linkedTest
 * -----------------------
 * Creates a PCB array of numPCBs size, and runs rounds rounds of create and destroy operations on the PCB array
 *
 * precondition:    numPCBs > MIN_PCBS and rounds > 0
 *
 * numPCBs: The size of the PCB array to be used in the tests
 * rounds:  The number of rounds of create and destroy cycles
 *
 * returns: The runtime in milliseconds to run the specified rounds of create and destroy cycles
 */
long linkedTest(int numPCBs, long rounds);

// * Version Two *
// Version 2 of the same process creation hierarchy uses no linked lists. Instead, each PCB contains the 4 integer
//   fields parent, first_child, younger_sibling, and older_sibling, as described in the subsection "Avoiding linked lists".
struct unlinkedPCB {
    int parent;
    int first_child;
    int younger_sibling;
    int older_sibling;
};

/* Function:  unlinkedCreate
 * -----------------------
 * Allocates a new PCB in the given PCB array, and sets it as a child of the provided parent.
 * Does not use linked lists to track relationships.
 *
 * precondition:    pcbArray has been initialized so that all fields in empty entries = EMPTY_PCB
 *
 * pcbArray[]:  An initialized array of linkedPCB structs
 * arraySize:   The size of the pcbArray
 * parentIndex: The index in pcbArray of the parent of the new PCB being created
 *
 * returns:         nothing
 * postcondition:   A new PCB entry has been created as a child of the given parent
 */
void unlinkedCreate(struct unlinkedPCB pcbArray[], int arraySize, int parentIndex);

/* Function:  unlinkedDestroy
 * -----------------------
 * Removes the given PCB in the given PCB array, and removes it as a child of its parent and as a sibling
 *     of its older and younger siblings
 *
 * precondition:    The pcb at pcbArray[pcbIndex] has been created
 *
 * pcbArray[]:  An initialized array of linkedPCB structs
 * arraySize:   The size of the pcbArray
 * pcbIndex:    The index in pcbArray of the PCB being destroyed
 *
 * returns:         nothing
 * postcondition:   The specified PCB has been cleared, removed as a child of its parent, and removed as a sibling
 */
void unlinkedDestroy(struct unlinkedPCB pcbArray[], int arraySize, int pcbIndex);

/* Function:  unlinkedTest
 * -----------------------
 * Creates a PCB array of numPCBs size, and runs rounds rounds of create and destroy operations on the PCB array
 *
 * precondition:    numPCBs > MIN_PCBS and rounds > 0
 *
 * numPCBs: The size of the PCB array to be used in the tests
 * rounds:  The number of rounds of create and destroy cycles
 *
 * returns: The runtime in milliseconds to run the specified rounds of create and destroy cycles
 */
long unlinkedTest(int numPCBs, long rounds);


/*
 * Prompts a user for a given number of PCBs to create, and a given number of rounds of create/destroy cycles to run
 * Displays the runtime of both linked and unlinked PCB versions, and the difference between the two
 */
int main() {
    //printf("Hello, World!\n");
    //return 0;

    // Get number of PCBs to create, must be at least 5 to satisfy test requirements
    int numPCBs = 0;
    while (numPCBs < 6) {
        printf("Enter number of PCBs to create for both tests (n>=6): ");
        scanf("%d", &numPCBs); // NOLINT(cert-err34-c)
    }

    // Get number of cycles of creation/destruction to run in each test
    long rounds = 0;
    while (rounds < 1) {
        printf("Enter number of rounds of creation/destruction to run for each test (n>0): ");
        scanf("%ld", &rounds); // NOLINT(cert-err34-c)
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
    // Guard clause for trying to destroy an empty PCB
    if (pcbArray[pcbIndex].parent == EMPTY_PCB) {
        printf("\nlinkedDestroy called on empty PCB. pcbIndex: %d", pcbIndex);
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
            // Child frees its own node in its destroy cycle, don't do it here
            // Increment to next node
            curr = next;
        }

        // Clear the PCB pointer to the list of (now deallocated) children
        pcbArray[pcbIndex].children = NULL;
    }

    // free node from parent's children list
    struct Node *ParentChildList = pcbArray[pcbArray[pcbIndex].parent].children;
    if (ParentChildList->index == pcbIndex) {
        struct Node *next = ParentChildList->next;
        free (ParentChildList);
        pcbArray[pcbArray[pcbIndex].parent].children = next;
    }
    else {
        struct Node *prev = ParentChildList;
        struct Node *curr = ParentChildList->next;

        while (curr != NULL && curr->index != pcbIndex) {
            prev = curr;
            curr = prev->next;
        }

        // remove node from middle of list
        prev->next = curr->next;
        // free the removed node
        free(curr);
    }

    // Clear the PCB entry, freeing it for later use
    pcbArray[pcbIndex].parent = EMPTY_PCB;
}

long linkedTest(int numPCBs, long rounds) {
    // Guard clause against invalid arguments
    if (numPCBs < MIN_PCBS || rounds < 1) {
        printf("\nCalled to linkedTest passed invalid arguments. numPCBs: %d, rounds: %d", numPCBs, rounds);
        return -1;
    }

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

    return millis;
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
        printf("unlinkedDestroy called on invalid index. pcbIndex: %d, arraySize: %d", pcbIndex, arraySize);
        return;
    }
    // Guard clause for trying to destroy an empty PCB
    if (pcbArray[pcbIndex].parent == EMPTY_PCB) {
        printf("\nlinkedDestroy called on empty PCB. pcbIndex: %d", pcbIndex);
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
        // Set older sibling's younger sibling to this pcb's younger sibling.  Works even if no younger sibling
        pcbArray[pcbToDelete->older_sibling].younger_sibling = pcbToDelete->younger_sibling;
    }
    else {
        // pcbToDelete is its parent's first child, so first child must be cleared or redirected to the next sibling
        pcbArray[pcbToDelete->parent].first_child = pcbToDelete->younger_sibling;
    }

    // Fix link between older and younger siblings of pcbToDelete
    if (pcbToDelete->younger_sibling != EMPTY_PCB) {
        // Set younger siblings older sibling to this pcbs older sibling.  Works even if no older sibling
        pcbArray[pcbToDelete->younger_sibling].older_sibling = pcbToDelete->older_sibling;
    }

    // Clear the PCB entry, freeing it for later use
    pcbToDelete->parent = EMPTY_PCB;
    pcbToDelete->first_child = EMPTY_PCB;
    pcbToDelete->older_sibling = EMPTY_PCB;
    pcbToDelete->younger_sibling = EMPTY_PCB;
}

long unlinkedTest(int numPCBs, long rounds) {
    // Guard clause against invalid arguments
    if (numPCBs < MIN_PCBS || rounds < 1) {
        printf("\nCalled to linkedTest passed invalid arguments. numPCBs: %d, rounds: %d", numPCBs, rounds);
        return -1;
    }

    // Create and initialize the empty array of PCBs
    struct unlinkedPCB pcbArray[numPCBs];

    for (int i = 0; i < numPCBs; i++) {
        pcbArray[i].parent = EMPTY_PCB;
        pcbArray[0].younger_sibling = EMPTY_PCB;
        pcbArray[0].older_sibling = EMPTY_PCB;
        pcbArray[0].first_child = EMPTY_PCB;
    }
    pcbArray[0].parent = 0;  // first node parent is set to self otherwise create will overwrite pcbArray[0]

    // record start time (clicks)
    clock_t t;
    t = clock();

    // Run the test
    for (int i = 0; i < rounds; i++) {
        // Create children
        unlinkedCreate(pcbArray, numPCBs, 0);
        unlinkedCreate(pcbArray, numPCBs, 0);
        unlinkedCreate(pcbArray, numPCBs, 2);
        unlinkedCreate(pcbArray, numPCBs, 3);
        unlinkedCreate(pcbArray, numPCBs, 0);

        unlinkedDestroy(pcbArray, numPCBs, 2); // Destroy all children
        unlinkedDestroy(pcbArray, numPCBs, 1);
        unlinkedDestroy(pcbArray, numPCBs, 5);
    }

    // Record clicks elapsed
    t = clock() - t;
    // Convert clicks to milliseconds
    long millis = (long) ((((float) t) / CLOCKS_PER_SEC) * 1000);

    return millis;

}


/*
 TEST RUNS
 -------------------------------------
 Run 1
 -------------------------------------
 Enter number of PCBs to create for both tests (n>=6): 100
 Enter number of rounds of creation/destruction to run for each test (n>0): 1000000

 Version 1 used 163 milliseconds
 Version 2 used 97 milliseconds
 Version 1 is 66 milliseconds slower

 -------------------------------------
 Run 2
 -------------------------------------
 Enter number of PCBs to create for both tests (n>=6): 10
 Enter number of rounds of creation/destruction to run for each test (n>0): 1000000

 Version 1 used 169 milliseconds
 Version 2 used 95 milliseconds
 Version 1 is 74 milliseconds slower

 -------------------------------------
 Run 3
 -------------------------------------
 Enter number of PCBs to create for both tests (n>=6): 100
 Enter number of rounds of creation/destruction to run for each test (n>0): 10000000

 Version 1 used 1481 milliseconds
 Version 2 used 956 milliseconds
 Version 1 is 525 milliseconds slower

 */