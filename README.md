# HMM-Rabiner
A Java implementation of the Baum-Welch algorithm based on HMM models with multiple and continuous observations, presented in L. R. Rabiner's paper "A Tutorial on Hidden Markov Models". All the erratas are integrated in this project.

## About this project
This is a university project I'm developing for an exam. Many parts are or can be incorrect (including the formula, code and tests) or missing and are subjected to changes without any notice due to the fact I'm learning how to work with HMMs and classifiers while actively working on it.

## State

### Current version
Version 0.1.15 - Tuesday, April 2, 2018

Made with Eclipse Oxygen.2 and Java 9

### Functionalities
- [x] Implementation of basic structures (Couple, SparseArray, ...) inside utils package
- [x] Implementation of Alpha, Beta, Gamma and Psi formulas
- [x] Logging options for Alpha, Beta, Gamma and Psi formulas
- [x] Implementation of Viterbi algorithm
- [x] Implementation of Continuous data observations
- [x] Implementation of scaled versions of Alpha and Beta
- [x] Implementation of Baum-Welch algorithm
- [x] Logging options for Baum-Welch algorithm
- [ ] FINAL STEP - Implementation of a classifier which uses models created with Baum-Welch **<<< In progress**

### Tests
- [ ] Tests based on learning/pruning set and real data

### Revisions
- [ ] Code cleanup **<<< In progress**
- [ ] Code comments **<<< In progress**
- [ ] Optimizations

## References
* L. R. Rabiner - A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition: 
https://www.cs.ubc.ca/~murphyk/Bayes/rabiner.pdf

* A. Rahimi - An Erratum for "A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition": 
http://alumni.media.mit.edu/~rahimi/rabiner/rabiner-errata/rabiner-errata.html
