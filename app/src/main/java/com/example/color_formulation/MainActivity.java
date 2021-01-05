package com.example.color_formulation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    int NUM_CHROMOSOMES = 1000; //number of chromosomes/color formulations
    int NUM_COLORANTS = 21; // number of colorants(pigments and flocking) and elastomer.
    int NUM_LAMBDA = 31; //number of wavelengths (400nm to 700nm - in steps of 10nm)
    int ELASTOMER_GENE = 20; //elastomer position on the gene sequence ("loci")
    int NUM_OF_TOP_CHROMOSOMES = 5; //number of top formulations
    int NUM_OF_TOP_GENES = 5; //number of top genes
    int NUM_ITERATIONS = 100; //number of iterations
    double MUTATION_RATE = 0.1;
    double SAMPLE_THICKNESS = 2.0;//thickness of the prosthesis in millimeters

    public static final String EXTRA_TEXT_PIGMENTS = "com.application.myApplication.TEXT_PIGMENTS";

    double grams_prosthesis, thickness_prosthesis;
    LAB target;
    LAB target_illA;
    Chromosome[] gene = new Chromosome[NUM_CHROMOSOMES];
    int[] top_chromosomes = new int[NUM_OF_TOP_CHROMOSOMES];
    double[] top_fitting = new double[NUM_OF_TOP_CHROMOSOMES];
    double[] top_fitting_D65 = new double[NUM_OF_TOP_CHROMOSOMES];
    double[] top_fitting_IlA = new double[NUM_OF_TOP_CHROMOSOMES];

    double[][] R_inf = new double[NUM_COLORANTS][NUM_LAMBDA];
    double[][] Rsp = new double[NUM_COLORANTS][NUM_LAMBDA];
    double[][] Rsb = new double[NUM_COLORANTS][NUM_LAMBDA];
    double[] Rp = new double[NUM_LAMBDA];
    double[] Rb = new double[NUM_LAMBDA];

    double[][] S = new double[NUM_COLORANTS][NUM_LAMBDA];
    double[][] K = new double[NUM_COLORANTS][NUM_LAMBDA];
    double[][] K_CHROMOSOME = new double[NUM_CHROMOSOMES][NUM_LAMBDA];
    double[][] S_CHROMOSOME = new double[NUM_CHROMOSOMES][NUM_LAMBDA];

    double[][] corrected_R = new double[NUM_CHROMOSOMES][NUM_LAMBDA];
    LAB[] LAB_colors = new LAB[NUM_CHROMOSOMES];
    LAB[] LAB_colors_illuminantA = new LAB[NUM_CHROMOSOMES];

    double[] fitting = new double[NUM_CHROMOSOMES];
    double[] fitting_D65 = new double[NUM_CHROMOSOMES];
    double[] fittingIlA = new double[NUM_CHROMOSOMES];
    boolean SPECTROPHOTOMETER = true;
    boolean[] invalid_colorants = new boolean[NUM_COLORANTS];

    private String[] backgrounds = {"" +
            "Ideal black/Preto Ideal", "Black/Preto", "White/Branco", "Light Skin/Pele clara"};
    private String chosen_background;

    private static final int GALLERY_REQUEST_CODE = 1234;
    private static final int LAB_REQUEST_CODE = 1235;
    public static final String EXTRA_URI_PICTURE = "com.application.myApplication.SELECTED_IMAGE";

    double startTime;

    EditText edit_a;
    EditText edit_b;
    EditText edit_L;
    EditText edit_grams;
    EditText edit_prosthesis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner select_background = findViewById(R.id.select_background);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, backgrounds);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_background.setAdapter(adapter);
        select_background.setOnItemSelectedListener(this);

        edit_L = findViewById(R.id.value_L);
        edit_a = findViewById(R.id.value_a);
        edit_b = findViewById(R.id.value_b);
        edit_grams = findViewById(R.id.grams_prosthesis);
        edit_prosthesis = findViewById(R.id.thickness_prosthesis);

        Button select_image_button = findViewById(R.id.select_image);
        select_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image/Selecione uma imagem"), GALLERY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();
            Intent intent = new Intent(this, ImageActivity.class);
            intent.putExtra(EXTRA_URI_PICTURE, imageData.toString());
            startActivityForResult(intent, LAB_REQUEST_CODE);
        }
        if(requestCode == LAB_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            edit_a.setText(String.valueOf(data.getDoubleExtra(ImageActivity.value_a, 0)));
            edit_b.setText(String.valueOf(data.getDoubleExtra(ImageActivity.value_b, 0)));
            edit_L.setText(String.valueOf(data.getDoubleExtra(ImageActivity.value_L, 0)));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        chosen_background = backgrounds[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        chosen_background = backgrounds[0];
    }

    /**
     * Called when the user clicks the Clear button
     */
    public void clearTexts(View view) {
        edit_a.setText("");
        edit_b.setText("");
        edit_L.setText("");
        edit_grams.setText("");
        edit_prosthesis.setText("");
    }

    /**
     * Called when the user taps the Send button
     */
    public void selectChromosomes(View view) {
        startTime = System.nanoTime();

        SpectralVariables.populateVariables(SPECTROPHOTOMETER, Rsb, Rb, Rsp, Rp, R_inf);
        spectral_curve_calculation();
        create_population();

        double target_L = Double.parseDouble(edit_L.getText().toString());
        double target_a = Double.parseDouble(edit_a.getText().toString());
        double target_b = Double.parseDouble(edit_b.getText().toString());
        target = new LAB(target_L, target_a, target_b);
        target_illA = ColorTransformation.convertIlluminantLAB(
                target, ColorTransformation.ILLUMINANT_A_10_DEGREES
        );
        grams_prosthesis = Double.parseDouble(edit_grams.getText().toString());
        thickness_prosthesis = Double.parseDouble(edit_prosthesis.getText().toString());

        iterateCromossomes(false);

        selectTopPigments();
        Chromosome.set_invalid_pigments(invalid_colorants);
        for (int n = 0; n < NUM_CHROMOSOMES; n++) {
            gene[n].initialize_weights();
        }

        iterateCromossomes(true);

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(EXTRA_TEXT_PIGMENTS, createTextForCromossomes(grams_prosthesis));
        startActivity(intent);
    }

    public String createTextForCromossomes(double grams_prosthesis){
        StringBuilder text_pigments = new StringBuilder();
        double grams_pigment;
        for (int i = 0; i< NUM_OF_TOP_CHROMOSOMES; i++){
            //text_pigments += "Option/Opção "+(i+1)+" (ΔEab "+String.format("%.2f", 1/top_fitting[i])+" Fitting "+String.format("%.2f", top_fitting[i])+"):\n";
            text_pigments.append("Option/Opção ").append(i + 1).append(":\nΔE*D65 = ").append(String.format("%.2f", top_fitting_D65[i])).append("; ΔE*IlA = ").append(String.format("%.2f", top_fitting_IlA[i]));
            text_pigments.append("; Fitting = ").append(String.format("%.2f", top_fitting[i])).append("\n");
            for (int pigment = 0; pigment< NUM_COLORANTS; pigment++){
                if (pigment == ELASTOMER_GENE) continue;
                grams_pigment = gene[top_chromosomes[i]].weights[pigment]*0.00001*grams_prosthesis;
                if (grams_pigment < 0.01) continue;
                text_pigments.append(Chromosome.pigment_names[pigment]);
                text_pigments.append(": ");
                text_pigments.append(String.format(Locale.getDefault(), "%.2f", grams_pigment));
                text_pigments.append("g\n");
            }
            text_pigments.append("\n");
        }

        double totalTime = (System.nanoTime() - startTime)/1000000000;
        text_pigments.append("\n Execution time: ").append(String.format(Locale.getDefault(), "%.2f", totalTime)).append("s\n");

        return text_pigments.toString();
    }
    //----------------------------------------------------------------------------------------------
    public void iterateCromossomes(boolean useIlluminantA){
        for (int iter = 0; iter < NUM_ITERATIONS; iter++) {
            calculo_k_e_s_mistura();
            chromosome_reflectance_calculation();
            chromosome_to_LAB();
            calculate_fitting(useIlluminantA);
            if (iter == NUM_ITERATIONS - 1) selectTopCromossomes();
            else selection();
        }
    }

    //----------------------------------------------------------------------------------------------

    public void selectTopCromossomes(){

        for (int n = 0; n< NUM_OF_TOP_CHROMOSOMES; n++){
            for (int chromosome = 1; chromosome< NUM_CHROMOSOMES; chromosome++){
                if (fitting[chromosome] > fitting[top_chromosomes[n]]) {
                    top_chromosomes[n] = chromosome;
                }
            }
            top_fitting[n] = fitting[top_chromosomes[n]];
            top_fitting_D65[n] = fitting_D65[top_chromosomes[n]];
            top_fitting_IlA[n] = fittingIlA[top_chromosomes[n]];
            fitting[top_chromosomes[n]] = 0;
        }
    }

    //----------------------------------------------------------------------------------------------
    public void selectTopPigments(){
        int top_pigment;
        float weight_top_pigment;
        float[] weights_pigments = new float[NUM_COLORANTS];
        int[] top_pigments = new int[NUM_OF_TOP_GENES];

        for (int i = 0; i < NUM_OF_TOP_CHROMOSOMES; i++){
            for (int pigment = 0; pigment < NUM_COLORANTS; pigment ++){
                weights_pigments[pigment] += gene[top_chromosomes[i]].weights[pigment];
            }
        }

        for (int i = 0; i < NUM_OF_TOP_GENES; i++){
            top_pigment = 0;
            weight_top_pigment = 0;
            for (int pigment = 0; pigment < NUM_COLORANTS; pigment++) {
                if (weights_pigments[pigment] > weight_top_pigment) {
                    top_pigment = pigment;
                    weight_top_pigment = weights_pigments[pigment];
                }
            }
            if (weight_top_pigment == 0) {
                top_pigments[i] = -1;
                break;
            }
            top_pigments[i] = top_pigment;
            weights_pigments[top_pigment] = 0;
        }

        Arrays.fill(invalid_colorants, true);
        for (int i = 0; i < NUM_OF_TOP_GENES; i++){
            if (top_pigments[i] == -1) break;
            invalid_colorants[top_pigments[i]] = false;
        }

    }

    //----------------------------------------------------------------------------------------------

    public void selection() {

        double fitting_media = 0;
        int chromosome_lenght = fitting.length;
        int num_genes_mantem = 0;
        int index_new_gene = 0;
        Chromosome[] genes_novo = new Chromosome[NUM_CHROMOSOMES +1];
        for (int i = 0; i< NUM_CHROMOSOMES +1; i++){
            genes_novo[i] = new Chromosome();
        }

        // mean fitting calculation
        for (double v : fitting) {
            fitting_media += v;
        }
        fitting_media /= fitting.length;

        // selection: above average chromosomes
        for (int index = 0; index < chromosome_lenght; index++){
            if (fitting[index] > fitting_media) {
                genes_novo[index_new_gene] = gene[index];
                index_new_gene++;
                num_genes_mantem++;
            }
        }

        //crossover colocando nova geração em lista_nova
        //crossover entre itens aleatorios
        //local do crossover tambem aleatorio

        while (index_new_gene < NUM_CHROMOSOMES) {
            //crossover: chromosomes selection
            int cromossomo_crossover_1 = (int) (Math.random() * num_genes_mantem);
            int cromossomo_crossover_2 = (int) (Math.random() * num_genes_mantem);

            //transfer values to index_new_gene array
            Chromosome.crossover(gene[cromossomo_crossover_1], gene[cromossomo_crossover_2], genes_novo[index_new_gene], genes_novo[index_new_gene + 1]);
            index_new_gene += 2;
        }

        // Mutation
        int num_mutation = (int) (MUTATION_RATE*(NUM_CHROMOSOMES -1)* Chromosome.NUM_BITS);
        for (int i=0; i<num_mutation; i++){
            int cromossomo_mutacao = (int) (Math.random() * NUM_CHROMOSOMES);
            genes_novo[cromossomo_mutacao].mutate();
        }


        System.arraycopy(genes_novo,0,gene,0,gene.length);
    }

    //----------------------------------------------------------------------------------------------
    public void calculate_fitting(boolean useIlluminantA) {
        /*
        double L = 70.50;
        double A = 5.69;
        double B = 16.42;
         */

        for (int chromosome = 0; chromosome < NUM_CHROMOSOMES; chromosome++){
            if (LAB_colors[chromosome].getL() < 0 || LAB_colors[chromosome].getL() > 100 ||
                    LAB_colors[chromosome].getA() < -128 || LAB_colors[chromosome].getA() > 128 ||
                    LAB_colors[chromosome].getB() < -128 || LAB_colors[chromosome].getB() > 128) {
                fitting[chromosome] = 0;
            }
            else {

                double fittingD65 = Math.sqrt(Math.pow(LAB_colors[chromosome].getL() - target.getL(), 2)
                        + Math.pow(LAB_colors[chromosome].getA() - target.getA(), 2)
                        + Math.pow(LAB_colors[chromosome].getB() - target.getB(), 2));

                double fittingA = Math.sqrt(Math.pow(LAB_colors_illuminantA[chromosome].getL() - target_illA.getL(), 2)
                        + Math.pow(LAB_colors_illuminantA[chromosome].getA() - target_illA.getA(), 2)
                        + Math.pow(LAB_colors_illuminantA[chromosome].getB() - target_illA.getB(), 2));

                fitting[chromosome] = useIlluminantA ? 1 / (fittingA*fittingA + fittingD65*fittingD65 + 0.00001) : 1 / (fittingD65 + 0.00001);
                fitting_D65[chromosome] = fittingD65;
                fittingIlA[chromosome] = fittingA;
            }
        }

    }

    //----------------------------------------------------------------------------------------------

    public void create_population() {
        Chromosome.set_NUM_PIGMENTS(NUM_COLORANTS);
        Chromosome.set_invalid_pigments(invalid_colorants);
        for (int n = 0; n < NUM_CHROMOSOMES; n++) {
            gene[n] = new Chromosome();
            gene[n].initialize_weights();
        }
    }

    //----------------------------------------------------------------------------------------------

    public void spectral_curve_calculation() {
        double[][] a = new double[NUM_COLORANTS][NUM_LAMBDA];
        double[][] b = new double[NUM_COLORANTS][NUM_LAMBDA];

        for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) /*de 400 a 700 de 10 em 10 = QTDE_LAMBDA*/ {
            Rp[lambda] /= 100;
            Rb[lambda] /= 100;

            for (int pigmento = 0; pigmento < NUM_COLORANTS; pigmento++) {
                Rsp[pigmento][lambda] /= 100;
                Rsb[pigmento][lambda] /= 100;
                R_inf[pigmento][lambda] /= 100;
            }
        }

        Rb = R_correction(Rb);
        Rp = R_correction(Rp);
        Rsp = R_correction(Rsp);
        Rsb = R_correction(Rsb);
        R_inf = R_correction(R_inf);

        double arc_cotgh;

        for (int colorant = 0; colorant < NUM_COLORANTS; colorant++) {
            for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) /*from 400 to 700 (steps of 10nm) = NUM_LAMBDA*/ {

                //-------------calculando a e b com infinito----------------//
                a[colorant][lambda] = 0.5*(1/R_inf[colorant][lambda] + R_inf[colorant][lambda]);
                b[colorant][lambda] = Math.sqrt(a[colorant][lambda]*a[colorant][lambda] - 1);
                //--------------------------------------------------------//

                /*
                arc_cotgh = (1.0 - a[colorant][lambda] * Rsp[colorant][lambda]) /
                        (b[colorant][lambda] * Rsp[colorant][lambda]);
                 */
                arc_cotgh = (1.0 - a[colorant][lambda] * (Rsb[colorant][lambda] + Rb[lambda]) + Rsb[colorant][lambda]*Rb[lambda]) /
                        (b[colorant][lambda] * (Rsb[colorant][lambda] - Rb[lambda]));
                //----------------------------------------------------------------------------------

                arc_cotgh = arc_cotgh(arc_cotgh);

                S[colorant][lambda] = (1.0 / (b[colorant][lambda] * SAMPLE_THICKNESS)) * arc_cotgh;

                K[colorant][lambda] = S[colorant][lambda] * (a[colorant][lambda] - 1.0);

                if (Double.isNaN(K[colorant][lambda]) || Double.isNaN(S[colorant][lambda])) {
                    invalid_colorants[colorant] = true;
                    K[colorant][lambda] = 0;
                    S[colorant][lambda] = 0;
                }

            }
        }

        // Elastomer cannot be altered
        invalid_colorants[ELASTOMER_GENE] = true;

        // Iteration using all colorants (excludes elastomer),K = (K - K0)/c
        for (int colorant = 0; colorant < NUM_COLORANTS; colorant++) {
            if (invalid_colorants[colorant]) continue;
            for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) /*de 400 a 700 de 10 em 10 = QTDE_LAMBDA*/ {
                /* pigment (de 0 a 8) multiply 1%. Flockings (de 9 a 19) multiply 2% (concentration OF THE EXPERIMENT)*/
                if (colorant < 9) {
                    S[colorant][lambda] = (S[colorant][lambda] - S[20][lambda])/0.0099;
                    K[colorant][lambda] = (K[colorant][lambda] - K[20][lambda])/0.0099;
                } else if (colorant <= 19) {
                    S[colorant][lambda] = (S[colorant][lambda] - S[20][lambda])/0.0196;
                    K[colorant][lambda] = (K[colorant][lambda] - K[20][lambda])/0.0196;
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public double arc_cotgh(double arc_cotgh) {
        double arc_cotgh_resp;
        arc_cotgh_resp = 0.5*(Math.log((arc_cotgh + 1.0) / (arc_cotgh - 1.0)));
        return arc_cotgh_resp;
    }

    //----------------------------------------------------------------------------------------------
    public double cotgh(double valor) {
        return Math.cosh(valor) / Math.sinh(valor);
    }

    //----------------------------------------------------------------------------------------------
    // calculo k e s por concentracao de pigmento
    public void calculo_k_e_s_mistura() {
        for (int cromo = 0; cromo < NUM_CHROMOSOMES; cromo++) {
            for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) {
                K_CHROMOSOME[cromo][lambda] = 0;
                S_CHROMOSOME[cromo][lambda] = 0;
                for (int pigmento = 0; pigmento < NUM_COLORANTS; pigmento++) {

                    if (pigmento == ELASTOMER_GENE) {
                        K_CHROMOSOME[cromo][lambda] += K[pigmento][lambda];
                        S_CHROMOSOME[cromo][lambda] += S[pigmento][lambda];
                    } else {
                        K_CHROMOSOME[cromo][lambda] += K[pigmento][lambda] * gene[cromo].weights[pigmento] * 0.00001;
                        S_CHROMOSOME[cromo][lambda] += S[pigmento][lambda] * gene[cromo].weights[pigmento] * 0.00001;
                    }
                }

                //if (K_cromossomo[cromo][lambda] < 0) K_cromossomo[cromo][lambda] = 1e-9;
                //if (S_cromossomo[cromo][lambda] < 0) S_cromossomo[cromo][lambda] = 1e-9;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    public double calculate_R(int lambda, int cromossomo){
        double a, b, R;
        double S = S_CHROMOSOME[cromossomo][lambda];
        double K = K_CHROMOSOME[cromossomo][lambda];

        a = 1 + K/S;
        b = Math.sqrt(a*a - 1);

        if(chosen_background.equals("Ideal black/Preto Ideal")){
            R = 1/(a+b*cotgh(b*S*thickness_prosthesis));
        }
        else if(chosen_background.equals("Black/Preto")){
            R = (1 - Rp[lambda]*(a - b*cotgh(b*S*thickness_prosthesis)))/
                    (a - Rp[lambda] + b*cotgh(b*S*thickness_prosthesis));
        }
        else if(chosen_background.equals("White/Branco")){
            R = (1 - Rb[lambda]*(a - b*cotgh(b*S*thickness_prosthesis)))/
                    (a - Rb[lambda] + b*cotgh(b*S*thickness_prosthesis));
        }
        else {
            // R_inf[0] is light skin pigment at infinite optical thickness
            R = (1 - R_inf[0][lambda]*(a - b*cotgh(b*S*thickness_prosthesis)))/
                    (a - R_inf[0][lambda] + b*cotgh(b*S*thickness_prosthesis));
        }
        return R;
    }

    //----------------------------------------------------------------------------------------------
    public void chromosome_reflectance_calculation() {
        double K1 = 0.039;
        double K2 = 0.540;
        double elastomer_refraction_index = 1.415; //n
        double R;

        for (int chromosome = 0; chromosome < NUM_CHROMOSOMES; chromosome++) {
            for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) {

                R = calculate_R(lambda, chromosome);

                //R’ = (1 − k1)(1 − k2)R / (1 − k2R)
                corrected_R[chromosome][lambda] = ((1 - K1) * (1 - K2) * R) /
                        (1 - K2 * R);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    //R_correction is the reflectance modified by boundary reflection corrections
    public double[][] R_correction(double[][] R) {
        double K1 = 0.039; /*the fraction of the incident light with externally specularly reflected
                    on the front surface of the sample which can be calculated by Fresnel’s law*/
        double K2 = 0.540; /*the fraction oflight internally diffusely reflected on the front
                    surface of the sample*/
        double[][] R_linha2 = new double[NUM_COLORANTS][NUM_LAMBDA];

        for (int chromosome = 0; chromosome < NUM_COLORANTS; chromosome++) {
            for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) {

                //R’ = (1 − k1)(1 − k2)R / 1 − k2R --INVERTIDO, DESCOBRIR R A PARTIR DO R' MEDIDO
                R_linha2[chromosome][lambda] = R[chromosome][lambda]/(1-K2-K1+(K2*R[chromosome][lambda]));
            }
        }
        return R_linha2;
    }

    //----------------------------------------------------------------------------------------------
    public double[] R_correction(double[] R) {
        double K1 = 0.039;
        double K2 = 0.540;
        double[] R_linha2 = new double[NUM_LAMBDA];

        for (int lambda = 0; lambda < NUM_LAMBDA; lambda++) {

            //R’ = (1 − k1)(1 − k2)R / 1 − k2R --Find R from R'
            //R_linha2[lambda] = R[lambda]/(1-K2-K1+(K2*R[lambda]));
            R_linha2[lambda] = R[lambda]/((1-K1)*(1-K2) + K2*R[lambda]);
        }
        return R_linha2;
    }

    //----------------------------------------------------------------------------------------------
    public void chromosome_to_LAB() {
        for (int index = 0; index < NUM_CHROMOSOMES; index++) {
            if (verify_corrected_R(index)) {
                LAB_colors[index] = ColorTransformation.spectrumToLAB(
                        corrected_R[index], ColorTransformation.ILLUMINANT_D65_10_DEGREES
                );
                LAB_colors_illuminantA[index] = ColorTransformation.spectrumToLAB(
                        corrected_R[index], ColorTransformation.ILLUMINANT_A_10_DEGREES
                );
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public boolean verify_corrected_R(int index)
    {
        for (int i = 0; i < NUM_LAMBDA; i++){
            if (Double.isNaN(corrected_R[index][i]))
                return false;
        }
        return true;
    }

}