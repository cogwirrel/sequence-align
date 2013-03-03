package smithwaterman;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class SmithWaterman {
  // Smith Waterman entry point
  public static void main(String[] args) throws Exception {
    if(args.length < 1 || args.length > 2 || args[0].equals("--help")) {
      System.err.println("Usage: java smithwaterman/SmithWaterman " +
                         "<SequenceFile> <NumberOfThreads>\n");
      System.err.println("<SequenceFile> must contain 3 sequences in FASTA " +
                         "format, separated by new lines.\n");
      System.err.println("<NumberOfThreads> is an optional parameter. This " +
                         "specifies the maximum number of threads that can " +
                         "be used to compute the optimal alignment. " +
                         "The default value is 1.");
      System.exit(1);
    }

    // Read 3 sequences from input file
    String[] sequences = new String[3];
    try {
      sequences = parseSequenceFile(args[0]);
    } catch (Exception e) {
      System.err.println("Unable to parse sequence file: '" + args[0] + "'");
      System.exit(1);
    }

    // Parse the number of threads provided
    int numThreads = 1;
    if(args.length > 1) {
      try {
        int userNumThreads = Integer.parseInt(args[1]);
        if(userNumThreads > 0) {
          numThreads = userNumThreads;
        } else {
          System.err.println("Number of threads must be an integer greater than 0.");
          System.err.println("Continuing using 1 thread.");
        }
      } catch (Exception e) {
        System.err.println("Number of threads must be an integer greater than 0.");
        System.err.println("Continuing using 1 thread.");
      }
    }

    // Initialise Smith Waterman Aligner with the 3 input sequences and
    // maximum number of threads to use
    SmithWatermanAligner sw = new SmithWatermanAligner(
      sequences[0], sequences[1], sequences[2], numThreads);

    // Perform the Smith Waterman algorithm to align the 3 sequences
    String[] alignment = sw.align();

    // Print the alignment to screen
    for(int i = 0; i < alignment.length; i++) {
      System.out.println(alignment[i]);
    }
  }

  // Parse a sequence file containing 3 FASTA formatted sequences
  private static String[] parseSequenceFile(String filename) throws Exception {
    // A > indicates a new sequence
    String[] contents = readFile(filename).split(">");
    String[] sequences = new String[3];

    // Loop through 3 FASTA sequences
    for(int i = 1; i <= 3; i++) {
      StringBuilder builder = new StringBuilder();
      String[] sequence = contents[i].split("\n");
      // Ignore first line - only interested in the sequence itself
      for(int j = 1; j < sequence.length; j++) {
        builder.append(sequence[j]);
      }
      sequences[i-1] = builder.toString();
    }
    return sequences;
  }

  // Read a file into a string
  private static String readFile(String filename) throws Exception {
    FileInputStream stream = new FileInputStream(new File(filename));
    try {
      FileChannel fileChannel = stream.getChannel();
      MappedByteBuffer buffer =
        fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
      return Charset.defaultCharset().decode(buffer).toString();
    } finally {
      stream.close();
    }
  }
}