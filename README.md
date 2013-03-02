3-Sequence Parallel Smith Waterman
==================================

This is a multi-threaded implementation of the Smith Waterman algorithm, used to
compute the optimal alignment of 3 protein sequences.

Compilation
-----------
Make sure that your current directory is the same as this README file - ie. you
can see the directory named 'smithwaterman'.

Compile the java code using:
`javac smithwaterman/*.java`

This should produce some .class files in the 'smithwaterman' directory.

Running
-------
Sequences should be stored in FASTA format in a file, separated by new lines.
See 'sequences.in' for an example.

Run using:

`java smithwaterman/SmithWaterman <SequenceFile> <NumberOfThreads>`