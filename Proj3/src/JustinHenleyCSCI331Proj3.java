import java.util.Scanner;

public class JustinHenleyCSCI331Proj3 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

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
        int[] maxClaims = new int[processes];

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
            while (maxClaims[i] <= 0) {
                // Force positive value
                maxClaims[i] = input.nextInt();
            }
        }

        // Create a new representation of the system
        systemRepresentation state = new systemRepresentation(processes, resources, resourceUnits, maxClaims);

        // TODO Begin an interactive session with user

        // TODO Respond whether each request has been granted or denied
    }
}

class systemRepresentation {
    public systemRepresentation(int processes, int resources, int[] resourcesUnits, int[] maxClaims) {

    }
}
