package com.elibrary.model;

/**
 * Represents a video course in the catalog.
 */
public class Course extends Content {

    private double durationHours;
    private int numberOfLectures;

    public Course(String contentId, String title, String author, String category,
                  SubscriptionTier minimumTier, double durationHours, int numberOfLectures) {
        super(contentId, title, author, category, minimumTier);
        this.durationHours = durationHours;
        this.numberOfLectures = numberOfLectures;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }

    public int getNumberOfLectures() {
        return numberOfLectures;
    }

    public void setNumberOfLectures(int numberOfLectures) {
        this.numberOfLectures = numberOfLectures;
    }

    @Override
    public String getContentType() {
        return "Course";
    }

    @Override
    public String getSummary() {
        return String.format("Course: \"%s\" - %.1f hrs across %d lectures",
                getTitle(), durationHours, numberOfLectures);
    }
}
