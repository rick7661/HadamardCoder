import java.util.Arrays;

/**
 * Hadamard Encoder and Decoder
 * @author RickWu
 */
public class Hadamard
{
    // Class variables
    private static final int[][] H1 = {{1}};
    private static final int[][] H2 = {{1, 1}, {1, -1}};
    
    //Instance variables
    private int[][] hadCode;
    private int k; // order of hadamard matrix
    
    //constructor
    public Hadamard(int order)
    {
        this.k = order;
        
        // Initialize a Hadamard encoder/decoder which
        // holds a Hadamard code HC(k) of order k
        hadCode = hadCod2(k);
    }
    
    /*
     * Fastly generating Hadamard matrix
     * Algorithm from:
     * http://introcs.cs.princeton.edu/java/14array/Hadamard.java.html
     */
    // Upper half of a hadamard code is a Hadamard matrix of order k : H(k)
    private int[][] hadMx2(int k)
    {
        // compute matrix size N
        int N = new Double(Math.pow(2, k)).intValue();
        int[][] had = new int[N][N];

        // initialize Hadamard matrix of order N
        had[0][0] = 1;
        for (int n = 1; n < N; n += n)
        {
            for (int i = 0; i < n; i++)
            {
                for (int j = 0; j < n; j++)
                {
                    had[i+n][j]   =  had[i][j];
                    had[i][j+n]   =  had[i][j];
                    had[i+n][j+n] =  (had[i][j] + 1) % 2; //1 -> 0 || 0 -> 1
                }
            }
        }
        
        return had;
    }
    
    /*
     * Bottom half of a hadamard code
     * is the negation of a Hadamard matrix: -H(k)
     */
    private int[][] hadMxNeg2(int k)
    {
        int N = new Double(Math.pow(2, k)).intValue();
        int[][] had = new int[N][N];

        // initialize Hadamard matrix of order N
        had[0][0] = 0;
        for (int n = 1; n < N; n += n)
        {
            for (int i = 0; i < n; i++)
            {
                for (int j = 0; j < n; j++)
                {
                    had[i+n][j]   =  had[i][j];
                    had[i][j+n]   =  had[i][j];
                    had[i+n][j+n] =  (had[i][j] + 1) % 2; //1 -> 0 || 0 -> 1
                }
            }
        }
        return had;
    }
    
    // Let H(k) be theHadamard code of order k, then
    // hadamard code HC(k) consists of H(k) as upper half and -H(k) as bottom half
    private int[][] hadCod2(int k)
    {
        int[][] h = hadMx2(k);
        int[][] hNeg = hadMxNeg2(k);
        
        return vConcat(h, hNeg);
    }
    
    
    /*
     * DEPRECATED due to performance issue
     * Kept for reference
     * Hadamard Code
     * Take a Hadamard Matrix and change all -1s to 0s
     */
    private int[][] hadamardCode(int k)
    {
        int[][] h = hadamardMatrix(k);
        
        int[][] hNeg = new int[h.length][h[0].length];
        for (int i = 0; i < h.length; ++i)
        {
            for (int j = 0; j < h[0].length; ++j)
            {   
                hNeg[i][j]  = (h[i][j] + 2) % 3;    //flip: 1 -> 0, -1 -> 1
                h[i][j]     = (-h[i][j] + 2) % 3;   //flip: 1 -> 1, -1 -> 0
            }
        }
        int[][] c = vConcat(h, hNeg);
        return c;
    }
    
    /*
     * DEPRECATED due to performance issue
     * Kept for reference
     * Hadamard Matrix:
     * H(1) =   [1]
     * H(2) =   [1, 1]
     *          [1,-1]
     * H(2^k) = [H(2^(k-1)), H(2^(k-1))]
     *          [H(2^(k-1)),-H(2^(k-1))]
     *        = H(2) crossProduct H(2^(k-1))
     */
    private int[][] hadamardMatrix(int k)
    {
        if (k <= 0) return H1;
        else if (k == 1) return H2;
        else
        {
            return kroneckerProduct(H2, hadamardMatrix(k-1));
        }
    }
    
