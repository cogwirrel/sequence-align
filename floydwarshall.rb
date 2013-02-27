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

# Print a given matrix (2d array) to the screen
def printMatrix(m)
  m.each do |row|
    puts row.map{|i| i.to_s.rjust(2)}.inspect.gsub('"', '')
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

  # Create shortest path matrix and initialise paths to infinity
  paths = Array.new(vertices) {Array.new(vertices) {Float::INFINITY}}

  # Store highest index vertex that must be travelled through to take the
  # shortest path
  intermediates = Array.new(vertices) {Array.new(vertices) {nil}}
  
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
          intermediates[i][j] = k
        end
      end
    end
  end

  # Return matrix of shortest path lengths
  return paths, intermediates
end

# Return the intermediate nodes in the shortest path from u to v
def shortestPath(u, v, paths, intermediates)
  mid = intermediates[u][v]
  if paths[u][v] == Float::INFINITY || mid == nil
    return []
  end

  shortestPath(u, mid, paths, intermediates) +
  [mid] +
  shortestPath(mid, v, paths, intermediates)
end



paths, intermediates = floydWarshall GRAPH
puts shortestPath(1, 8, paths, intermediates)