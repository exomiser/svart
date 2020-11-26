package org.monarchinitiative.variant.api;

/**
 * The <code>Stranded</code> interface is implemented by classes that are aware of nucleic acid {@link Strand}.
 * <p>
 * When performing conversion to desired {@link Strand} using {@link #withStrand(Strand)}, the {@link Strand}
 * conversion is performed by the following rules:
 *
 * <table>
 *     <tr><th>From</th><th>To</th><th>Supported</th></tr>
 *     <tr><th rowspan="3">POSITIVE}</th><td>NEGATIVE}</td><td>Yes</td></tr>
 *     <tr><td>UNSTRANDED}</td><td>Yes</td></tr>
 *     <tr><td>UNKNOWN}</td><td>Yes</td></tr>
 *     <tr><th rowspan="3">NEGATIVE}</th><td>POSITIVE}</td><td>Yes</td></tr>
 *     <tr><td>UNSTRANDED}</td><td>Yes</td></tr>
 *     <tr><td>UNKNOWN}</td><td>Yes</td></tr>
 * </table>
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Stranded<T> {

    Strand strand();

    /**
     * Attempt to convert the instance from current strand to target <code>strand</code>.
     * <p>
     * The conversion is <em>legal</em> in the following scenarios:
     *
     * <table>
     *     <tr><th>Current</th><th>Target</th><th>Legal</th></tr>
     *     <tr><td rowspan="4" valign="top">POSITIVE</td><td>POSITIVE</td><td>No</td></tr>
     *     <tr><td>NEGATIVE</td><td>Yes</td></tr>
     *     <tr><td>UNSTRANDED</td><td>Yes</td></tr>
     *     <tr><td>UNKNOWN</td><td>Yes</td></tr>
     *     <tr style="border-top: dashed 1px"><td rowspan="4" valign="top">NEGATIVE</td><td>POSITIVE</td><td>Yes</td></tr>
     *     <tr><td>NEGATIVE</td><td>No</td></tr>
     *     <tr><td>UNSTRANDED</td><td>Yes</td></tr>
     *     <tr><td>UNKNOWN</td><td>Yes</td></tr>
     *     <tr style="border-top: dashed 1px"><td rowspan="4" valign="top">UNSTRANDED</td><td>POSITIVE</td><td>No</td></tr>
     *     <tr><td>NEGATIVE</td><td>No</td></tr>
     *     <tr><td>UNSTRANDED</td><td>No</td></tr>
     *     <tr><td>UNKNOWN</td><td>Yes</td></tr>
     *     <tr style="border-top: dashed 1px"><td rowspan="4" valign="top">UNKNOWN</td><td>POSITIVE</td><td>No</td></tr>
     *     <tr><td>NEGATIVE</td><td>No</td></tr>
     *     <tr><td>UNSTRANDED</td><td>No</td></tr>
     *     <tr><td>UNKNOWN</td><td>No</td></tr>
     * </table>
     *
     * @param strand target strand
     * @return an instance converted to the target <code>strand</code> or <code>this</code> if the conversion is
     * not legal
     */
    T withStrand(Strand strand);

    default T toOppositeStrand() {
        return withStrand(strand().opposite());
    }
}
