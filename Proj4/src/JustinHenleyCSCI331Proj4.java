/*
Project 4:      Performance of Page Replacement Algorithms
Description:    Compares the performance of page replacement algorithms for fixed numbers of frames: Optimal, FIFO, LRU, Second Chance
Author:         Justin Henley, jahenley@mail.fhsu.edu
Date:           2021-05-06
 */


public class JustinHenleyCSCI331Proj4 {
    // TODO add comments
    public static void main(String[] args) {

    }

    // TODO add comments
    // TODO fix return value and args
    private static boolean createRS() {
        // TODO complete
        return false;
    }

    // TODO add comments
    // TODO fix return value and args
    private static boolean FIFOReplacement() {
        // TODO complete
        return false;
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
