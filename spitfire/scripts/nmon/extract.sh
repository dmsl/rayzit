#!/bin/bash

#hadoop.crowdhood10.r1.64.txt:AAA,time,08:24.07
 grep "READ" hadoop.crowdhood10.*64.txt| tr ":" ","  | tail -n +340 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood10.64.txt
 grep "WRITE" hadoop.crowdhood10.*64.txt| tr ":" ","  | tail -n +340 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood10.64.txt
 grep "NET," hadoop.crowdhood10.*64.txt| tr ":" ","  | tail -n +340 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood10.64.txt
#hadoop.crowdhood11.r1.64.txt:AAA,time,08:29.46
 grep "READ" hadoop.crowdhood11.*64.txt| tr ":" ","  | tail -n +0 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood11.64.txt
 grep "WRITE" hadoop.crowdhood11.*64.txt| tr ":" ","  | tail -n +0 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood11.64.txt
 grep "NET," hadoop.crowdhood11.*64.txt| tr ":" ","  | tail -n +0 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood11.64.txt
#hadoop.crowdhood12.r1.64.txt:AAA,time,08:29.44
 grep "READ" hadoop.crowdhood12.*64.txt| tr ":" ","  | tail -n +2 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood12.64.txt
 grep "WRITE" hadoop.crowdhood12.*64.txt| tr ":" ","  | tail -n +2 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood12.64.txt
 grep "NET," hadoop.crowdhood12.*64.txt| tr ":" ","  | tail -n +2 | cat -n| tr "t" ","  > ../../iops/r1/n.hadoop.crowdhood12.64.txt
#hadoop.crowdhood13.r1.64.txt:AAA,time,08:17.37
 grep "READ" hadoop.crowdhood13.*64.txt| tr ":" ","  | tail -n +730 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood13.64.txt
 grep "WRITE" hadoop.crowdhood13.*64.txt| tr ":" ","  | tail -n +730 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood13.64.txt
 grep "NET," hadoop.crowdhood13.*64.txt| tr ":" ","  | tail -n +730 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood13.64.txt
#hadoop.crowdhood14.r1.64.txt:AAA,time,08:25.06
 grep "READ" hadoop.crowdhood14.*64.txt| tr ":" ","  | tail -n +280 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood14.64.txt
 grep "WRITE" hadoop.crowdhood14.*64.txt| tr ":" ","  | tail -n +280 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood14.64.txt
 grep "NET," hadoop.crowdhood14.*64.txt| tr ":" ","  | tail -n +280 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood14.64.txt
#hadoop.crowdhood4.r1.64.txt:AAA,time,08:26.13
 grep "READ" hadoop.crowdhood4.*64.txt| tr ":" ","  | tail -n +213 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood4.64.txt
 grep "WRITE" hadoop.crowdhood4.*64.txt| tr ":" ","  | tail -n +213 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood4.64.txt
 grep "NET," hadoop.crowdhood4.*64.txt| tr ":" ","  | tail -n +213 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood4.64.txt
#hadoop.crowdhood6.r1.64.txt:AAA,time,08:25.52
 grep "READ" hadoop.crowdhood6.*64.txt| tr ":" ","  | tail -n +194 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood6.64.txt
 grep "WRITE" hadoop.crowdhood6.*64.txt| tr ":" ","  | tail -n +194 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood6.64.txt
 grep "NET," hadoop.crowdhood6.*64.txt| tr ":" ","  | tail -n +194 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood6.64.txt
#hadoop.crowdhood7.r1.64.txt:AAA,time,08:25.51
 grep "READ" hadoop.crowdhood7.*64.txt| tr ":" ","  | tail -n +195 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood7.64.txt
 grep "WRITE" hadoop.crowdhood7.*64.txt| tr ":" ","  | tail -n +195 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood7.64.txt
 grep "NET," hadoop.crowdhood7.*64.txt| tr ":" ","  | tail -n +195 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood7.64.txt
#hadoop.crowdhood8.r1.64.txt:AAA,time,08:25.49
 grep "READ" hadoop.crowdhood8.*64.txt| tr ":" ","  | tail -n +197 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood8.64.txt
 grep "WRITE" hadoop.crowdhood8.*64.txt| tr ":" ","  | tail -n +197 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood8.64.txt
 grep "NET," hadoop.crowdhood8.*64.txt| tr ":" ","  | tail -n +197 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood8.64.txt
#hadoop.crowdhood9.r1.64.txt:AAA,time,08:24.59
 grep "READ" hadoop.crowdhood9.*64.txt| tr ":" ","  | tail -n +292 | cat -n| tr "\t" ","  > ../../iops/r1/r.hadoop.crowdhood9.64.txt
 grep "WRITE" hadoop.crowdhood9.*64.txt| tr ":" ","  | tail -n +292 | cat -n| tr "\t" ","  > ../../iops/r1/w.hadoop.crowdhood9.64.txt
 grep "NET," hadoop.crowdhood9.*64.txt| tr ":" ","  | tail -n +292 | cat -n| tr "\t" ","  > ../../iops/r1/n.hadoop.crowdhood9.64.txt

