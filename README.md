Code base for the paper: **Babayev, R., & Wiese, L. (2021). Benchmarking Multi-instance Learning for Multivariate Time Series Analysis. In Heterogeneous Data Management, Polystores, and Analytics for Healthcare (pp. 103-120). Springer, Cham.**

Link for the paper: [Springer link](https://link.springer.com/chapter/10.1007/978-3-030-93663-1_9)

The main class is ```Launcher.java``` file. For reproducing the results, you should add this to your run configuration. 
The results are generated in [Physionet dataset](https://physionet.org/content/challenge-2012/1.0.0/). To run the experiments 
please update ```PHYSIONET_DATA_FOLDER``` constant in ```Constants.java``` file with a path to your own directory of Physionet dataset files.
It suffices to have the following structure in your own directory:

![directory structure](https://raw.githubusercontent.com/CavaJ/time-series-analysis/master/directory_structure.PNG)

The experiments will run in one-click execution style both for train/test and cross-validation procedures on all learners. Note that, dependencies 
are based on Maven and available in ```Time Series Analysis.iml``` file. Only one dependency is not Maven based, since, at the time of writing
there was not Maven repo for Weka version 3.7.2 on which we run our experiments. 
Therefore, we provide it in a ```lib``` folder as ```weka-3.7.2.jar``` file.
