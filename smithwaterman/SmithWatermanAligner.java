package smithwaterman;

import java.util.*;
import java.lang.*;

public class SmithWatermanAligner {
  // THREAD_THRESHOLD is the number of groups to process at which it is
  // beneficial to spawn a new thread
  private static final int THREAD_THRESHOLD = 20000;

  // Maximum number of threads created
  private final int MAX_THREADS;

  // The three sequences
  private String A,B,C;

  // Instance of Blosum class to score alignments
  private Blosum blosum;

  // F is three dimensional matrix of scores
  private double[][][] F;

  // T is three dimensional backtrace matrix
  private Coord[][][] T;

  // Constructor takes three sequences as strings, and the maximum number of
  // threads that may be spawned
  public SmithWatermanAligner(String a, String b, String c, int threads) {
    this.MAX_THREADS = threads;
    this.A = a;
    this.B = b;
    this.C = c;
    this.blosum = new Blosum();

    int aLength = a.length() + 1;
    int bLength = b.length() + 1;
    int cLength = c.length() + 1;

    // Construct the matrices
    F = new double[aLength][bLength][cLength];
    T = new Coord[aLength][bLength][cLength];

    // Initialise first plane for A to 0
    for(int j = 0; j < bLength; j++) {
      for(int k = 0; k < cLength; k++) {
        F[0][j][k] = 0;
      }
    }

    // Initialise first plane for B to 0
    for(int k = 0; k < cLength; k++) {
      for(int i = 0; i < aLength; i++) {
        F[i][0][k] = 0;
      }
    }

    // Initialise first plane for C to 0
    for(int i = 0; i < aLength; i++) {
      for(int j = 0; j < bLength; j++) {
        F[i][j][0] = 0;
      }
    }
  }

  // Find optimal alignment of sequences A, B and C
  public String[] align() {
    // Initialise endpoint
    double bestF = 0;
    Coord bestT = new Coord(0,0,0);

    // All cells in a diagonal plane of a 3D matrix do not depend on one another
    // so we split the 3D array into coordinate groups, each group corresponding
    // to a diagonal plane. Every cell in a group can be processed in
    // parallel as they do not depend on one another.
    ArrayList<ArrayList<Coord>> groups =
      diagonalise(A.length(), B.length(), C.length());

    // Loop through each group serially
    for(final ArrayList<Coord> group : groups) {
      // Each cell in the group can be processed in parallel

      // Use the group size and thread threshold to spawn beneficial number of
      // threads - the multithreading benefit must outweigh the overhead of
      // spawning a new thread
      int numThreads =
        Math.min(group.size() / THREAD_THRESHOLD + 1, MAX_THREADS);

      // Initialise array of threads
      Thread[] threads = new Thread[numThreads];

      for(int t = 1; t <= numThreads; t++) {
        // Give the thread an ID, used to split work over multiple threads
        final int threadId = t;
        // Create a new thread
        Thread thread = new Thread(new Runnable() {
          public void run() {
            // Loop through every coordinate in the group
            for(Coord c : group) {
              // Only process thread's share of group
              if(c.id % threadId == 0) {
                // Obtain maximum score and backtrace coordinate for that score
                Pair<Double,Coord> result = bestScore(c.x, c.y, c.z);
                F[c.x][c.y][c.z] = result.l;
                T[c.x][c.y][c.z] = result.r;
              }
            }
          }
        });
        threads[t-1] = thread;
        thread.start();
      }


      // Wait for all threads to complete
      for(int t = 0; t < numThreads; t++) {
        try {
          threads[t].join();
        } catch (InterruptedException e) {
          System.err.println("Thread interrupted, exiting.");
          System.exit(1);
        }
      }

      // Find our maximum score so far
      for(Coord c : group) {
        if(F[c.x][c.y][c.z] > bestF) {
          bestF = F[c.x][c.y][c.z];
          bestT = T[c.x][c.y][c.z];
        }
      }
    }

    // Initialise alignment
    String[] alignment = new String[3];
    alignment[0] = "";
    alignment[1] = "";
    alignment[2] = "";
    int i = bestT.x;
    int j = bestT.y;
    int k = bestT.z;

    // Trace back through F and T to store best alignment
    while(F[i][j][k] != 0) {
      for(int n = 0; n < 7; n++) {
        // Manipulate binary counting to loop through all combinations of
        // predecessor cells
        Pair<Integer,Character> x = (n & 1) > 0 ?
          new Pair<Integer,Character>(i, '-') :
          new Pair<Integer, Character>(i - 1, A.charAt(i - 1));
        Pair<Integer,Character> y = (n & 2) > 0 ?
          new Pair<Integer,Character>(j, '-') :
          new Pair<Integer, Character>(j - 1, B.charAt(j - 1));
        Pair<Integer,Character> z = (n & 4) > 0 ?
          new Pair<Integer,Character>(k, '-') :
          new Pair<Integer, Character>(k - 1, C.charAt(k - 1));

        // If this was the cell from which we arrived
        if(T[i][j][k].x == x.l &&
           T[i][j][k].y == y.l &&
           T[i][j][k].z == z.l) {
          // Prepend the character to our alignment as we are tracing the
          // alignment backwards
          alignment[0] = x.r + alignment[0];
          alignment[1] = y.r + alignment[1];
          alignment[2] = z.r + alignment[2];
        }
      }

      // Set the next cell to check
      Coord c = T[i][j][k];
      i = c.x;
      j = c.y;
      k = c.z;
    }

    return alignment;
  }

