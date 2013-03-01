#!/usr/bin/env ruby

require './utils.rb'
require './smithwaterman.rb'

if ARGV.length != 1
  puts "Please specify filename.", "Usage: ./sw 'sequences.in'"
end

sequences = Utils.parseSequenceFile(ARGV[0])
sw = SmithWaterman.new(sequences[0],sequences[1],sequences[2])

t1 = Time.now
puts sw.align
t2 = Time.now
puts "\nCompleted in #{t2 - t1} seconds."