package smithwaterman;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class SmithWaterman {
  public static void main(String[] args) throws Exception {
    if(args.length != 2) {
      System.err.println("Incorrect number of arguments.");
      System.err.println("Usage: smithwaterman <SequenceFile> <NumberOfThreads>");
      System.exit(1);
    }

    String[] sequences = new String[3];
    try {
      sequences = parseSequenceFile(args[0]);
    } catch (Exception e) {
      System.err.println("Could not parse sequence file: " + args[0]);
      System.exit(1);
    }

    long startTime = System.currentTimeMillis();

    SmithWatermanAligner sw = new SmithWatermanAligner(
      sequences[0],sequences[1],sequences[2], Integer.parseInt(args[1]));

    String[] alignment = sw.align();

    long endTime = System.currentTimeMillis();

    for(int i = 0; i < alignment.length; i++) {
      System.out.println(alignment[i]);
    }

    System.out.println("");
    System.out.println("Completed in " + (endTime - startTime) + "ms");
  }

  private static String[] parseSequenceFile(String filename) throws Exception {
    String[] contents = readFile(filename).split(">");
    String[] sequences = new String[3];
    for(int i = 1; i <= 3; i++) {
      StringBuilder builder = new StringBuilder();
      String[] sequence = contents[i].split("\n");
      for(int j = 1; j < sequence.length; j++) {
        builder.append(sequence[j]);
      }
      sequences[i-1] = builder.toString();
    }
    return sequences;
  }

  private static String readFile(String filename) throws Exception {
    FileInputStream stream = new FileInputStream(new File(filename));
    try {
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      return Charset.defaultCharset().decode(bb).toString();
    } finally {
      stream.close();
    }
  }
}