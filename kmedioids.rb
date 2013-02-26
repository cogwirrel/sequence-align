# Given set of genes
GENES = [[1.1,  1.9, 0],
         [2.5,  3.8, 0],
         [2.45, 2.4, 0],
         [1.8,  4.8, 0],
         [2.3,  3.7, 0],
         [4.8,  0.8, 0],
         [1.9,  1.1, 0],
         [1.2,  0.6, 0],
         [0.7,  0.7, 0],
         [4.9,  0.4, 0]]

# Print a given matrix (2d array) to the screen
def printMatrix(m)
  m.each do |row|
    puts row.map{|i| i.to_s.rjust(7)}.inspect.gsub('"', '')
  end
end

# Print the genes matrix nicely
def printGenes(genes)
  clusters = []
  gs = []
  gs.push(["Gene", "Exp 1", "Exp 2", "Cluster"])
  genes.each_with_index do |gene, index|
    if(!clusters.include?(gene[2]))
      clusters.push(gene[2])
    end
    gs.push(["Gene #{index}", gene[0], gene[1], "C#{clusters.index(gene[2]) + 1}"])
  end
  printMatrix gs
end

# Return euclidean distance between 2 points
def dist(x1, y1, x2, y2)
  return Math.sqrt((x1 - x2).abs ** 2 + (y1 - y2).abs ** 2)
end

# Return euclidean distance between 2 genes
def geneDist(gene1, gene2)
  return dist(gene1[0], gene1[1], gene2[0], gene2[1])
end

# Return closest medioid and distance to closest medioid
# for given gene and medioids
def closest(gene, medioids)
  closest = medioids[0]
  closestDist = geneDist(gene, GENES[medioids[0]])
  medioids.each do |med|
    d = geneDist(gene, GENES[med])
    if(d < closestDist)
      closestDist = d
      closest = med
    end
  end
  return [closest, closestDist]
end

# Return the genes in the given cluster
def getGenesInCluster(c)
  genesInCluster = []
  GENES.each do |gene|
    if(gene[2] == c)
      genesInCluster.push(gene)
    end
  end
end

# Calculate the average distance between each gene in genes
# and their closest medioid in medioids
def cost(medioids, genes)
  totalDist = 0
  genes.each do |gene|
    totalDist += closest(gene, medioids)[1]
  end
  return totalDist / genes.length
end

# Iterate kmedioids a given number of times with specified
# initial medioids
def kmedioids(medioids, iterations)
  iterations.times do |iteration|
    # ASSIGNMENT STEP
    GENES.each do |gene|
      gene[2] = closest(gene, medioids)[0]
    end

    bestmeds = medioids.dup

    # UPDATE STEP
    medioids.each_with_index do |med, med_index|
      bestcost = cost(medioids, getGenesInCluster(med))
      GENES.each_with_index do |gene, gene_index|
        # Only look at genes in the cluster not including current
        if(gene[2] == med)
          # Gene is in this cluster
          meds = medioids.dup
          meds[med_index] = gene_index
          c = cost(meds, getGenesInCluster(med))
          if(c <= bestcost)
            bestcost = c
            bestmeds[med_index] = gene_index
          end
        end
      end
    end

    medioids = bestmeds

    puts "Iteration #{iteration + 1}"
    printGenes GENES
    puts '------------------------------------'
  end
end

# Question 1.2.b
kmedioids([0,1,2],3)