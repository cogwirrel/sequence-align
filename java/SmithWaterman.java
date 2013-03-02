package smithwaterman;

public class SmithWaterman {
  public static void main(String[] args) {
    if(args.length != 1) {
      System.out.println("Please specify sequence filename.");
    }

    String[] sequences = parseSequenceFile(args[0]);

    long startTime = System.currentTimeMillis();

    SmithWatermanAligner sw = new SmithWatermanAligner(sequences[0],sequences[1],sequences[2]);
    
    long endTime = System.currentTimeMillis();

    String[] alignment = sw.align();
    for(int i = 0; i < alignment.length; i++) {
      System.out.println(alignment[i]);
    }

    System.out.println("Completed in " + (endTime - startTime) + "ms");
  }

  private static String[] parseSequenceFile(String filename) {
    String[] contents = readFile(filename).split(">");
    String[] sequences = new String[3];
    for(int i = 0; i < 3; i++) {
      StringBuilder builder = new StringBuilder();
      String[] sequence = contents[i].split("\n");
      for(int j = 1; j < sequence.length; j++) {
        builder.append(sequence[j]);
      }
      sequences[i] = builder.toString();
    }
    return sequences;
  }

  private static String readFile(String filename) throws IOException {
    FileInputStream stream = new FileInputStream(new File(path));
    try {
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      return Charset.defaultCharset().decode(bb).toString();
    } finally {
      stream.close();
    }
  }
}