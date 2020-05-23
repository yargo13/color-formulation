package com.example.myapplication;

public class Cromossome {
    static protected int NUM_PIGMENTS;
    static protected boolean[] invalid_pigments;
    public int[] weights;

    static void set_NUM_PIGMENTS(int NUM_PIGMENTS){
        Cromossome.NUM_PIGMENTS = NUM_PIGMENTS;
    }

    static void set_invalid_pigments(boolean[] invalid_pigments){
        Cromossome.invalid_pigments = invalid_pigments;
    }

    public Cromossome(){
        this.weights = new int[NUM_PIGMENTS];
    }

    public void initialize_weights(){
        for(int i=0; i<NUM_PIGMENTS; i++){
            if (invalid_pigments[i]) continue;
            // We will use 9 bits for each cromossome, so that the value for each pigment is from
            // 0.00000 to 0.00511 in steps of 0.00001
            this.weights[i] = (int) (Math.random()*512);
        }
    }

    static void crossover(Cromossome c1, Cromossome c2, Cromossome new_c1, Cromossome new_c2){
        int point_crossover = (int) (Math.random()*(9*NUM_PIGMENTS-1));
        int pigment_crossover = point_crossover/9;
        // Bit that the crossover is going to happen, counting from right to left
        int bit_pigment = 9-(point_crossover%9+1);
        System.arraycopy(c1.weights, 0, new_c1.weights, 0, pigment_crossover);
        System.arraycopy(c1.weights, pigment_crossover+1, new_c2.weights, pigment_crossover+1, NUM_PIGMENTS-pigment_crossover-1);
        System.arraycopy(c2.weights, pigment_crossover+1, new_c1.weights, pigment_crossover+1, NUM_PIGMENTS-pigment_crossover-1);
        System.arraycopy(c2.weights, 0, new_c2.weights, 0, pigment_crossover);
        // Do bitwise operations to get weight at the point of
        int left_c1 = c1.weights[pigment_crossover] >> bit_pigment;
        int right_c1 = c1.weights[pigment_crossover] & ((1 << bit_pigment) - 1);
        int left_c2 = c2.weights[pigment_crossover] >> bit_pigment;
        int right_c2 = c2.weights[pigment_crossover] & ((1 << bit_pigment) - 1);
        new_c1.weights[pigment_crossover] = (left_c1 << bit_pigment) | (right_c2);
        new_c2.weights[pigment_crossover] = (left_c2 << bit_pigment) | (right_c1);

    }

}
