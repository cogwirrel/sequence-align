package smithwaterman;

// Class to hold a coordinate
public class Coord {
  // Keep track of static id variable to ensure coordinates are assigned a
  // unique identifier. This is used to dynamically allocate processing of
  // coordinates to different threads
  private static int currentId = 0;

  public int x, y, z, id;

  public Coord(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = currentId;
    currentId++;
  }
}