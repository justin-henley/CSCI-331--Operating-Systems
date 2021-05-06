/*
Project 4:      Performance of Page Replacement Algorithms
Description:    Compares the performance of page replacement algorithms for fixed numbers of frames: Optimal, FIFO, LRU, Second Chance
Author:         Justin Henley, jahenley@mail.fhsu.edu
Date:           2021-05-06
 */


import java.util.ArrayList;

public class JustinHenleyCSCI331Proj4 {
    // TODO add comments
    public static void main(String[] args) {

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
    // TODO fix return value and args
    private static boolean LRUReplacement() {
        // TODO complete
        return false;
    }

    // TODO add comments
    // TODO fix return value and args
    private static boolean test() {
        // TODO complete
        return false;
    }

    // TODO add comments
    private static int isInArray(int[] frames, int page) {
        for(int i = 0; i < frames.length; i++) {
            if(frames[i] == page) return i;
        }
        return -1;
    }
}
