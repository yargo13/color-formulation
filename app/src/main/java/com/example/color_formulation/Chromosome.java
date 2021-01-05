package com.example.color_formulation;

import java.util.Arrays;

public class Chromosome {
    static protected int NUM_PIGMENTS;
    static protected boolean[] invalid_pigments;
    static public String[] pigment_names = {
            "Light skin/Pele clara", "Red/Vermelho", "Yellow/Amarelo", "Green/Verde", "Brown/Marrom", "Blue/Azul", "Blood/Sangue",
            "White/Branco", "Black/Preto", "Orange flocking/Flocagem Laranja", "Skin flocking/Flocagem Pele", "Black flocking/Flocagem Preto",
            "Gold flocking/Flocagem Ouro", "Red flocking/Flocagem Vermelho", "Purple flocking/Flocagem Roxo", "Green flocking/Flocagem Verde",
            "White flocking/Flocagem Branco", "Light brown flocking/Flocagem Marrom Claro", "Dark brown flocking//Flocagem Marrom Escuro",
            "Havana flocking/Flocagem Havana", "Only elastomer/Apenas Silicone"
    };
    final static public int NUM_BITS = 11;
    public int[] weights;

    static void set_NUM_PIGMENTS(int NUM_PIGMENTS){
        Chromosome.NUM_PIGMENTS = NUM_PIGMENTS;
    }

    static void set_invalid_pigments(boolean[] invalid_pigments){
        Chromosome.invalid_pigments = invalid_pigments.clone();
    }

    public Chromosome(){
        this.weights = new int[NUM_PIGMENTS];
    }

    public void initialize_weights(){
        int sum = 0;
        Arrays.fill(weights, 0);
        for(int i=0; i<NUM_PIGMENTS; i++){
            if (invalid_pigments[i]) continue;
            // We will use 11 bits for each chromosome, so that the value for each pigment is from
            // 0 to 2047 => 0.00000 to 0.02047 in steps of 0.00001 considering weight percentage
            this.weights[i] = (int) (Math.random()*Math.pow(2, NUM_BITS));
            sum += this.weights[i];
        }
        if (sum >= 3000) {
            for (int i=0; i<NUM_PIGMENTS; i++){
                this.weights[i] = (this.weights[i]* (int) (Math.random()*3000))/sum;
            }
        }

    }

    static void crossover(Chromosome c1, Chromosome c2, Chromosome new_c1, Chromosome new_c2){
        int point_crossover = (int) (Math.random()*(NUM_BITS*NUM_PIGMENTS-1));
        int pigment = point_crossover/(NUM_BITS);
        // Bit that the crossover is going to happen, counting from right to left
        int bit_pigment = NUM_BITS-(point_crossover%NUM_BITS + 1);
        System.arraycopy(c1.weights, 0, new_c1.weights, 0, pigment);
        System.arraycopy(c1.weights, pigment+1, new_c2.weights, pigment+1, NUM_PIGMENTS-pigment-1);
        System.arraycopy(c2.weights, pigment+1, new_c1.weights, pigment+1, NUM_PIGMENTS-pigment-1);
        System.arraycopy(c2.weights, 0, new_c2.weights, 0, pigment);
        // Do bitwise operations to get weight at the point of
        int left_c1 = c1.weights[pigment] >> bit_pigment;
        int right_c1 = c1.weights[pigment] & ((1 << bit_pigment) - 1);
        int left_c2 = c2.weights[pigment] >> bit_pigment;
        int right_c2 = c2.weights[pigment] & ((1 << bit_pigment) - 1);
        new_c1.weights[pigment] = (left_c1 << bit_pigment) | (right_c2);
        new_c2.weights[pigment] = (left_c2 << bit_pigment) | (right_c1);

    }


    public void mutate(){
        boolean is_pigment_invalid = true;
        int point_mutation = 0;
        int pigment = 0;
        int bit_pigment = 0;
        while (is_pigment_invalid) {
            point_mutation = (int) (Math.random()*(NUM_BITS*NUM_PIGMENTS-1));
            pigment = point_mutation/NUM_BITS;
            bit_pigment = NUM_BITS-(point_mutation%NUM_BITS + 1);
            is_pigment_invalid = invalid_pigments[pigment];
        }

        this.weights[pigment] = this.weights[pigment] ^ (1 << bit_pigment);
    }

}