    //Encode a string
    public String encodeStr(String str)
    {
        //str must be dividable by the input length = (k + 1)
        int inputLength = k + 1;
        if(str.length() % inputLength != 0) return null;
        
        //The regex (?<=\\G.{8})
        //matches an empty string that has the last match (\\G) followed by
        //eight characters .{8} before it ((?<= ))
        //this split the string to 8-characters each
        String regex = "(?<=\\G.{" + inputLength + "})";
        String[] words = str.split(regex);
        
        //Encode each (k + 1)-bit input word into (2^k)-bit codeword
        String[] codewords = new String[words.length];
        for(int i = 0; i < codewords.length; ++i)
        {
            codewords[i] = encode(words[i]);
        }
        
        //Concatenate all codewords into a string and return it
        return concatStrings(codewords);
    }
    
    //Decode a codeword string
    public String decodeStr(String str)
    {
        int cwLength = new Double(Math.pow(2, k)).intValue();
        
        //str must be dividable by the cw length = (2^k)
        if(str.length() % cwLength != 0) return null;
        
        //The regex (?<=\\G.{2^k})
        //matches an empty string that has the last match (\\G) followed by
        //2^k characters .{2^k} before it ((?<= ))
        //this split the string to (2^k)-characters each
        String regex = "(?<=\\G.{" + cwLength +  "})"; //"(?<=\\G.{128})"
        String[] codewords = str.split(regex);
        
        //Decode each (2^k)-bit input word into (k + 1)-bit codeword
        String[] words = new String[codewords.length];
        for(int i = 0; i < codewords.length; ++i)
        {
            words[i] = decode(codewords[i]);
            
            //If word[i].length() < (k + 1)-bit
            //Pad 0s to the word, make it (k + 1)-bit
            int diff = (k + 1) - words[i].length();
            if(diff > 0)
            {
                StringBuilder builder = new StringBuilder();
                for(int j = 0; j < diff; ++j)
                {
                    builder.append('0');
                }
                builder.append(words[i]);
                
                words[i] = builder.toString();
            }
        }
        
        //Concatenate all codewords into a string and return it
        return concatStrings(words);
    }
    
    /*
     * Hadamard encode
     */
    public String encode(String word)
    {
        //if word length > (k + 1) bit it cannot be encoded
        if (word.length() > (k + 1)) return null;
        
        //convert word from binary string to integer
        int index = Integer.parseInt(word, 2);
        
        //get codeword at given row index
        int[] cw = hadCode[index];
        
        String codeword = "";
        
        int i = 0;
        while(i < cw.length)
        {
            codeword += cw[i++];
        }
        
        return codeword;
    }
    
    /*
     * Hadamard decode
     */
    public String decode(String codeword)
    {
        //if codeword length != 2^k bit then it cannot be decoded
        if(codeword.length() != new Double(Math.pow(2, k)).intValue()) return null;
        
        int[] cw = new int[codeword.length()];
        for(int i = 0; i < codeword.length(); ++i)
        {
            cw[i] = Integer.parseInt(codeword.substring(i, i+1));
        }
        
        //compute the weight of each index to original message
        //index = (row number) of Hadamard code
        int[] weight = weight(cw, hadCode);
        
        //get the position of the greatest weight
        int idx = 0;
        for (int i = 0; i < weight.length; ++i) idx = (Math.abs(weight[i]) > Math.abs(weight[idx])? i : idx);
        if (weight[idx] < 0) idx += weight.length;
        
        String word = Integer.toBinaryString(idx);
        return word;
    }
    
    /*
     * Outer product of matrix A and B,
     * known as Kronecker product or cross product
     */
    private int[][] kroneckerProduct(int[][] a, int[][] b)
    {   
        int m = a.length;
        int n = a[0].length;
        int p = b.length;
        int q = b[0].length;
        
        int[][] rslt = null;
        for(int ai = 0; ai < m; ++ai)
        {
            int[][] rowI = null;
            
            for(int aj = 0; aj < n; ++aj)
            {
                int[][] aijB = scalarMult(b, a[ai][aj]);
                
                if(rowI == null) rowI = aijB;
                else rowI = hConcat(rowI, aijB);
            }
            
            if (rslt == null) rslt = rowI;
            else rslt = vConcat(rslt, rowI);
        }
        return rslt;
    }
    
