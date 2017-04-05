## Sparse Stress

`sparse stress` is a graph drawing tool to approximate the full stress model. Via term aggregation`sparse stress` can compute layouts for very large graphs far beyond the the limits of the full stress.


## Build

### Requirements
`sparse_stress` requires Java 1.7 and gradle (wrapper included).

### Compiling

1. run the build script from within the directory

        $ ./gradlew build

## Input format

The input format is an edge list, where the first line contains the number of vertices in the graph. For weighted graphs an edge is encoded by `v1,v2,w` with `v1` and `v2` being the vertices connected by the edge and `w` specifying the desired distance. An undirected edge is a tupele `v1,v2`. For example the (un)weighted complete graph with three vertices has the following format:

|graph | (unweighted) | (weighted) |
|-|--------------|-------------|
|n |3            |     3|
|edge |0,1          |     0,1,40.2544|
|edge |0,2          |     0,2,100.120|
|edge |1,2          |     1,2,23.1|

**The user is responsible that the graph is undirected, simple, and connected and that the vertex ids are integers in the range [0,n).**


## Execution
`sparse_stress` is a command line tool. To display the help message run:

        ./sparse_stress --help

        Usage: sparse_stress -p P -s SAMPLER -f FACTOR -i ITER -/+b -/+w                     [-r SEED] [-m MDS] [-c] INPUT
               sparse_stress -p P -s kmeans  -f FACTOR -i ITER -/+b -/+w --features FEATURES [-r SEED] [-m MDS] [-c] INPUT

        The INPUT graph must be specified according the input format
        The OUTPUT is the layout on standard out; optimally rescaled stress value and running time in seconds on standard error

        Options:
          -p P                - sample P pivots
          -s SAMPLER          - sampler (random, maxmin, kmeans)
          -f FACTOR           - scale the layout by constant factor
          -i ITER             - set the maximum iterations
          -b                  - break condition: -b stops after ITER iterations; +b stops once sparse stress converges, but
                                after at most ITER iterations
          -w                  - Indicates whether the INPUT is an unweighted graph (-w) or weighted (+w)
          --features FEATURES - number of entries sampled from the shortest-path distance matrix
          -r SEED             - SEED value to be used by the sampler (default 0)
          -m MDS              - number of pivots used by PivotMDS (default 200)
          -c                  - calculates the stress in the final layout
          -h, --help, -?      - print help message


* `-p` Specifies the number of pivots used. A multi-source shortest-path algorithm is run to create the spare stress model. As more pivots are used the quality of the layout improves at the cost of more computation time and memory. If `P` is larger than the number of nodes in the graph, n, it set to to n.
* `-s` Specifies the routine used to sample the pivots (**random,maxmin,kmeans**). Typically **kmeans** sampling results in the best layouts w.r.t. stress at the costs of an increase of the computation time.
* `-f` Specifies a constant scaling factor that is multiplied to the coordinate of each vertex in the final layout.
* `-i` Specifies the maximum number of iterations for the iterative layout algorithm. A larger number of iterations tends to improve the quality of the layout at the cost of additional time.
* `-b` Specifies whether the sparse stress algorithm should stop before `ITER` iterations, given the change in the stress value is below 0.01% (`+b`). Otherwise, the algorithm will stop after exactly `ITER` iterations (`-b`).  
* `-w` Specifies whether the input graph is unweighted (`-w`) or weighted (`+w`).
* `--features` Specifies the number of entries of the shortest-path distance matrix, sampled via **maxmin**, used by the **kmeans** sampler. A larger number of `FEATURES` tends to improve the quality of the layout at the costs of additional running time. If `FEATURES` is larger than `P` it is set to `P`.
* `-r` Specifies the SEED value of the random number generator used by the different sampler. ** The default is set to 0**.
* `-m` Specifies the number of pivots used by to compute the initial layout via PivotMDS. A higher number of pivots results in increased running time, yet might improve the quality of the final drawing. **The default is set to 200**.
* `-c` Calculates the optimally rescaled stress value of the final layout. **Note that this computation requires n^2 space**.


Lastly, `sparse_stress` returns the layout on standard out; optimally rescaled stress value and running time in seconds on standard error.

## Reference

This tool implements the work in [A Sparse Stress Model](https://arxiv.org/abs/1608.08909), which was written in collaboration with [Ulrik Brandes](http://algo.uni-konstanz.de/members/brandes/), and Mirza Klimenta and got published in [GD'16](http://algo.math.ntua.gr/~gd2016/). Our publication used `-p 50|100|200 -s kmeans -f 1 -i 200 +b -m 200 -k 50|100|200` implying that option kmeans refers to **k-means sp**. **These parameters are not necessarily the best in other settings.** Please note that the implementation does not match one by one the one used in the publication.
