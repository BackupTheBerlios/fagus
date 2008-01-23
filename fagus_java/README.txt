Java Classification and Feature Selection Library:
==================================================

This project is split into two distinct parts: a statistical classification
library and a feature selection (and feature extraction) library. Both
libraries assume that features are stored in the LIBSVM format. Here is
an example of this format:

+1 1:0.1 2:-2.3 3:1.5e-4
-1 1:0.3 2:1.0  3:-1.3

The first element is the class label. All other elements are split into a
feature label and a floating point number, separated by a colon. The number 
is the value of this particular feature for this pattern.


Installation:
-------------

You will need Apache Ant (http://ant.apache.org) to build the sources.
Change to the base directory of this distribution and run

$ ant

This will build both libraries and a third utility library, that is used by
both, the classifier and the feature selector.

To be able to run the tools, add the jar files in the directories "dist/" and
"lib/" to your classpath.


Classification Library:
-----------------------

Currently, four classifiers are a available: a Bayes normal classifier, a
k-Nearest Neighbor algorithm, a Parzen Window classifier, and a Support-Vector-
Machine (SVM). The SVM implementation is based on LibSVM [1]. The classifier 
is invoked by

java apps.CrossValidation [-lda n] CLASSIFIER [OPTIONS] INPUT_FILE

where 
  -lda n          : This option uses Linear Discriminant Analysis (LDA) for 
                    preprocessing the input data. The data is reduced to n
                    dimensions. If n is a negative value, the number of 
                    features is reduced by -n dimensions.
                    
and CLASSIFIER [OPTIONS] is one of
  knn [k]         : k is the number of neighbors to consider, if k is not set
                    it is assumed to be 1.
  bayes [-linear] | [-regularize alpha] : 
                    The bayes classifier usually draws quadratic decision
                    boundaries. If the option -linear is set, it will draw
                    linear boundaries. A regularized Bayes classifier uses
                    an interpolation parameter 0.0 <= alpha <= 1.0 that 
                    allows to create some interpolated version of a linear
                    and a quadratic classifier (see [2]).
  parzen [r]      : The Parzen window is a nonparametric classifier. A free
                    radius parameter r might be set (default 1.0).
  svm [c gamma]   : The Support-Vector-Machine (SVM). c and gamma can be set
                    to optimize the machine's and kernel's performance (c 
                    defaults to 1.0 and gamma defaults to the reciprocal of 
                    the feature vector dimension).


Feature Selection Library:
--------------------------

This library provides tools for feature subset selection and feature 
extraction. The subset selection will find the optimal subset for some
criterion function and a pattern set.

java apps.FeatureSelection [CRITERION] -FEATURES INPUT_FILE OUTPUT_FILE

where CRITERION is one of
  bayes         : Use the cross validation procedure of a Bayes classifier
                  as the criterion.
  chernoff      : Use the Chernoff extension to the Fisher criterion. 
  fisher        : The Fisher class separability criterion.
  bhattacharyya : The Bhattacharyya distance. This is only applicable for
                  two-class problems.
and FEATURES is the number of features to be dropped.


The feature extraction tool, on the other hand, will not preserve original 
features, but instead map the feature space to a space with lower dimension.

java apps.FeatureExtraction CRITERION [OPTIONS] INPUT_FILE OUTPUT_FILE

where CRITERION is one of
  fisher               : The Fisher linear discriminant. For a C-classes
                         problem, it will reduce the feature vector to 
                         C-1 elements.
  chernoff [-FEATURES] : The Chernoff extension to LDA. The optional argument
                         -FEATURES sets the number of features to be dropped.
                         If it is omitted, it will again reduce to C-1 
                         features (see [3]).

               
Examples:
---------

Reduce a pattern set by 16 features by dropping the most irrelevant features
for classification.

$ java apps.FeatureSelection -16 in.libsvm out.libsvm

To explicitly apply the Fisher criterion, which is usually faster than the 
Bhattacharyya distance, run

$ java apps.FeatureSelection fisher -16 in.libsvm out.libsvm

If you do not want to preserve the original features, it is usually better to
use feature extraction.

$ java apps.FeatureExtraction chernoff -16 in.libsvm out.libsvm


References:
-----------

[1] C.C. Chang and C.J. Lin 
    "LIBSVM: a library for support vector machines"
    http://www.csie.ntu.edu.tw/~cjlin/libsvm

[2] J. Friedman
    "Regularized Discriminant Analysis"
    in Journal of the American Statistical Association
    vol 84 pp. 165--175
    1989

[3] M. Loog and R.P.W. Duin
    "Linear Dimensionality Reduction via a Heteroscedastic Extension of LDA"
    in IEEE Transactions on Pattern Analysis and Machine Intelligence
    vol. 26:6  pp. 732--739
    IEEE 2004
