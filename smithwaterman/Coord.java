package smithwaterman;

public class Coord {
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