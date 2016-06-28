# Rayzit / Spitfire

Spitfire is an open source, MIT licensed, distributed
algorithm that provides a scalable and high-performance AkNN
processing framework to our award-winning geo-social network
named Rayzit.

## Motivation
A wide spectrum of Internet-scale mobile applications,
ranging from social networking, gaming and entertainment
to emergency response and crisis management, all
require efficient and scalable All k Nearest Neighbor (AkNN)
computations over millions of moving objects every few seconds
to be operational. 

## Overview
Spitfire deploys a fast load-balanced partitioning along with an efficient replication-set selection, to provide fast main-memory computations of the exact AkNN results in a batch-oriented manner. The pruning efficiency of the Spitfire algorithm plays a pivotal role in reducing communication and response time up to an order of magnitude, compared to three state-of-the-art distributed AkNN algorithms executed in distributed main-memory.

Spitfire is based on a IEEE TKDE'16 journal paper and a respective IEEE ICDE'16 poster paper. Its source code can be downloaded at https://github.com/dmsl/rayzit/spitfire. The source code of the Rayzit service (iOS, Windows, Web, Android) can be downloaded at https://github.com/dmsl/rayzit.

Enjoy Spitfire!

The Spitfire Team

Copyright (c) 2016, Data Management Systems Lab (DMSL), Department of Computer Science
University of Cyprus.

All rights reserved.

## GPL Open Source Licence

Spitfire: A distributed main-memory algorithm for AkNN query processing
Copyright (c) 2016 Data Management Systems Laboratory, University of Cyprus

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
## Components 

Short description of the contents included in this release.

### Spitfire Algorithm
The Source code for the Spitfire Algorithm. 

### H-NJ, H-BNLJ, H-BRJ, PGBJ Algorithms
Please request the source code directly from: W. Lu, Y. Shen, S. Chen and B.C. Ooi. “Efficient processing of k nearest neighbor joins using mapreduce”. In Proceedings of the 38th international conference on Very Large Data Bases (PVLDB’12). VLDB Endowment 5, 1016–1027, 2012.

### Datasets
The Datsets used for the experimental evaluation.

### Automation
The automation scripts used to launch the experiments

Authors: Constantinos Costa and George Chatzimilioudis 
(costa.c@cs.ucy.ac.cy; gchatzim@cs.ucy.ac.cy)


