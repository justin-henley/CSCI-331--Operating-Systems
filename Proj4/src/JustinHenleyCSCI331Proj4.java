/*
Project 4:      Performance of Page Replacement Algorithms
Description:    Compares the performance of page replacement algorithms for fixed numbers of frames: Optimal, FIFO, LRU, Second Chance
Author:         Justin Henley, jahenley@mail.fhsu.edu
Date:           2021-05-06
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

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
            // TODO expand for reporting other algorithms
            System.out.println("The number of page faults using the FIFO replacement algorithm: ");
            System.out.println(FIFOReplacement(rs, numOfFrames));
            System.out.println("The number of page faults using the LRU replacement algorithm: ");
            System.out.println(LRUReplacement(rs, numOfFrames));

            // Prompt for continuation
            System.out.println("Do you want to run another test? Y/N");
            choice = input.nextLine().toUpperCase().charAt(0);
        } while(choice == 'Y');
    }

    // TODO add comments
    // TODO fix return value and args
    private static ArrayList<Integer> createRS(int sizeOfVM, int length, int sizeOfLocus, int rateOfMotion, double prob) {
        // Create a new array list to store the reference string (RS)
        ArrayList<Integer> result = new ArrayList<Integer>();
        int start = 0;
        int n;  // A page number in a reference string, declared out here for persistence across while iterations

        // Repeat until desired size is reached
        while(result.size() < length) {
            // Add size of locus random number in it
            for (int i = 0; i < rateOfMotion; i++) {
                n = (int) (Math.random() * sizeOfLocus + start);
                result.add(n);
            }
            // Generate a random number between 0 and 1
            if (Math.random() < prob)
                start = (int) Math.random() * sizeOfVM;
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

        for(int i = 0; i < frames.length; i++) {
            // No page loaded
            frames[i] = -1;
        }

        for(int i = 0; i < rs.size(); i++) {
            // Page fault
            if(isInArray(frames, rs.get(i)) == -1) {
                frames[oldest] = rs.get(i);  // Copy new page into oldest page frame
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
        // Index of first and a count of the number of page faults
        int first = 0, numPageFaults = 0;

        for (int i = 0; i < numOfFrames; i++) {
            // No pages loaded yet
            frames[i] = -1;
        }

        for(int i = 0; i < rs.size(); i++) {
            int index = isInArray(frames, rs.get(i));
            int most;  // most recently used page

            if(index == -1) {  // Page fault
                most = rs.get(i);
                numPageFaults++;
                index = 0;  // The first (least recently used) element will be removed
            }
            else
                most = frames[index];  // The page that is most recently moved shall get moved to the end

            // Shifts right side of array left, overwriting the position to be removed. Leaves last position unmodified
            for(int j = index; j < frames.length - 1; j++) {
                frames[j] = frames[j + 1];
            }
            // Adds the most recently used page to the end of the list, overwriting previous value
            frames[frames.length - 1] = most;
        }

        return numPageFaults;
    }

    // TODO add comments
    // TODO fix return value and args
    // TODO modify to include other algos
    private static int[] test() {
        // TODO complete
        int[] result = new int[2];  // Stores the reported number of page faults for each algorithm over the same reference string
        // TODO this should generate a full rs, not this tiny mockery
        ArrayList<Integer> rs = new ArrayList<Integer>(Arrays.asList(0,1,4,0,2,3,0,1,0,2,3,4,2,3));

        // Apply the algorithms to the rs
        result[0] = FIFOReplacement(rs, 4);
        result[1] = LRUReplacement(rs, 4);

        return result;
    }

    // TODO add comments
    private static int isInArray(int[] frames, int page) {
        for(int i = 0; i < frames.length; i++) {
            if(frames[i] == page) return i;
        }
        return -1;
    }
}
