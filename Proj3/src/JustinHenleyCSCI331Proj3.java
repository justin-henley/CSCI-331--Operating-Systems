/*
Project 3:      Banker's Algorithm
Description:    Demonstrates use of claim graphs and implementation of the banker's algorithm
Author:         Justin Henley, jahenley@mail.fhsu.edu
Date:           2021-04-15
 */

// TODO SAMPLE RUN

import java.util.Scanner;

// Main class drives the interactive program
public class JustinHenleyCSCI331Proj3 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Read in initial conditions of system
        Object[] init = new Object[4];
        promptInitialState(init);
        int processes = (int) init[0];
        int resources = (int) init[1];
        int[] resourceUnits = (int[]) init[2];
        int[][] maxClaims = (int[][]) init[3];

        // Create a new representation of the system
        systemRepresentation state = new systemRepresentation(processes, resources, resourceUnits, maxClaims);
        // Create a deadlock-reachable representation for proving the Banker's algorithm is working
        // resourceUnits and maxClaims must be cloned to avoid state and dead-state aliasing the same array in memory
        deadlockSystem deadState = new deadlockSystem(processes, resources, resourceUnits.clone(), maxClaims.clone());

        // Begin an interactive session with user
        // Print the instructions prompt before allowing input
        promptSession();
        String entry = "";
        // Continue reading inputs until 'exit' command
        while(!entry.equals("exit")) {
            entry = input.next();
            if (!entry.equals("exit")) {
                // Attempt to execute the command and print the result
                System.out.println(entryParser(entry, state, deadState));
            }
        }
    }

    // Prompts the user for the initial state of the system, and returns the initialization information
    // in an Object array.
    // Precondition: init is an Object array of length 4
    // Postcondition: init contains the initial state of the system
    private static void promptInitialState(Object[] init) {
        // Create a local scanner instance
        Scanner input = new Scanner(System.in);

        // Prompt for # of processes and resources
        int processes = 0, resources = 0;

        System.out.println("Enter the number of processes: ");
        while (processes <= 0) {
            // Force positive value
            processes = input.nextInt();
        }
        System.out.println("Enter the number of resources: ");
        while (resources <= 0) {
            // Force positive value
            resources = input.nextInt();
        }

        // Array to hold the # of units within each resource
        int[] resourceUnits = new int[resources];
        // Array to hold the max claim of each process
        int[][] maxClaims = new int[processes][resources];

        // Prompt for resource units
        for (int i = 0; i < resources; i++) {
            System.out.println("Please specify the number of units for Resource #" + i);
            while (resourceUnits[i] <= 0) {
                // Force positive value
                resourceUnits[i] = input.nextInt();
            }
        }
        // Prompt for max claims
        for (int i = 0; i < processes; i++) {
            System.out.println("Please specify the maximum claim of Process #" + i);
            for (int j = 0; j < resources; j++) {
                System.out.println("Resource #" + j + ":");
                do {
                    // Force non-negative value
                    maxClaims[i][j] = input.nextInt();
                } while (maxClaims[i][j] < 0);
            }
        }

        // Store values into init array to pass back to main
        init[0] = processes;
        init[1] = resources;
        init[2] = resourceUnits;
        init[3] = maxClaims;
    }

    // Prompts the user with instructions before the interactive session starts
    private static void promptSession() {
        System.out.println("\nSystem representation created, you may begin making requests or releases.");
        System.out.println("To make a request, type request(i,j,k)");
        System.out.println("To make a release, type release(i,j,k)");
        System.out.println("i = a process number, j = a resource number, k = # of units of resource j requested or released.");
        System.out.println("Type exit to end program");
    }

    // Parses the entry string from the user, and executes any valid commands with the given state and deadState
    // Preconditions: state and deadState have been initialized with the same initial state
    // Postcondition:  Returns a string communicating the result of the given command
    private static String entryParser(String entry, systemRepresentation state, deadlockSystem deadState) {
        // Tokenize input string to separate expected arguments
        String[] entrySplit = entry.split("[(\\(,\\)]+");

        // Check for valid number of arguments
        if (entrySplit.length != 4) {
            return "Invalid entry";
        }
        // Process a request or release command
        else if (entrySplit[0].equals("request") || entrySplit[0].equals("release")) {
            // Create an array to hold to arguments
            int[] entryArgs = new int[3];
            // Extract the arguments as integers
            try {
                for (int i = 0; i < 3; i++) {
                    entryArgs[i] = Integer.parseInt(entrySplit[i+1]);
                }
            }
            catch (Exception e) {
                return "Invalid argument(s), please enter integers only.";
            }

            // Attempt the operation
            if (entrySplit[0].equals("request")) {
                // Uses string concatenation to append a message about deadlock from the unsafe version
                return state.request(entryArgs[0], entryArgs[1], entryArgs[2]) + deadState.request(entryArgs[0], entryArgs[1], entryArgs[2]);
            }
            else {
                deadState.release(entryArgs[0], entryArgs[1], entryArgs[2]);
                return state.release(entryArgs[0], entryArgs[1], entryArgs[2]);
            }
        }

        // If the command did not match a valid command, return
        else return "Command not found, please try again.";
    }
}

