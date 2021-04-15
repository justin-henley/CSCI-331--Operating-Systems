import java.util.Scanner;

public class JustinHenleyCSCI331Proj3 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        // TODO handle non-integer inputs
        // Read in initial conditions of system
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
                while (maxClaims[i][j] <= 0) {
                    // Force positive value
                    maxClaims[i][j] = input.nextInt();
                }
            }
        }

        // Create a new representation of the system
        systemRepresentation state = new systemRepresentation(processes, resources, resourceUnits, maxClaims);

        // Begin an interactive session with user
        // Print the instructions prompt before allowing input
        promptSession();
        String entry = "";
        // Continue reading inputs until 'exit' command
        while(!entry.equals("exit")) {
            entry = input.next();
            if (!entry.equals("exit")) {
                // Attempt to execute the command and print the result
                System.out.println(entryParser(entry, state));
            }
        }
    }
    
    private static void promptSession() {
        System.out.println("\nSystem representation created, you may begin making requests or releases.");
        System.out.println("To make a request, type request(i,j,k)");
        System.out.println("To make a release, type release(i,j,k)");
        System.out.println("i = a process number, j = a resource number, k = # of units of resource j requested or released.");
        System.out.println("Type exit to end program");
    }

    private static String entryParser(String entry, systemRepresentation state) {
        // Tokenize input string to separate expected arguments
        String[] entrySplit = entry.split("[(\\(,\\)]+");

        // Check for valid number of arguments
        if (entrySplit.length != 4) {
            return "Invalid entry";
        }
        // Process a request command
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
                return state.request(entryArgs[0], entryArgs[1], entryArgs[2]);
            }
            else {
                return state.release(entryArgs[0], entryArgs[1], entryArgs[2]);
            }
        }

        // If the command did not match a valid command, return
        else return "Command not found, please try again.";
    }
}

class systemRepresentation {
    private int numberOfProcesses;
    private int numberOfResources;
    private int[] unitsOfEachResource;
    private int[][] maxClaimsOfProcesses;
    private int[] currentUnitsAvailable;
    private int[][] currentAllocation;

    public systemRepresentation(int numberOfProcesses, int numberOfResources, int[] unitsOfEachResource, int[][] maxClaimsOfProcesses) {
        this.numberOfProcesses = numberOfProcesses;
        this.numberOfResources = numberOfResources;
        this.unitsOfEachResource = unitsOfEachResource;
        this.maxClaimsOfProcesses = maxClaimsOfProcesses;

        currentUnitsAvailable = unitsOfEachResource.clone();
        currentAllocation = new int[numberOfProcesses][numberOfResources];
    }

    // TODO block comment
    private boolean checkValid(int processNumber, int resourceNumber) {
        boolean processValid = (processNumber < 0 || processNumber > numberOfProcesses - 1);
        boolean resourceValid = (resourceNumber < 0 || resourceNumber > numberOfResources - 1);
        return processValid && resourceValid;
    }

    // TODO block comment
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

    // TODO comment
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

    // TODO comment
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

    // TODO comment
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