  // Returns groups of cell coordinates for which their value can be computed
  // in parallel
  private ArrayList<ArrayList<Coord>> diagonalise(
    int aLength, int bLength, int cLength) {

    // Initialise list of groups
    ArrayList<ArrayList<Coord>> groups = new ArrayList<ArrayList<Coord>>();

    // The number of diagonal planes in the 3D matrix
    int numGroups = aLength + bLength + cLength - 2;

    // Initialise each group to empty list of coordinates
    for(int i = 0; i < numGroups; i++) {
      groups.add(new ArrayList<Coord>());
    }

    // Split the 3D matrix into diagonal planes
    for(int i = 1; i <= aLength; i++) {
      for(int j = 1; j <= bLength; j++) {
        for(int k = 1; k <= cLength; k++) {
          Coord coord = new Coord(i,j,k);
          // -3 as we are indexing from 1 in 3 dimensions
          groups.get(i + j + k - 3).add(coord);
        }
      }
    }
    return groups;
  }

  // Compute blosum score of 3 amino acids
  private double score(char a, char b, char c) {
    // Return the average blosum score of each pairing
    return (blosum.score(a, b) + blosum.score(a, c) + blosum.score(b, c)) / 3.0;
  }

  // Compute the best cell to have come from based on blosum score
  private Pair<Double, Coord> bestScore(int i, int j, int k) {
    // Initialise best score to 0
    double bestF = 0;
    Coord bestT = new Coord(0,0,0);
    for(int n = 0; n < 7; n++) {
      // Manipulate binary counting to loop through all combinations of
      // predecessor cells.
      Pair<Integer,Character> x = (n & 1) > 0 ?
        new Pair<Integer,Character>(i, '-') :
        new Pair<Integer, Character>(i - 1, A.charAt(i - 1));
      Pair<Integer,Character> y = (n & 2) > 0 ?
        new Pair<Integer,Character>(j, '-') :
        new Pair<Integer, Character>(j - 1, B.charAt(j - 1));
      Pair<Integer,Character> z = (n & 4) > 0 ?
        new Pair<Integer,Character>(k, '-') :
        new Pair<Integer, Character>(k - 1, C.charAt(k - 1));

      // Score of arriving from predecessor cell
      double sc = F[x.l][y.l][z.l] + score(x.r, y.r, z.r);

      if(sc > bestF) {
        bestF = sc;
        bestT.x = x.l;
        bestT.y = y.l;
        bestT.z = z.l;
      }
    }
    // Return both the best score and the predecessor cell
    return new Pair<Double, Coord>(bestF, bestT);
  }

}