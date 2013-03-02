package smithwaterman;

import java.util.*;
import java.lang.*;

public class SmithWatermanAligner {
  private static final int NUM_THREADS = 2;

  private String A,B,C;
  private Blosum blosum;

  private double[][][] F;
  private Coord[][][] T;

  public SmithWatermanAligner(String a, String b, String c) {
    this.A = a;
    this.B = b;
    this.C = c;
    this.blosum = new Blosum();

    int aLength = a.length() + 1;
    int bLength = b.length() + 1;
    int cLength = c.length() + 1;

    F = new double[aLength][bLength][cLength];
    T = new Coord[aLength][bLength][cLength];

    for(int i = 0; i < aLength; i++) {
      for(int j = 0; j < bLength; j++) {
        F[i][j][0] = 0;
      }
    }

    for(int j = 0; j < bLength; j++) {
      for(int k = 0; k < cLength; k++) {
        F[0][j][k] = 0;
      }
    }

    for(int k = 0; k < cLength; k++) {
      for(int i = 0; i < aLength; i++) {
        F[i][0][k] = 0;
      }
    }
  }

  public String[] align() {
    // Initialise endpoint
    double bestF = 0;
    Coord bestT = new Coord(0,0,0);

    ArrayList<ArrayList<Coord>> groups = diagonalise(A.length(), B.length(), C.length());

    for(final ArrayList<Coord> group : groups) {
      // Each group can be computed in parallel
      ArrayList<Thread> threads = new ArrayList<Thread>();
      for(int t = 1; t <= NUM_THREADS; t++) {      
        final int threadId = t;
        // Create a new thread
        Thread thread = new Thread(new Runnable() {
          public void run() {
            for(Coord coord : group) {
              if(coord.id % threadId == 0) {
                int i = coord.x;
                int j = coord.y;
                int k = coord.z;
                Pair<Double,Coord> result = bestScore(i,j,k);
                F[i][j][k] = result.l;
                T[i][j][k] = result.r;
              }
            }
          }
        });
        threads.add(thread);
        thread.start();
      }


      // Wait for all threads to complete
      for(Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          System.err.println("Thread interrupted, exiting.");
          System.exit(1);
        }
      }

      // Find our max score so far
      for(Coord coord : group) {
        int i = coord.x;
        int j = coord.y;
        int k = coord.z;
        if(F[i][j][k] > bestF) {
          bestF = F[i][j][k];
          bestT = T[i][j][k];
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

    while(F[i][j][k] != 0) {
      for(int n = 0; n < 7; n++) {
        Pair<Integer,Character> x = (n & 1) > 0 ? new Pair<Integer,Character>(i, '-') : new Pair<Integer, Character>(i - 1, A.charAt(i - 1));
        Pair<Integer,Character> y = (n & 2) > 0 ? new Pair<Integer,Character>(j, '-') : new Pair<Integer, Character>(j - 1, B.charAt(j - 1));
        Pair<Integer,Character> z = (n & 4) > 0 ? new Pair<Integer,Character>(k, '-') : new Pair<Integer, Character>(k - 1, C.charAt(k - 1));

        if(T[i][j][k].x == x.l &&
           T[i][j][k].y == y.l &&
           T[i][j][k].z == z.l) {
          alignment[0] = alignment[0] + x.r;
          alignment[1] = alignment[1] + y.r;
          alignment[2] = alignment[2] + z.r;
        }
      }

      Coord coord = T[i][j][k];

      i = coord.x;
      j = coord.y;
      k = coord.z;
    }

    return alignment;
  }

  private ArrayList<ArrayList<Coord>> diagonalise(
    int aLength, int bLength, int cLength) {

    ArrayList<ArrayList<Coord>> groups = new ArrayList<ArrayList<Coord>>();
    int numGroups = aLength + bLength + cLength - 2;
    for(int i = 0; i < numGroups; i++) {
      groups.add(new ArrayList<Coord>());
    }

    for(int i = 1; i <= aLength; i++) {
      for(int j = 1; j <= bLength; j++) {
        for(int k = 1; k <= cLength; k++) {
          Coord coord = new Coord(i,j,k);
          groups.get(i + j + k - 3).add(coord);
        }
      }
    }
    return groups;
  }

  private double score(char a, char b, char c) {
    return (blosum.score(a, b) + blosum.score(a, c) + blosum.score(b, c)) / 3.0;
  }

  private Pair<Double, Coord> bestScore(int i, int j, int k) {
    double bestF = 0;
    Coord bestT = new Coord(0,0,0);
    for(int n = 0; n < 7; n++) {
      Pair<Integer,Character> x = (n & 1) > 0 ? new Pair<Integer,Character>(i, '-') : new Pair<Integer, Character>(i - 1, A.charAt(i - 1));
      Pair<Integer,Character> y = (n & 2) > 0 ? new Pair<Integer,Character>(j, '-') : new Pair<Integer, Character>(j - 1, B.charAt(j - 1));
      Pair<Integer,Character> z = (n & 4) > 0 ? new Pair<Integer,Character>(k, '-') : new Pair<Integer, Character>(k - 1, C.charAt(k - 1));

      double sc = F[x.l][y.l][z.l] + score(x.r, y.r, z.r);

      if(sc > bestF) {
        bestF = sc;
        bestT.x = x.l;
        bestT.y = y.l;
        bestT.z = z.l;
      }
    }
    return new Pair<Double, Coord>(bestF, bestT);
  }

}