package smithwaterman;

public class SmithWatermanAligner {
  private String A,B,C;
  private Blosum blosum;

  private double[][][] F;
  private int[][][][] T;

  public SmithWatermanAligner(String a, String b, String c) {
    this.A = a;
    this.B = b;
    this.C = c;
    this.blosum = new Blosum();

    int aLength = a.length + 1;
    int bLength = b.length + 1;
    int cLength = c.length + 1;

    F = new double[aLength][bLength][cLength];
    T = new int[aLength][bLength][cLength][3];

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
    int[] bestT = {0,0,0};

    ArrayList<ArrayList<Integer[]>> groups = diagonalise(A.length, B.length, C.length);

    for(ArrayList<Integer[]> group : groups) {
      // Each group can be computed in parallel
      ArrayList<Thread> threads = new ArrayList<Thread>();
      for(Integer[] coords : group) {
        // Create a new thread
        Thread t = new Thread(new Runnable() {
          public void run() {
            int i = coords[0];
            int j = coords[1];
            int k = coords[2];
            Pair<double,Integer[]> result = bestScore(i,j,k);
            F[i][j][k] = result.l;
            T[i][j][k] = result.r;
          }
        });
        threads.add(t);
        t.start();
      }

      // Wait for all threads to complete
      for(Thread t : threads) {
        t.join();
      }

      // Find our max score so far
      for(Integer[] coords : group) {
        int i = coords[0];
        int j = coords[1];
        int k = coords[2];
        if(F[i][j][k] > bestF) {
          bestF = F[i][j][k];
          bestT = T[i][j][k];
        }
      }
    }

    // Initialise alignment
    StringBuilder[] alignmentBuilder = new StringBuilder[3];
    int i = bestT[0];
    int j = bestT[1];
    int k = bestT[2];

    while(F[i][j][k] != 0) {
      for(int n = 0; n < 7; n++) {
        Pair<int,char> x = n & 1 > 0 ? new Pair<int,char>(i, '-') : new Pair<int, char>(i - 1, A.charAt(i - 1));
        Pair<int,char> y = n & 2 > 0 ? new Pair<int,char>(j, '-') : new Pair<int, char>(j - 1, B.charAt(j - 1));
        Pair<int,char> z = n & 4 > 0 ? new Pair<int,char>(k, '-') : new Pair<int, char>(k - 1, C.charAt(k - 1));

        if(T[i][j][k][0] == x.l &&
           T[i][j][k][1] == y.l &&
           T[i][j][k][2] == z.l) {
          alignmentBuilder[0].Insert(0, x.r);
          alignmentBuilder[1].Insert(0, y.r);
          alignmentBuilder[2].Insert(0, z.r);
        }
      }

      i = T[i][j][k][0];
      j = T[i][j][k][1];
      k = T[i][j][k][2];
    }

    String[] alignment = new String[3];
    alignment[0] = alignmentBuilder[0].toString();
    alignment[1] = alignmentBuilder[1].toString();
    alignment[2] = alignmentBuilder[2].toString();
    return alignment;
  }

  private ArrayList<ArrayList<Integer[]>> diagonalise(
    int aLength, int bLength, int cLength) {

    ArrayList<ArrayList<Integer[]>> groups = new ArrayList<ArrayList<Integer[]>>();
    int numGroups = aLength + bLength + cLength - 2;
    for(int i = 0; i < numGroups; i++) {
      groups.add(new ArrayList<Integer[]>);
    }

    for(int i = 1; i <= aLength; i++) {
      for(int j = 1; j <= bLength; j++) {
        for(int k = 1; k <= cLength; k++) {
          Integer[] coords = new Integer[];
          coords[0] = i;
          coords[1] = j;
          coords[2] = k;
          groups.get(i + j + k - 3).add(coords);
        }
      }
    }
    return groups;
  }

  private double score(char a, char b, char c) {
    return (blosum.score(a, b) + blosum.score(a, c) + blosum.score(b, c)) / 3.0;
  }

  private Pair<double, Integer[]> bestScore(int i, int j, int k) {
    double bestF = 0;
    Integer[] bestT = {0,0,0};
    for(int n = 0; n < 7; n++) {
      Pair<int,char> x = n & 1 > 0 ? new Pair<int,char>(i, '-') : new Pair<int, char>(i - 1, A.charAt(i - 1));
      Pair<int,char> y = n & 2 > 0 ? new Pair<int,char>(j, '-') : new Pair<int, char>(j - 1, B.charAt(j - 1));
      Pair<int,char> z = n & 4 > 0 ? new Pair<int,char>(k, '-') : new Pair<int, char>(k - 1, C.charAt(k - 1));

      sc = F[x.l][y.l][z.l] + score(x.r, y.r, z.r);

      if(sc > bestSc) {
        bestSc = sc;
        bestT = {x.l, y.l, z.l};
      }
    }
    return new Pair<double, Integer[]>(bestSc, bestT);
  }

}