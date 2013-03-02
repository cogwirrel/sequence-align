require './blosum.rb'
require './utils.rb'

class SmithWaterman
  def initialize(seqA, seqB, seqC)
    @A = seqA
    @B = seqB
    @C = seqC
    @blosum = Blosum.new

    alen = @A.length + 1
    blen = @B.length + 1
    clen = @C.length + 1

    @F = Array.new(alen) {Array.new(blen) {Array.new(clen)}}
    @T = Array.new(alen) {Array.new(blen) {Array.new(clen)}}
    
    # First plane in A set to 0
    (blen).times do |b|
      (clen).times do |c|
        @F[0][b][c] = 0
      end
    end

    # First plane in B set to 0
    (clen).times do |c|
      (alen).times do |a|
        @F[a][0][c] = 0
      end
    end

    # First plane in C set to 0
    (alen).times do |a|
      (blen).times do |b|
        @F[a][b][0] = 0
      end
    end
  end

  def diagonalise(aLength, bLength, cLength)
    groups = Array.new([aLength, bLength, cLength].reduce(:+) - 2) {[]}
    1.upto(aLength) do |i|
      1.upto(bLength) do |j|
        1.upto(cLength) do |k|
          groups[i+j+k-3].push [i,j,k]
        end
      end
    end
    return groups
  end

  def align
    # Initialise end point
    bestF = 0
    bestT = [0,0,0]

    groups = diagonalise(@A.length, @B.length, @C.length)

    sliceSize = groups.map{ |g| g.length }.max / 8

    groups.each do |group|
      threads = []
      scores = []
      group.each_slice(sliceSize).with_index do |subgroup, gIndex|
        threads << Thread.new {
          subgroup.each_with_index do |coords, g|
            i,j,k = coords
            @F[i][j][k], @T[i][j][k] = maxScore(i,j,k)
            scores.push [@F[i][j][k], @T[i][j][k]]
          end
        }
      end

      threads.each {|t| t.join}
      maxScore = scores.map{|s| s[0]}.each_with_index.max
      if bestF < maxScore[0]
        bestF = maxScore[0]
        bestT = scores[maxScore[1]][1]
      end
    end

    # Initialise our alignments
    i, j, k = bestT
    alignment = ["","",""]

    # Follow T and F backwards and trace optimal allignment
    while @F[i][j][k] != 0 do
      7.times do |n|
        x, ai = n & 1 > 0 ? [i, '-'] : [i - 1, @A[i - 1, 1]]
        y, bj = n & 2 > 0 ? [j, '-'] : [j - 1, @B[j - 1, 1]]
        z, ck = n & 4 > 0 ? [k, '-'] : [k - 1, @C[k - 1, 1]]

        if @T[i][j][k] == [x,y,z]
          alignment[0] = ai + alignment[0]
          alignment[1] = bj + alignment[1]
          alignment[2] = ck + alignment[2]
          break
        end
      end
      i, j, k = @T[i][j][k]
    end

    alignment
  end

  def score(a, b, c)
    (@blosum.score(a,b) + @blosum.score(a,c) + @blosum.score(b,c)) / 3.0
  end

  def maxScore(i, j, k)
    maxF = 0
    maxT = [0,0,0]
    7.times do |n|
      # Manipulate binary to iterate over adjacent cells in f
      x, ai = n & 1 > 0 ? [i, '-'] : [i - 1, @A[i - 1, 1]]
      y, bj = n & 2 > 0 ? [j, '-'] : [j - 1, @B[j - 1, 1]]
      z, ck = n & 4 > 0 ? [k, '-'] : [k - 1, @C[k - 1, 1]]
      
      sc = @F[x][y][z] + score(ai, bj, ck)
      if sc > maxF
        maxF = sc
        maxT = [x,y,z]
      end
    end

    [maxF, maxT]
  end
end