    /*
     * inner product: v * hT = [a0, a1, ... , a(n-1)];
     * v: vector = codeword
     * hT = transpose of hadamard code
     * [a0, a1, ... , a(n-1)]: original code with each "aX" representing 1 bit
     */
    private int[] weight(int[] v, int[][] h)
    {
        int[] r = new int[h[0].length];
        
        // 0 -> -1 for v
        int i = 0;
        while(i < v.length) v[i] = (v[i++] == 0? -1 : 1);
        
        //dot product of v and h
        for(int hj = 0; hj < h[0].length; ++hj)
        {
            for(int vj = 0; vj < v.length; ++vj)
            {
                //h is hadamard code, to get hT simply swap row/column index
                //so instead of using hT[vj][hj]
                //h[hj][vj] is used because: h[i][j] = hT[j][i]
                r[hj] += (v[vj] * h[hj][vj]);
            }
        }
        
        /*
        //normal way using hT
        int[][] hT = transpose(h);
        for(int hj = 0; hj < hT.length; ++hj)
        {
            for(int vj = 0; vj < v.length; ++vj)
            {
                r[hj] += (v[vj] * hT[vj][hj]);
            }
        }
        */
        return r;
    }
    
    /*
     * Scalar multiplication of matrix A
     * int version
     */
    private static int[][] scalarMult(int[][] a, int b)
    {
        int[][] rslt = new int[a.length][a[0].length];
        for(int i = 0; i < a.length; ++i)
        {
            for(int j = 0; j < a[0].length; ++j)
            {
                rslt[i][j] = a[i][j] * b;
            }
        }
        return rslt;
    }
    
    /*
     * Dotted representations of a matrix
     */
    private String dotRepStr(int[][] a)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < a.length; ++i)
        {
            for(int j = 0; j < a[i].length; j++)
            {
                if (a[i][j] > 0)
                {
                    builder.append('+');
                    builder.append('|');
                }
                else
                {
                    builder.append(' ');
                    builder.append('|');
                }
            }
            builder.append('\n');
        }
        return builder.toString();
    }
    
    //String representation of an array
    private String intArrayToStr(int[][] a)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < a.length; ++i)
        {
            for(int j = 0; j < a[i].length; j++)
            {
                builder.append(a[i][j]);
                builder.append(' ');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
    
    @Override
    public String toString()
    {
        return intArrayToStr(hadCode);
    }
    
    public String toStringDots()
    {
        return dotRepStr(hadCode);
    }
    
    /*
     * Matrix transposition - not an effecient way
     * Can be improved with fourior transform alg
     */
    private int[][] transpose(int[][] a)
    {
        int[][] b = new int[a[0].length][a.length];
        
        for(int i = 0; i < a.length; ++i)
        {
            for(int j = 0; j < a[0].length; ++j)
            {
                b[j][i] = a[i][j];
            }
        }
        return b;
    }
    
    /*
     * Concatenate array vertically
     */
    private int[][] vConcat(int[][] a, int[][] b)
    {
        if(a[0].length <= b[0].length) //b has to have equal or more columns than a
        {
            int[][] rslt = new int[a.length + b.length][a[0].length];
            
            for(int j = 0; j < a[0].length; ++j)
            {
                int ai = 0;
                int bi = 0;
                
                while(ai < a.length)
                {
                    rslt[ai][j] = a[ai++][j];
                }

                while(bi < b.length)
                {
                    rslt[ai + bi][j] = b[bi++][j];
                }
            }
            return rslt;
        }
        else
        {
            return null;
        }
    }
    
    /*
     * Concatenate array horizontally
     */
    private int[][] hConcat(int[][] a, int[][] b)
    {
        if(a.length <= b.length) //b has to have equal or more rows than a
        {
            int[][] rslt = new int[a.length][a[0].length + b[0].length];
            
            for(int i = 0; i < a.length; ++i)
            {
                int aj = 0;
                int bj = 0;
                
                while(aj < a[0].length)
                {
                    rslt[i][aj] = a[i][aj++];
                }
                
                while(bj < b[0].length)
                {
                    rslt[i][aj + bj] = b[i][bj++];
                }
            }
            return rslt;
        }
        else
        {
            return null;
        }
    }
    
    //concatenate an array of strings to one string
    //using StringBuilder for better performance
    private String concatStrings(String[] strings)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; ++i)
        {
            builder.append(strings[i]);
        }
        return builder.toString();
    }
    
    /*
     * Main method. For testing only
     */
    public static void main(String[] args)
    {   
        int k = 7;
        Hadamard had = new Hadamard(k);
        String msg = "11110000";
        
        //Encode
        String cw = had.encode(msg);
        System.out.println("Codeword: " + cw);
        
        //Decode
        String word = had.decode(cw);
        System.out.println("Message: " + msg);
    }
}