class Utils
  # Print a given matrix (2d array) to the screen
  def self.printMatrix(m)
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

  def self.parseSequenceFile(filename)
    contents = File.read(filename)
    sequences = []
    contents.split('>')[1..-1].each do |seq|
      sequences.push seq.split("\n")[1..-1].join
    end

    return sequences
  end
end