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
    static int itemCount;
    static int userCount;

    // Toy rating matrix example
    // Rows represent items and columns represent users
    // Values represent a rating from 1-5 of an item
    // 0 represents an item that a user has not yet rated
    static ArrayList<ArrayList<Double>> ratings = new ArrayList<>();

    // Array list for row means
    static double [] itemRatingMean;

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
     */
    private static ArrayList<ArrayList<Double>> centeredCosineSimilarity(ArrayList<ArrayList<Double>> normalizedRatings){

        ArrayList<ArrayList<Double>> ccsMatrix = new ArrayList<>();
        for(int i = 0; i < itemCount; i++)
            ccsMatrix.add(new ArrayList<Double>(itemCount));

        for(int item1 = 0; item1 < itemCount; item1++){
            for(int item2 = 0; item2 < itemCount; item2++) {

                double similarity, dotProduct, item1Magnitude, item2Magnitude;
                dotProduct = item1Magnitude = item2Magnitude= 0.0;

                for (int user = 0; user < userCount; user++) {
                    dotProduct += normalizedRatings.get(item1).get(user) * normalizedRatings.get(item2).get(user);
                    item1Magnitude += normalizedRatings.get(item1).get(user) * normalizedRatings.get(item1).get(user);
                    item2Magnitude += normalizedRatings.get(item2).get(user) * normalizedRatings.get(item2).get(user);
                }
                double denominator = (Math.sqrt(item1Magnitude) * Math.sqrt(item2Magnitude));
                similarity = (denominator != 0 ? dotProduct / denominator : 0.0); // check that we do not divide by 0
                ccsMatrix.get(item1).add(similarity);
            }
        }

        return ccsMatrix;
    }

    /*
        Implement item-item collaborative filter to estimate missing ratings for items
     */
    private static void item_itemCollaborativeFilter(ArrayList<ArrayList<Double>> ccsMatrix){

        DecimalFormat df = new DecimalFormat("####0.00");

        // Get item neighborhood for all items in descending order based on similarity
        ArrayList<ArrayList<Item>> itemNeighborhood = new ArrayList<>();
        int i = 0;
        for(ArrayList<Double> items : ccsMatrix){

            ArrayList<Item> neighborhood = new ArrayList<>();

            for(int similarity = 0; similarity < items.size(); similarity++){
                // Don't add item to its own neighborhood
                if(similarity != i)
                    neighborhood.add(new Item(similarity, items.get(similarity)));
            }

            // Sort the item neighborhood
            Collections.sort(neighborhood, new CustomComparator());
            Collections.reverse(neighborhood);
            itemNeighborhood.add(neighborhood);

            i++;
        }
        System.out.println("\n");


        // summations of similarity;
        double predictedRating, similarityByRating, similarity;

        // Traverse ratings matrix and estimate missing ratings
        for(int item = 0; item < itemCount; item++){
            for(int user = 0; user < userCount; user++){

                // If we have a missing rating
                if(ratings.get(item).get(user) == 0){

                    // reset summations of similarity
                    predictedRating = similarityByRating = similarity = 0.0;

                    // Pick the top N most similar items to the current item that have been rated by the current user
                    int n = 0;
                    for(Item in : itemNeighborhood.get(item)){
                        if(ratings.get(in.item).get(user) != 0.0){
                            similarityByRating += (in.similarity) * ratings.get(in.item).get(user);
                            similarity += (in.similarity);
                            n++;
                        }
                        if(n == N) break;
                    }

                    predictedRating = (similarity != 0 ? similarityByRating / similarity : 0.0);

                    ratings.get(item).set(user, (predictedRating < 1.0 ? 1.0 : (predictedRating > 5.0 ? 5.0 : predictedRating)));
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

        itemCount = ratings.size();
        userCount = ratings.get(0).size();
        itemRatingMean = new double[itemCount];

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
        item_itemCollaborativeFilter(ccsMatrx);

        System.out.println("Predicted Ratings Matrix:\n");
        printMatrix(ratings);
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