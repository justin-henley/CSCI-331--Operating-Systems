/*
Project 4:      Performance of Page Replacement Algorithms
Description:    Compares the performance of page replacement algorithms for fixed numbers of frames: Optimal, FIFO, LRU, Second Chance
Author:         Justin Henley, jahenley@mail.fhsu.edu
Date:           2021-05-06
 */


import java.util.*;

public class JustinHenleyCSCI331Proj4 {

    // TODO add comments
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        int sizeOfVM, lengthOfRS, sizeOfLocus, rateOfMotion, numOfFrames;
        double prob;
        char choice;

        do {
            // Get user spec for test run
            System.out.println("Enter size of virtual memory: ");
            sizeOfVM = input.nextInt();
            System.out.println("Enter length of reference string: ");
            lengthOfRS = input.nextInt();
            System.out.println("Enter size of locus: ");
            sizeOfLocus = input.nextInt();
            System.out.println("Enter rate of motion: ");
            rateOfMotion = input.nextInt();
            System.out.println("Enter probability of transition: ");
            prob = input.nextDouble();
            System.out.println("Enter number of frames: ");
            numOfFrames = input.nextInt();

            // Create the new reference string
            ArrayList<Integer> rs = createRS(sizeOfVM, lengthOfRS, sizeOfLocus, rateOfMotion, prob);

            // Report results
            // TODO make this more interesting with a horizontal bar graph
            System.out.println("The number of page faults using the FIFO replacement algorithm: ");
            System.out.println(FIFOReplacement(rs, numOfFrames));
            System.out.println("The number of page faults using the LRU replacement algorithm: ");
            System.out.println(LRUReplacement(rs, numOfFrames));
            System.out.println("The number of page faults using the Optimal replacement algorithm: ");
            System.out.println(OptimalReplacement(rs, numOfFrames));
            System.out.println("The number of page faults using the Second Chance replacement algorithm: ");
            System.out.println(SecondChanceReplacement(rs, numOfFrames));

            // TODO remove this
            // Show a run of test function
            System.out.println("Test function result: " + Arrays.toString(test()));

            // Prompt for continuation
            System.out.println("Do you want to run another test? Y/N");
            input.nextLine();  // Clear out newline character left in stream
            choice = input.nextLine().toUpperCase().charAt(0);  // Take new input from user
        } while(choice == 'Y');
    }

    // TODO add comments
    // TODO fix return value and args
    private static ArrayList<Integer> createRS(int sizeOfVM, int length, int sizeOfLocus, int rateOfMotion, double prob) {
        // Create a new array list to store the reference string (RS)
        ArrayList<Integer> result = new ArrayList<>();
        int start = 0;
        int n;  // A page number in a reference string, declared out here for persistence across while iterations

        // Repeat until desired size is reached
        while(result.size() < length) {
            // Add size of locus random number in it
            for (int i = 0; i < rateOfMotion; i++) {
                n = (int) (Math.random() * sizeOfLocus + start);
                result.add(n);
            }
            // Generate a random number between 0 and 1 to decide whether to transition
            if (Math.random() < prob)
                start = (int) (Math.random() * sizeOfVM);
            else
                start = (start + 1) % sizeOfVM;
        }
        return result;
    }

    // TODO add comments
    private static int FIFOReplacement(ArrayList<Integer> rs, int numOfFrames) {
        // All frames are empty
        int[] frames = new int[numOfFrames];
        // Index of oldest frame, count of page faults
        int oldest = 0, numPageFaults = 0;

        // No pages loaded yet
        Arrays.fill(frames, -1);

        for (Integer r : rs) {
            // Page fault
            if (isInArray(frames, r) == -1) {
                frames[oldest] = r;  // Copy new page into oldest page frame
                numPageFaults++;  // Record this page fault
                // Frames should be added sequentially, thus the next-oldest frame is the next one in the list
                oldest = (oldest + 1) % (frames.length);
            }
        }

        return numPageFaults;
    }

    // TODO add comments
    private static int LRUReplacement(ArrayList<Integer> rs, int numOfFrames) {
        // All frames are empty
        int[] frames = new int[numOfFrames];
        // Count of the number of page faults
        int numPageFaults = 0;

        // No pages loaded yet
        Arrays.fill(frames, -1);

        for (Integer r : rs) {
            int index = isInArray(frames, r);
            int most;  // most recently used page

            if (index == -1) {  // Page fault
                most = r;
                numPageFaults++;
                index = 0;  // The first (least recently used) element will be removed
            } else
                most = frames[index];  // The page that is most recently moved shall get moved to the end

            // Shifts right side of array left, overwriting the position to be removed. Leaves last position unmodified
            if (frames.length - 1 - index >= 0)
                System.arraycopy(frames, index + 1, frames, index, frames.length - 1 - index);
            // Adds the most recently used page to the end of the list, overwriting previous value
            frames[frames.length - 1] = most;
        }

        return numPageFaults;
    }

    // TODO add comments
    private static int OptimalReplacement(ArrayList<Integer> rs, int numOfFrames) {
        // All frames are empty
        int[] frames = new int[numOfFrames];
        // Count of the number of page faults
        int numPageFaults = 0;

        // No pages loaded yet
        Arrays.fill(frames, -1);

        // Iterate over entire reference string.  Used traditional for loop to access index for forward-search
        for (int pos = 0; pos < rs.size(); pos++) {
            // Page fault
            if (isInArray(frames, rs.get(pos)) == -1) {
                // Create sublist of future references to search
               List<Integer> futureRS = rs.subList(pos, rs.size());

               int furthestFrameIndex = 0;
               int furthestFrameDistance = 0;

               // Check all frames for page referenced furthest in the future
               // If a page is never referenced, it sets distance to -1 to exit search and replace that page in memory
                for (int f = 0; f < frames.length && furthestFrameDistance != -1; f++) {
                    int page = frames[f];
                    int nextReference = futureRS.indexOf(page);

                    // If no future reference occurs, this can be considered the furthest reference
                    if (nextReference == -1) {
                        furthestFrameIndex = f;
                        furthestFrameDistance = -1;
                    }
                    // If a future reference is found, check if it s the furthest
                    else {
                        // If this is the furthest reference found, record it
                        if (nextReference > furthestFrameDistance) {
                            furthestFrameDistance = nextReference;
                            furthestFrameIndex = f;
                        }
                    }
                }

                // Replace the frame with the furthest subsequent reference in the reference string
                frames[furthestFrameIndex] = rs.get(pos);
                // Record page fault
                numPageFaults++;


            }
        }


        return numPageFaults;
    }

    // TODO add comments
    private static int SecondChanceReplacement(ArrayList<Integer> rs, int numOfFrames) {
        // All frames are empty
        int[][] frames = new int[numOfFrames][2];
        // Count of the number of page faults
        int numPageFaults = 0;
        // Pointer to the frame to be considered for replacement
        int replace = 0;

        // No pages loaded yet
        for(int i = 0; i < frames.length; i++) {
            frames[i][0] = -1;
            frames[i][1] = 0;
        }

        for (Integer r : rs) {
            int index = isInArray(frames, r);

            if (index != -1) {
                // If page is found in memory, set r-bit to 1
                frames[index][1] = 1;
            }
            else {  // page is not in memory
                numPageFaults++;  // Record page fault

                // Iterate until a frame is found where r-bit is 0
                while (frames[replace][1] == 1) {
                    frames[replace][1] = 0;
                    replace = (replace + 1) % frames.length;
                }

                // Once a suitable frame is found, replace
                frames[replace][0] = r;
                frames[replace][1] = 0;

                // Increment replace
                replace = (replace + 1) % frames.length;
            }
        }

        return numPageFaults;
    }

    // TODO add comments
    // TODO fix return value and args
    // TODO modify to include other algos
    private static int[] test() {
        // TODO complete
        int[] result = new int[4];  // Stores the reported number of page faults for each algorithm over the same reference string
        // TODO this should generate a full rs, not this tiny mockery
        ArrayList<Integer> rs = new ArrayList<>(Arrays.asList(0, 1, 4, 0, 2, 3, 0, 1, 0, 2, 3, 4, 2, 3));

        // Apply the algorithms to the rs
        result[0] = FIFOReplacement(rs, 4);
        result[1] = LRUReplacement(rs, 4);
        result[2] = OptimalReplacement(rs, 4);
        result[3] = SecondChanceReplacement(rs, 4);

        return result;
    }

    // TODO add comments
    private static int isInArray(int[] frames, int page) {
        for(int i = 0; i < frames.length; i++) {
            if(frames[i] == page) return i;
        }
        return -1;
    }

    // TODO add comments, this one is for SecondChance
    private  static int isInArray(int[][] frames, int page) {
        for (int i = 0; i < frames.length; i++) {
            if(frames[i][0] == page) return i;
        }
        return -1;
    }
}