// Represents a system of processes and resources and provides for requests and releases of resources
// Uses the Banker's Algorithm to ensure only safe states are reached
class systemRepresentation {
    protected int numberOfProcesses;
    protected int numberOfResources;
    protected int[][] maxClaimsOfProcesses;
    protected int[] currentUnitsAvailable;
    protected int[][] currentAllocation;

    public systemRepresentation(int numberOfProcesses, int numberOfResources, int[] currentUnitsAvailable, int[][] maxClaimsOfProcesses) {
        this.numberOfProcesses = numberOfProcesses;
        this.numberOfResources = numberOfResources;
        this.maxClaimsOfProcesses = maxClaimsOfProcesses;
        this.currentUnitsAvailable = currentUnitsAvailable;
        currentAllocation = new int[numberOfProcesses][numberOfResources];
    }

    // Checks that the process and resource number from a given command are within the valid ranges
    // Precondition: numberOfProcesses and numberOfResources are both positive
    // Postcondition: returns true if both processNumber and resourceNumber are valid indices
    protected boolean checkValid(int processNumber, int resourceNumber) {
        boolean processValid = !(processNumber < 0 || processNumber > numberOfProcesses - 1);
        boolean resourceValid = !(resourceNumber < 0 || resourceNumber > numberOfResources - 1);
        return processValid && resourceValid;
    }

    // Attempts to complete a request by processNumber for unitsRequested units of resourceNumber
    // Postcondition: currentAllocation and currentUnitsAvailable are updated if the new state is determined safe
    //                returns a string detailing the result of the request attempt
    public String request(int processNumber, int resourceNumber, int unitsRequested) {
        // Check for valid resource and process indices
        if (!checkValid(processNumber, resourceNumber))
            return "Request failed: Invalid process or resource number";

        // Check if quantity requested of resource is available
        if (currentUnitsAvailable[resourceNumber] < unitsRequested) {
            return "Requested failed: Units requested exceeds units available.";
        }
        // Check if request exceeds max claim of process
        if (currentAllocation[processNumber][resourceNumber] + unitsRequested > maxClaimsOfProcesses[processNumber][resourceNumber]) {
            return "Request failed: Maximum claim of process exceeded.";
        }

        // Grant the request temporarily
        // Update available resources
        currentUnitsAvailable[resourceNumber] -= unitsRequested;
        // Update current allocation
        currentAllocation[processNumber][resourceNumber] += unitsRequested;

        // If new state is safe, return and confirm;
        if (isSafeState()) return "Request successful";
        // Otherwise, reverse the allocation, return and declare unsafe
        release(processNumber, resourceNumber, unitsRequested);
        return "Request denied: Unsafe state";
    }

    // Releases unitsReleased units of resourceNumber held by processNumber
    // Postconditions: currentAllocation and currentUnitsAvailable are updated
    //                 returns a string detailing the result of the release attempt
    public String release(int processNumber, int resourceNumber, int unitsReleased) {
        // Check for valid resource and process indices
        if (!checkValid(processNumber, resourceNumber))
            return "Release failed: Invalid process or resource number";

        // Check if quantity of resource to release is actually claimed by process
        if (currentAllocation[processNumber][resourceNumber] < unitsReleased) {
            return "Release failed: Number of units to release exceeds current allocation by process";
        }

        // Release the units
        currentAllocation[processNumber][resourceNumber] -= unitsReleased;
        currentUnitsAvailable[resourceNumber] += unitsReleased;
        return "Release successful.";
    }

