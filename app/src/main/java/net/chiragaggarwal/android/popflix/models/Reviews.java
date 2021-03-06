package net.chiragaggarwal.android.popflix.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class Reviews {
    private ArrayList<Review> reviews;
    private static final String RESULTS = "results";

    public Reviews(Review... reviews) {
        initializeReviews(reviews);
    }

    public static Reviews fromJson(JSONObject reviewsJsonObject) throws JSONException, ParseException {
        JSONArray reviewsResults = reviewsJsonObject.getJSONArray(RESULTS);
        Reviews reviews = new Reviews();
        for (Integer reviewIndex = 0; reviewIndex < reviewsResults.length(); reviewIndex++) {
            JSONObject reviewJsonObject = reviewsResults.getJSONObject(reviewIndex);
            Review review = Review.fromJson(reviewJsonObject);
            reviews.add(review);
        }
        return reviews;
    }

    public int count() {
        return getNumberOfReviews();
    }

    public Review get(int reviewIndex) {
        if (isReviewIndexValid(reviewIndex))
            return null;
        return this.reviews.get(reviewIndex);
    }

    public boolean any() {
        return getNumberOfReviews() > 0;
    }

    private void initializeReviews(Review[] reviews) {
        this.reviews = new ArrayList<>();
        for (Integer reviewIndex = 0; reviewIndex < reviews.length; reviewIndex++) {
            Review review = reviews[reviewIndex];
            this.reviews.add(review);
        }
    }

    private void add(Review review) {
        this.reviews.add(review);
    }

    private int getNumberOfReviews() {
        return this.reviews.size();
    }

    private boolean isReviewIndexValid(int reviewIndex) {
        return isReviewIndexNegative(reviewIndex) || isReviewIndexGreaterThanAvailableIndices(reviewIndex);
    }

    private boolean isReviewIndexNegative(int reviewIndex) {
        return reviewIndex < 0;
    }

    private boolean isReviewIndexGreaterThanAvailableIndices(int reviewIndex) {
        return reviewIndex >= getNumberOfReviews();
    }
}
