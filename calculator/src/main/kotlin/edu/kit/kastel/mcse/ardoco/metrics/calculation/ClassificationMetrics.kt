package edu.kit.kastel.mcse.ardoco.metrics.calculation

import java.math.BigDecimal
import java.math.MathContext

/**
 * Calculates the precision for the given True Positives (TPs) and False Positives (FPs). If TP+FP=0, then returns 1.0 because there was no wrong
 * classification.
 *
 * @param truePositives  number of TPs
 * @param falsePositives number of FPs
 * @return the Precision; 1.0 iff TP+FP=0
 */
fun calculatePrecision(
    truePositives: Int,
    falsePositives: Int
): Double {
    return if (truePositives + falsePositives == 0) 1.0 else truePositives.toDouble() / (truePositives + falsePositives)
}

/**
 * Calculates the recall for the given True Positives (TPs) and False Negatives (FNs). If TP+NP=0, then returns 1.0 because there was no missing element.
 *
 * @param truePositives  number of TPs
 * @param falseNegatives number of FNs
 * @return the Recall; 1.0 iff TP+NP=0
 */
fun calculateRecall(
    truePositives: Int,
    falseNegatives: Int
): Double {
    return if (truePositives + falseNegatives == 0) 1.0 else truePositives.toDouble() / (truePositives + falseNegatives)
}

/**
 * Calculates the F1-score using the provided precision and recall. If precision+recall=0, returns 0.0.
 *
 * @param precision the precision
 * @param recall    the recall
 * @return the F1-Score; 0.0 iff precision+recall=0
 */
fun calculateF1(
    precision: Double,
    recall: Double
): Double {
    val f1 = 2 * (precision * recall) / (precision + recall)
    return if (f1.isNaN()) 0.0 else f1
}

/**
 * Calculates the accuracy based on the true positives, false positives, false negatives, and true negatives.
 *
 * @return the accuracy
 * @see [Wikipedia: Accuracy and Precision](https://en.wikipedia.org/wiki/Accuracy_and_precision)
 */
fun calculateAccuracy(
    truePositives: Int,
    falsePositives: Int,
    falseNegatives: Int,
    trueNegatives: Int
): Double {
    val numerator = (truePositives + trueNegatives).toDouble()
    val denominator = (truePositives + falsePositives + falseNegatives + trueNegatives).toDouble()
    return numerator / denominator
}

/**
 * Calculates the specificity, also known as selectivity or true negative rate, based on the number of true negatives and false positives.
 *
 * @param trueNegatives  the number of true negatives
 * @param falsePositives the number of false positives
 * @return the specificity
 * @see [Wikipedia: Sensitivity and specificity](https://en.wikipedia.org/wiki/Sensitivity_and_specificity)
 */
fun calculateSpecificity(
    trueNegatives: Int,
    falsePositives: Int
): Double {
    if (trueNegatives + falsePositives == 0) return 1.0
    return trueNegatives.toDouble() / (trueNegatives + falsePositives)
}

/**
 * Returns the Phi Coefficient (also known as mean square contingency coefficient (MCC)) based on the true positives, false positives, false negatives, and
 * true negatives. The return value lies between -1 and +1. -1 show perfect disagreement, +1 shows perfect agreement and 0 indicates no relationship.
 * Therefore, good values should be close to +1.
 *
 * @return the value for Phi Coefficient (or MCC)
 * @see [Wikipedia: Phi coefficient](https://en.wikipedia.org/wiki/Phi_coefficient)
 */
fun calculatePhiCoefficient(
    truePositives: Int,
    falsePositives: Int,
    falseNegatives: Int,
    trueNegatives: Int
): Double {
    val tp = BigDecimal.valueOf(truePositives.toLong())
    val fp = BigDecimal.valueOf(falsePositives.toLong())
    val fn = BigDecimal.valueOf(falseNegatives.toLong())
    val tn = BigDecimal.valueOf(trueNegatives.toLong())

    val num = tp.multiply(tn).subtract((fp.multiply(fn)))

    val a = tp.add(fp)
    val b = tp.add(fn)
    val c = tn.add(fp)
    val d = tn.add(fn)
    if (a == BigDecimal.ZERO || b == BigDecimal.ZERO || c == BigDecimal.ZERO || d == BigDecimal.ZERO) {
        return 0.0
    }

    val productOfSumsInDenominator = a.multiply(b).multiply(c).multiply(d)
    val denominator = productOfSumsInDenominator.sqrt(MathContext.DECIMAL128)

    return num.divide(denominator, MathContext.DECIMAL128).toDouble()
}

/**
 * Calculates the maximum possible value of the phi coefficient given the four values of the confusion matrix (TP, FP, FN, TN).
 *
 * @param truePositives  number of true positives
 * @param falsePositives number of false positives
 * @param falseNegatives number of false negatives
 * @param trueNegatives  number of true negatives
 * @return The maximum possible value of phi.
 * @see [Paper about PhiMax by Ferguson
 * @see [Paper about Phi/PhiMax by Davenport et al.
](https://journals.sagepub.com/doi/abs/10.1177/001316449105100403)](https://link.springer.com/article/10.1007/BF02288588) */
fun calculatePhiCoefficientMax(
    truePositives: Int,
    falsePositives: Int,
    falseNegatives: Int,
    trueNegatives: Int
): Double {
    val tp = BigDecimal.valueOf(truePositives.toLong())
    val fp = BigDecimal.valueOf(falsePositives.toLong())
    val fn = BigDecimal.valueOf(falseNegatives.toLong())
    val tn = BigDecimal.valueOf(trueNegatives.toLong())

    val test = fn.add(tp) >= fp.add(tp)
    val nominator = fp.add(tn).multiply(tp.add(fp)).sqrt(MathContext.DECIMAL128)
    val denominator = fn.add(tn).multiply(tp.add(fn)).sqrt(MathContext.DECIMAL128)

    return if (test) {
        // standard case
        if (denominator == BigDecimal.ZERO) {
            return 0.0
        }
        nominator.divide(denominator, MathContext.DECIMAL128).toDouble()
    } else {
        // if test is not true, you have to swap nominator and denominator as then you have to mirror the confusion matrix (,i.e., swap TP and TN)
        if (nominator == BigDecimal.ZERO) {
            return 0.0
        }
        denominator.divide(nominator, MathContext.DECIMAL128).toDouble()
    }
}

/**
 * Calculates the normalized phi correlation coefficient value that is phi divided by its maximum possible value.
 *
 * @param truePositives  number of true positives
 * @param falsePositives number of false positives
 * @param falseNegatives number of false negatives
 * @param trueNegatives  number of true negatives
 * @return The value of Phi/PhiMax
 * @see [Paper about Phi/PhiMax](https://journals.sagepub.com/doi/abs/10.1177/001316449105100403)
 */
fun calculatePhiOverPhiMax(
    truePositives: Int,
    falsePositives: Int,
    falseNegatives: Int,
    trueNegatives: Int
): Double {
    val phi = calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives)
    val phiMax = calculatePhiCoefficientMax(truePositives, falsePositives, falseNegatives, trueNegatives)
    if (phiMax == 0.0) {
        return 0.0
    }
    return phi / phiMax
}