    // Checks if the current state is safe under the Banker's Algorithm
    // Postcondition: returns true if the state is safe from deadlock
    public boolean isSafeState() {
        // A copy of available units to pass to needLess
        int [] free = currentUnitsAvailable.clone();
        // Track whether each process can meet its claims and finish, initializes to false by default
        boolean[] processFinished = new boolean[numberOfProcesses];

        // Iterate over all processes and see if one can be reduced on this pass
        // If a pass finishes without a reduction, deadlock has been found, state is not safe
        boolean hasOneToRemove = true;
        for (int count = numberOfProcesses; count > 0 && hasOneToRemove; count++) {
            hasOneToRemove = false;
            // For each process, check if it can be removed. Loops end if a process to remove has been found
            for (int i = 0; i < numberOfProcesses && !hasOneToRemove; i++) {
                if (!processFinished[i] && needLess(i, free)) {
                    processFinished[i] = true;  // process i can finish
                    // Release i's resources
                    for (int j = 0; j < numberOfResources; j++) {
                        free[j] += currentAllocation[i][j];
                    }
                    // Can reduce a process, break out of for loop and continue to next round of outer loop
                    hasOneToRemove = true;
                }
            }
        }

        // Check all processes to ensure all have finished and state is completely reducible
        for (int i = 0; i < numberOfProcesses; i++) {
            if(!processFinished[i]) return false;
        }
        // If all processes are reduced, return true to signal a safe state
        return true;
    }

    // Checks if a process is reducible
    // Postcondition: Returns true if the process is reducible
    public boolean needLess(int process, int[] free) {
        // If a needed resource is not available, return false
        for (int resource = 0; resource < numberOfResources; resource++) {
            // Check if available resources are sufficient to meet the process' max claim and reduce the process
            if (maxClaimsOfProcesses[process][resource] - currentAllocation[process][resource] > free[resource]) {
                return false; // Not enough of resource to meet max claim of process
            }
        }
        return true; // Process is reducible
    }
}

// Extends systemRepresentation to create an unsafe system representation that allows for and detects deadlock
class deadlockSystem extends systemRepresentation {
    public deadlockSystem(int numberOfProcesses, int numberOfResources, int[] resourceUnitsAvailable, int[][] maxClaimsOfProcesses) {
        super(numberOfProcesses, numberOfResources, resourceUnitsAvailable, maxClaimsOfProcesses);
    }

    // Determines if the current state is a deadlocked state
    // Postcondition: Returns true if the current state is deadlocked
    public boolean isDeadlocked() {
        // If a process cannot finish even if given all resources, then the system is deadlocked
        int count = 0; // Number of processes that cannot finish
        boolean deadlockDetected;
        // Iterate over each process
        for(int i = 0; i < numberOfProcesses; i++) {
            deadlockDetected = false;
            // Iterate over each resource for process
            for(int j = 0; !deadlockDetected && j < numberOfResources; j++) {
                if(maxClaimsOfProcesses[i][j] - currentAllocation[i][j] > currentUnitsAvailable[j])
                    deadlockDetected = true;  // This process cannot be released
            }
            if (deadlockDetected)
                count ++;
        }
        // If no processes can finish, deadlock has been reached
        return count == numberOfProcesses;
    }

    // A copy of request from systemRepresentation, but only speaks up when deadlock is found
    public String request(int processNumber, int resourceNumber, int unitsRequested) {
        // Check for valid resource and process indices
        if (!checkValid(processNumber, resourceNumber))
            return "";

        // Check if quantity requested of resource is available
        if (currentUnitsAvailable[resourceNumber] < unitsRequested) {
            return "";
        }
        // Check if request exceeds max claim of process
        if (currentAllocation[processNumber][resourceNumber] + unitsRequested > maxClaimsOfProcesses[processNumber][resourceNumber]) {
            return "";
        }

        // Grant the request temporarily
        // Update available resources
        currentUnitsAvailable[resourceNumber] -= unitsRequested;
        // Update current allocation
        currentAllocation[processNumber][resourceNumber] += unitsRequested;

        // If new state is deadlocked, return and announce;
        if (isDeadlocked()) return " ** Deadlock detected in unsafe version **";
        else return "";
    }

