 #!/bin/bash 
  cat w.spitfire.crowdhood*.64.txt > aligned/spitfire.diskwrite.txt
  cat r.spitfire.crowdhood*.64.txt > aligned/spitfire.diskread.txt
  cat n.spitfire.crowdhood*.64.txt > aligned/spitfire.network.txt
  cat w.hadoop.crowdhood*.64.txt > aligned/hadoop.diskwrite.txt
  cat r.hadoop.crowdhood*.64.txt > aligned/hadoop.diskread.txt
  cat n.hadoop.crowdhood*.64.txt > aligned/hadoop.network.txt
