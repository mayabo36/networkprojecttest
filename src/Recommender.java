/*
    Implementation of item-item collaborative recommender system.
    Works on toy example from: https://www.youtube.com/watch?v=h9gpufJFF-0
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Recommender {

    // Item neighborhood size, the N most similar items we want to use in our collaborative filter algorithm
    static int N = 2;

    // row and column count for rating matrix
    static int itemCount = 6;
    static int userCount = 12;

    // Toy rating matrix example
    // Rows represent items and columns represent users
    // Values represent a rating from 1-5 of an item
    // 0 represents an item that a user has not yet rated
    static ArrayList<ArrayList<Double>> ratings = new ArrayList<>();

    // Array list for row means
    static double [] itemRatingMean  = new double [itemCount];

    /*
        Normalize ratings by subtracting row mean.
        Make 0 the average rating for a user.
     */
    private static ArrayList<ArrayList<Double>> normalizeRatings(){

        ArrayList<ArrayList<Double>> normalizedMatrix = new ArrayList<>();
        for(int i = 0; i < itemCount; i++)
            normalizedMatrix.add(new ArrayList<Double>(itemCount));

        // Calculate row mean for all items
        for(int item = 0; item < itemCount; item++){

            double ratingMean = 0.0;
            int reviewCnt = 0;

            for(int user = 0; user < userCount; user++){
                reviewCnt = (ratings.get(item).get(user) == 0 ? reviewCnt : reviewCnt + 1);
                ratingMean += ratings.get(item).get(user);
            }

            ratingMean /= reviewCnt;
            itemRatingMean[item] = ratingMean;
        }

        // Subtract row mean from rating to normalize
        for(int item = 0; item < itemCount; item++){
            for(int user = 0; user < userCount; user++){
                normalizedMatrix.get(item).add((ratings.get(item).get(user) == 0 ? ratings.get(item).get(user) : ratings.get(item).get(user) - itemRatingMean[item]));
            }
        }

        return normalizedMatrix;
    }

    /*
        Debugger function for printing rating matrix
     */
    private static void printMatrix(ArrayList<ArrayList<Double>> matrix){

        DecimalFormat df = new DecimalFormat("####0.00");

        for(ArrayList<Double> item : matrix){
            for(double ratingbyUser : item){
                System.out.print(df.format(ratingbyUser) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /*
        Calculate the centered cosine similarity matrix
        TODO: rename ratings to normalizedMatrix
     */
    private static ArrayList<ArrayList<Double>> centeredCosineSimilarity(ArrayList<ArrayList<Double>> ratings){

        ArrayList<ArrayList<Double>> ccsMatrix = new ArrayList<>();
        for(int i = 0; i < itemCount; i++)
            ccsMatrix.add(new ArrayList<Double>(itemCount));

        for(int item1 = 0; item1 < itemCount; item1++){
            for(int item2 = 0; item2 < itemCount; item2++) {

                double similarity, dotProduct, item1Magnitude, item2Magnitude;
                dotProduct = item1Magnitude = item2Magnitude= 0.0;

                for (int user = 0; user < userCount; user++) {
                    dotProduct += ratings.get(item1).get(user) * ratings.get(item2).get(user);
                    item1Magnitude += ratings.get(item1).get(user) * ratings.get(item1).get(user);
                    item2Magnitude += ratings.get(item2).get(user) * ratings.get(item2).get(user);
                }

                similarity = dotProduct / (Math.sqrt(item1Magnitude) * Math.sqrt(item2Magnitude));
                ccsMatrix.get(item1).add(similarity);
            }
        }

        return ccsMatrix;
    }

    /*
        Implement item-item collaborative filter to estimate missing ratings for items
        TODO: finish this
     */
    private static void item_itemCollaborativeFilter(ArrayList<ArrayList<Double>> ccsMatrix){

        ArrayList<ArrayList<Integer>> itemNeigborhood = new ArrayList<>();
        for(int user = 0; user < userCount; user++){

            ArrayList<Integer> neighborhood = new ArrayList<>();

            for(int item = 0; item < itemCount; item++){
                if(ratings.get(item).get(user) != 0){
                    neighborhood.add(item);
                }
            }
            itemNeigborhood.add(neighborhood);
        }


        for(int item = 0; item < itemCount; item++){
            for(int user = 0; user < userCount; user++){

                // Estimate rating for items that user has not yet rated
                if(ratings.get(item).get(user) == 0){
                    ArrayList<Item> items = new ArrayList<>();
                    for(int i = 0; i < itemNeigborhood.get(user).size(); i++){
                        items.add(new Item(itemNeigborhood.get(user).get(i), ccsMatrix.get(item).get(i)));
                    }
                    Collections.sort(items, new CustomComparator());
                    Collections.reverse(items);

                    for(int i = 0; i < itemNeigborhood.get(user).size(); i++){
                        System.out.print(items.get(i).similarity + " ");
                    }
                    System.out.println();
                }
            }
        }
    }

    /**/
    public static void main(String[] args){

        // Initializing toy input matrix
        ratings.add(new ArrayList<Double>(Arrays.asList(1.0, 0.0, 3.0, 0.0, 0.0, 5.0, 0.0, 0.0, 5.0, 0.0, 4.0, 0.0)));
        ratings.add(new ArrayList<Double>(Arrays.asList(0.0, 0.0, 5.0, 4.0, 0.0, 0.0, 4.0, 0.0, 0.0, 2.0, 1.0, 3.0)));
        ratings.add(new ArrayList<Double>(Arrays.asList(2.0, 4.0, 0.0, 1.0, 2.0, 0.0, 3.0, 0.0, 4.0, 3.0, 5.0, 0.0)));
        ratings.add(new ArrayList<Double>(Arrays.asList(0.0, 2.0, 4.0, 0.0, 5.0, 0.0, 0.0, 4.0, 0.0, 0.0, 2.0, 0.0)));
        ratings.add(new ArrayList<Double>(Arrays.asList(0.0, 0.0, 4.0, 3.0, 4.0, 2.0, 0.0, 0.0, 0.0, 0.0, 2.0, 5.0)));
        ratings.add(new ArrayList<Double>(Arrays.asList(1.0, 0.0, 3.0, 0.0, 3.0, 0.0, 0.0, 2.0, 0.0, 0.0, 4.0, 0.0)));

        System.out.println("Initial matrix:\n");
        printMatrix(ratings);

        // Normalize ratings
        ArrayList<ArrayList<Double>> normalizedMatrix = normalizeRatings();

        System.out.println("Normalized matrix:\n");
        printMatrix(normalizedMatrix);

        // Calculate centered cosine similarity
        ArrayList<ArrayList<Double>> ccsMatrx = centeredCosineSimilarity(normalizedMatrix);

        System.out.println("Centered Cosine Similarity Matrix:\n");
        printMatrix(ccsMatrx);

        // Calculate item-item collaborative filtering to estimate ratings
       // item_itemCollaborativeFilter(ccsMatrx);
    }

}

class Item{

    Double similarity;
    int item;

    public Item(int item, double similarity){
        this.item = item;
        this.similarity = similarity;
    }
}

class CustomComparator implements Comparator<Item> {
    @Override
    public int compare(Item o1, Item o2) {
        return o1.similarity.compareTo(o2.similarity);
    }
}