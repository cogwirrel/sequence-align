require 'rational'

# Each column represents a vertex, and each row represents
# the vertices this vertex is connected to.
# Eg GRAPH[1] == [0,2] means vertex 1 is connected to vertex 0 and 2
GRAPH = [[1,2],
         [0,2],
         [0,1,3,4],
         [2,5],
         [2,5],
         [3,4,6,7,8],
         [5,7,8],
         [5,6,8],
         [5,6,7]]

GRAPHY = [[1,2,5],
          [0,2,4],
          [0,1,3,4,6],
          [2,5],
          [0,2,5],
          [3,4,6,7,8,0],
          [5,7,8,2],
          [5,6,8],
          [5,6,7]]

GRIDGRAPH = [[1,2],
             [0,3],
             [0,3,4],
             [1,2,5],
             [2,5],
             [3,5]]

# Return all automorphism orbit pairs
def autoMorphismOrbitPairs(graph)
  pairs = []
  # Find pairs of vertices that can be swapped to produce isomorphism of graph
  graph.each_with_index do |v1Neighbours, v1|
    graph.each_with_index do |v2Neighbours, v2|
      # Only care about different vertices
      if v1 != v2
        # Vertices can be swapped if they have the same set of neighbours
        if v1Neighbours.sort == v2Neighbours.sort ||
           (v1Neighbours + [v1]).sort == (v2Neighbours + [v2]).sort
           pairs.push([v1, v2].sort)
        end
      end
    end
  end
  return pairs.uniq
end

def autoMorphismOrbits(graph)
  orbits = []
  pairs = autoMorphismOrbitPairs(graph)
  pairs.each_with_index do |pair, i|
    orbits.push []
    pairs.each do |pair2|
      if not (pair & pair2).empty?
        orbits[i] += pair + pair2
      end
    end
    orbits[i].uniq!.sort!
  end
  return orbits.uniq
end

# Print a given matrix (2d array) to the screen
def printMatrix(m)
  w = Array.new(m[0].length) {0}
  m.each do |row|
    row.each_with_index do |col, i|
      if w[i] < col.to_s.length
        w[i] = col.to_s.length
      end
    end
  end
  m.each do |row|
    puts row.each_with_index.map{|col, i| col.to_s.rjust(w[i])}.inspect.gsub('"', '')
  end
end

# Return all edges in a given graph as pairs of vertices
def getEdges(graph)
  edges = []
  graph.each_with_index do |neighbours, vertex|
    neighbours.each do |neighbour|
      edges.push([neighbour, vertex])
    end
  end
  return edges
end


# Perform the floyd-warshall algorithm
def floydWarshall(graph)
  # Number of vertices
  vertices = graph.length

  # Create shortest path matrix and initialise paths to maximum path
  # length - the number of vertices in the graph
  paths = Array.new(vertices) {Array.new(vertices) {vertices}}

  # Store highest index vertex that must be travelled through to take the
  # shortest path
  intermediates = Array.new(vertices) {Array.new(vertices) {[nil]}}
  
  # Shortest path from node to itself is 0
  vertices.times {|v| paths[v][v] = 0}

  # Path to adjacent vertices costs 1
  getEdges(graph).each {|e| paths[e[0]][e[1]] = 1}

  # Incrementally improve upon shortest path estimate
  vertices.times do |k|
    vertices.times do |i|
      vertices.times do |j|
        if paths[i][k] + paths[k][j] < paths[i][j]
          paths[i][j] = paths[i][k] + paths[k][j]
          intermediates[i][j].clear()
          intermediates[i][j].push(k)
        elsif paths[i][k] + paths[k][j] == paths[i][j] && k != j && k != i
          intermediates[i][j].push(k)
        end
      end
    end
  end

  # Return matrix of shortest path lengths
  return paths, intermediates
end

# Return the intermediate nodes in the shortest path from u to v
def shortestPaths(u, v, paths, intermediates)
  allShortestPaths = []
  intermediates[u][v].each do |mid|
    if mid == nil
      # Vertices are directly connected
      allShortestPaths.push [u,v]
    else
      # Recursively find shortest paths to and from middle vertex
      uToMids = shortestPaths(u, mid, paths, intermediates)
      midToVs = shortestPaths(mid, v, paths, intermediates)
      uToMids.each do |uToMid|
        midToVs.each do |midToV|
          # Last vertex in path repeated in midToV
          uToMid.pop
          # Add all combinations of shortest paths to middle vertex
          allShortestPaths.push(uToMid + midToV)
        end
      end
    end
  end

  return allShortestPaths
end

# Calculate betweenness centrality of given vertex in graph
def betweenness(v, graph)
  paths, intermediates = floydWarshall graph

  # Number of paths that pass through v
  passV = 0

  # Number of paths that don't pass through v
  noPassV = 0

  vertices = graph.length
  vertices.times do |s|
    vertices.times do |t|
      if s != v && v != t && s != t
        shortestPaths(s, t, paths, intermediates).each do |path|
          if path.include?(v) then passV += 1 else noPassV += 1 end
        end
      end
    end
  end

  return Rational(passV) / Rational(passV + noPassV)
end

# Calculate the betweenness centrality of all vertices in graph
def allBetweenness(graph)
  betweennesses = []
  graph.length.times {|v| betweennesses.push betweenness v, graph}
  return betweennesses
end

# Question 2 Part 2 c)
def q2_2_c
  printMatrix floydWarshall(GRAPH)[0]
end

# Question 2 Part 2 d)
def q2_2_d
  betweennesses = [["Vertex", "Betweenness Centrality"]]
  allBetweenness(GRAPH).each_with_index {|bc, v| betweennesses.push [v+1,bc]}
  printMatrix betweennesses
end

q2_2_c
q2_2_d

puts autoMorphismOrbitPairs(GRIDGRAPH).inspect
puts autoMorphismOrbits(GRIDGRAPH).inspect