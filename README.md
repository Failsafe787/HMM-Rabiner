# HMM-Rabiner
A Java implementation of the Baum-Welch algorithm based on HMM models with multiple and continuous observations, presented in L. R. Rabiner's paper "A Tutorial on Hidden Markov Models". All the erratas are integrated in this project.

## About this project
This is a university project I'm developing for an exam. Many parts are incorrect (both code and tests) or missing and are subjected to changes without any notice due to the fact I'm still learning most of the subject and I'm actively working on it.

## State

### Current version
Version 0.1.7 - Tuesday, February 21, 2018

Made with Eclipse Oxygen.2 and Java 9

### Functionalities
- [x] Implementation of basic structures (Couple, SparseArray, ...) inside utils package
- [x] Implementation of Alpha, Beta, Gamma and Psi formulas
- [x] Debug options for Alpha, Beta, Gamma and Psi formulas
- [ ] Implementation of Viterbi algorithm
- [x] Implementation of Continuous data observations
- [x] Implementation of scaled versions of Alpha and Beta
- [x] Implementation of Baum-Welch algorithm
- [ ] Debug options for Baum-Welch algorithm
- [ ] Implementation of the Main class

### Tests
- [x] Tests for basic structures
- [ ] Tests for Alpha, Beta, Gamma and Psi formulas
- [ ] Tests for Viterbi algoritm
- [x] Tests for continuous observations
- [ ] Tests for scaled versions of Alpha and Beta
- [ ] Tests for Baum-Welch algorithm
- [ ] Tests based on learning/pruning set and real data

### Revisions
- [ ] Code cleanup
- [ ] Optimizations

## References
* L. R. Rabiner - A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition: 
https://www.cs.ubc.ca/~murphyk/Bayes/rabiner.pdf

* A. Rahimi - An Erratum for "A Tutorial on Hidden Markov Models and Selected Applications in Speech Recognition": 
http://alumni.media.mit.edu/~rahimi/rabiner/rabiner-errata/rabiner-errata.html