    // Copy of release from systemRepresentation, but return value is always "" since it doesn't need to communicate with user
    public String release(int processNumber, int resourceNumber, int unitsReleased) {
        // Check for valid resource and process indices, and if  quantity of resource to release is actually claimed by process
        if (checkValid(processNumber, resourceNumber) && currentAllocation[processNumber][resourceNumber] >= unitsReleased) {
            // Release the units
            currentAllocation[processNumber][resourceNumber] -= unitsReleased;
            currentUnitsAvailable[resourceNumber] += unitsReleased;
        }
        return "";
    }
}

// SAMPLE RUNS

// The 5.3.3 Participation Activity state example
/*
Enter the number of processes:
3
Enter the number of resources:
2
Please specify the number of units for Resource #0
2
Please specify the number of units for Resource #1
3
Please specify the maximum claim of Process #0
Resource #0:
2
Resource #1:
1
Please specify the maximum claim of Process #1
Resource #0:
2
Resource #1:
2
Please specify the maximum claim of Process #2
Resource #0:
2
Resource #1:
2

System representation created, you may begin making requests or releases.
To make a request, type request(i,j,k)
To make a release, type release(i,j,k)
i = a process number, j = a resource number, k = # of units of resource j requested or released.
Type exit to end program
request(0,1,1)
Request successful
request(2,1,2)
Request successful
request(1,0,1)
Request denied: Unsafe state ** Deadlock detected in unsafe version **
request(0,0,1)
Request successful ** Deadlock detected in unsafe version **
release(0,0,1)
Release successful.
release(0,1,1)
Release successful.
release(2,1,2)
Release successful.
exit

Process finished with exit code 0
 */

// Participation activity 5.3.9 example
/*
Enter the number of processes:
4
Enter the number of resources:
3
Please specify the number of units for Resource #0
1
Please specify the number of units for Resource #1
1
Please specify the number of units for Resource #2
1
Please specify the maximum claim of Process #0
Resource #0:
1
Resource #1:
1
Resource #2:
0
Please specify the maximum claim of Process #1
Resource #0:
0
Resource #1:
1
Resource #2:
1
Please specify the maximum claim of Process #2
Resource #0:
1
Resource #1:
1
Resource #2:
0
Please specify the maximum claim of Process #3
Resource #0:
0
Resource #1:
0
Resource #2:
1

System representation created, you may begin making requests or releases.
To make a request, type request(i,j,k)
To make a release, type release(i,j,k)
i = a process number, j = a resource number, k = # of units of resource j requested or released.
Type exit to end program
request(1,2,1)
Request successful
request(2,0,1)
Request successful
request(0,1,1)
Request denied: Unsafe state ** Deadlock detected in unsafe version **
request(2,1,1)
Request successful
exit

Process finished with exit code 0
 */

// General demonstration of error handling
/*
Enter the number of processes:
0
-1
1
Enter the number of resources:
0
-1
1
Please specify the number of units for Resource #0
0
1
Please specify the maximum claim of Process #0
Resource #0:
-1
1

System representation created, you may begin making requests or releases.
To make a request, type request(i,j,k)
To make a release, type release(i,j,k)
i = a process number, j = a resource number, k = # of units of resource j requested or released.
Type exit to end program
request(0,0,1)
Request successful
release(0,0,1)
Release successful.
request(0,0,2)
Requested failed: Units requested exceeds units available.
release(0,0,2)
Release failed: Number of units to release exceeds current allocation by process
request(0,1,1)
Request failed: Invalid process or resource number
request(1,2,3)
Request failed: Invalid process or resource number
release(4,5,6)
Release failed: Invalid process or resource number
banana
Invalid entry
exit

Process finished with exit code 0
 */