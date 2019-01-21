# Continuous HMM Solver
A Java implementation of the Baum-Welch algorithm based on HMM models with multiple and continuous observations, presented in L. R. Rabiner's paper "A Tutorial on Hidden Markov Models". All the erratas are integrated in this project.

![Continuous HMM Solver Previww](https://raw.githubusercontent.com/subwave07/HMM-Rabiner/masterv1/README.md_images/logo.jpg)

## About this project
This is a university project I developed for an exam of the Machine Learning university course held by Professor Attilio Giordana
of the University of the Eastern Piedmont.

## Features
*	HMM training with Baum-Welch algorithm (with a fixed number of steps or dependant on the pruning set)
*	Possibile state sequences recognition with Viterbi's algorithm
*	Scaled formulas implemented (consult Rabiner's tutorial for more informations)
*	Support for multiple observations sequences
*	Support for continuous obsevations
*	A simple, multi-model classifier
*	Support for K-Fold Cross Validation
*	Dataset rescaling tool

## Download
You can grab the latest release of this project [clicking here](https://github.com/subwave07/HMM-Rabiner/releases)!

This package contains the following items:
* Continuous HMM Solver
* A dataset regarding a heat pump, working in two modes: heating/cooling
* Few sample Hidden Markov Models, ready to be trained with the above dataset in order to recognise if an observations sequence
has been produced by the heat pump while it was in heating or cooling mode

## Source Code
You can download this project (made with Eclipse 18-09 and JDK 1.8.0) by [clicking here](https://github.com/subwave07/HMM-Rabiner/archive/masterv1.zip) or using the command

```git clone https://github.com/subwave07/HMM-Rabiner```

## Bugs
Please, report any error or problem in the [Issues section](https://github.com/subwave07/HMM-Rabiner/issues) of this project!

## License
This project is distributed under the [MIT License](https://github.com/subwave07/HMM-Rabiner/blob/masterv1/LICENSE).
Some libraries included in this project are published under other licenses, available [here](https://github.com/subwave07/HMM-Rabiner/tree/masterv1/lib/doc).

## References
* L. R. Rabiner - A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition: 
https://www.cs.ubc.ca/~murphyk/Bayes/rabiner.pdf

* A. Rahimi - An Erratum for "A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition": 
http://alumni.media.mit.edu/~rahimi/rabiner/rabiner-errata/rabiner-errata.html

## Acknowledgements
I'd like to thank PhD [Dario Vogogna](https://github.com/akyrey) for his [Hidden Markov Model Solver](https://github.com/akyrey/HiddenMarkovModel_Solver)
implementation for the discrete domain obsevations, due to the fact it was very useful to understand some mistakes I was doing with my project.
